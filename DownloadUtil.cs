using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;
using Alpacka.Meta.AddOnService;
using Alpacka.Meta.LoginService;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using System.Text;
using System.Diagnostics;

namespace Alpacka.Meta 
{
    public class DownloadUtil
    {
        public static readonly Lazy<Task<AddOnServiceClient>> LazyAddonClient = new Lazy<Task<AddOnServiceClient>>( () => Authenticate() );
        public string OUTPUT { get; private set; }
        public string ADDONPATH {
            get { return Path.Combine(OUTPUT, "addon"); }
            private set {}
        }
        public bool verbose { get; set; }
        public bool changelogs { get; set; }
        public Filter filter { get; set; } = Filter.Default;
        private static string configFile;
        public static string CONFIG {
            get { return configFile ?? Constants.ConfigPath; }
            set { configFile = value;
            }
        }
        
        public DownloadUtil(string output) {
            OUTPUT = output ?? Path.Combine(Directory.GetCurrentDirectory(), "out");
            if(!Directory.Exists(OUTPUT))
                Directory.CreateDirectory(OUTPUT);
                
            Console.WriteLine($"output: { OUTPUT }");
        }
        
        public static readonly JsonSerializerSettings serializerSettings = new JsonSerializerSettings {
            Formatting = Formatting.Indented,
            MissingMemberHandling = MissingMemberHandling.Error,
            ContractResolver = new CamelCasePropertyNamesContractResolver(),
            NullValueHandling = NullValueHandling.Ignore            
        };
        
        public static async Task processAll(AddOn[] addons, Func<AddOn, Task> asyncAction, string display = "projects") {
            var rnd = new Random();
            var randomData = addons.OrderBy(x => rnd.Next()).ToList();
            
            var batchSize = 100;
            var all = randomData//.Take(1000)
                .Select((addon, index) => new { addon, index })
                .GroupBy(e => (e.index / batchSize), e => e.addon);
            
            // DownloadUtil.reset_failed();
            var timer = new Stopwatch();
            timer.Start();
            int k = 0;
            int k_all = all.Count();
            foreach (var batch in all) {
                var tasks = Task.WhenAll(batch.Select(
                    (a) => {
                        return asyncAction(a);
                    }
                ));
                Console.WriteLine($"batch [{++k} / {k_all}]");
                await tasks;
                await Task.Delay(TimeSpan.FromSeconds(1));
            }
            
            //TODO: add retrying
            
            timer.Stop();
            Console.WriteLine($"all { display } were processed in '{ timer.Elapsed }'");
        }
        
        public async Task processAddon(AddOn addon)
        {
            var client = await DownloadUtil.LazyAddonClient.Value;
            
            var directory = ADDONPATH;
            var addonFilesDirectory = Path.Combine(directory, $"{ addon.Id }", "files");
            Directory.CreateDirectory(addonFilesDirectory);
            
            var indexFile = "index.json";
            
            //Console.WriteLine($"[{addon.Name}] Id: {addon.Id} Stage: {addon.Stage} Status: {addon.Status}");
            File.WriteAllText(Path.Combine(directory, $"{ addon.Id }", indexFile), addon.ToFilteredJson(filter));
            
            var description = await client.v2GetAddOnDescriptionAsync(addon.Id);
            File.WriteAllText(Path.Combine(directory, $"{ addon.Id }", "description.html"), description);
            
            var files = await client.GetAllFilesForAddOnAsync(addon.Id);
            //TODO: go though unknown files in the directory and merge them in the files list ?
            var failedFiles = new List<AddOnFileBundle>();
            await Task.WhenAll(files.Select( f => processFile(addon, f, addonFilesDirectory, failedFiles) ));
            
            while(failedFiles.Count != 0) {
                var tmp = failedFiles.ToArray();
                failedFiles = new List<AddOnFileBundle>();
                Console.WriteLine($"retrying files {tmp.Select(f => f.file.FileName).Aggregate((a,b) => a +" , "+ b)}");
                await Task.WhenAll(tmp.Select( f => processFile(addon, f.file, addonFilesDirectory, failedFiles) ));
            }
            
            // File.WriteAllText(Path.Combine(directory, $"{ addon.Id }", "files", "index.json"), files.ToFilteredJson(filter));
            
            // create file index based on all files in the folder
            var filesIndexFile = "index.json";
            
            var allFiles = new List<AddOnFile>();
            var directoryInfo = new DirectoryInfo(addonFilesDirectory);
            foreach (var fileinfo in directoryInfo.EnumerateFiles().OrderBy(f => f.Name)) {
                if(fileinfo.Name != filesIndexFile && fileinfo.Name.EndsWith(".json")) {
                    var file = JsonConvert.DeserializeObject<AddOnFile>(File.ReadAllText(fileinfo.FullName), serializerSettings);
                    allFiles.Add(file);
                }
            }
            File.WriteAllText(Path.Combine(addonFilesDirectory, filesIndexFile), allFiles.ToFilteredJson(filter));
            
            // var directoryInfo = new DirectoryInfo(addonFilesDirectory);
            // var allFilesArray = directoryInfo.EnumerateFiles()
            //     .OrderBy(f => f.Name)
            //     .Where(f => f.Name != filesIndexFile && f.Name.EndsWith(".json"))
            //     .Select(f => File.ReadAllText(f.FullName))
            //     .ToArray();
            // var allFilesText = String.Join("\n", allFilesArray);
            // File.WriteAllText(Path.Combine(addonFilesDirectory, filesIndexFile), allFilesText);
            
            // var allFilesBuilder = new StringBuilder();
            // var directoryInfo = new DirectoryInfo(addonFilesDirectory);
            // foreach (var fileinfo in directoryInfo.EnumerateFiles().OrderBy(f => f.Name)) {
            //     if(fileinfo.Name != filesIndexFile && fileinfo.Name.EndsWith(".json")) {
            //         allFilesBuilder.Append(File.ReadAllText(fileinfo.FullName));
            //         allFilesBuilder.Append("\n");
            //     }
            // }
            // File.WriteAllText(Path.Combine(addonFilesDirectory, filesIndexFile), allFilesBuilder.ToString());
        }
        
        public async Task<AddOnFileBundle[]> getFiles(AddOnFileKey[] keys)
        {
            var client = await DownloadUtil.LazyAddonClient.Value;
            var failedFiles = new List<AddOnFileBundle>();
            var finishedFiles = new List<AddOnFileBundle>();
            foreach (var key in keys) {
                var addon = await client.GetAddOnAsync(key.AddOnID);
                
                Console.WriteLine($"[{addon.Name}] ({addon.Id}) {addon.PackageType} {addon.Stage} {addon.Status}");
                
                var file = await client.GetAddOnFileAsync(addon.Id, key.FileID);
                if(file == null) {
                    Console.WriteLine($"cannot find file { key.FileID} for [{addon.Name}]");
                    return finishedFiles.ToArray();
                }
                finishedFiles.Add(new AddOnFileBundle(addon, file));
                var addonFilesDirectory = Path.Combine(ADDONPATH, $"{ addon.Id }");
                Directory.CreateDirectory(addonFilesDirectory);
                Console.WriteLine($"processing file: {file.FileName}");
                await processFile(addon, file, addonFilesDirectory, failedFiles);
            }
            
            while(failedFiles.Count != 0) {
                var tmp = failedFiles.ToArray();
                failedFiles = new List<AddOnFileBundle>();
                Console.WriteLine($"retrying files {tmp.Select(f => f.file.FileName).Aggregate((a,b) => a +" , "+ b)}");
                await Task.WhenAll(tmp.Select( f => processFile(f.addon, f.file, Path.Combine(ADDONPATH, $"{ f.addon.Id }"), failedFiles) ));
            }
            
            // create file index based on all files in the folder
            foreach (var addonId in keys.Select(k => k.AddOnID).Distinct()) {
                var filesIndexFile = "index.json";
                var addonFilesDirectory = Path.Combine(ADDONPATH, $"{ addonId }");
                
                var allFiles = new List<AddOnFile>();
                var directoryInfo = new DirectoryInfo(addonFilesDirectory);
                foreach (var fileinfo in directoryInfo.EnumerateFiles().OrderBy(f => f.Name)) {
                    if(fileinfo.Name != filesIndexFile && fileinfo.Name.EndsWith(".json")) {
                        var file = JsonConvert.DeserializeObject<AddOnFile>(File.ReadAllText(fileinfo.FullName), serializerSettings);
                        allFiles.Add(file);
                    }
                }
                File.WriteAllText(Path.Combine(addonFilesDirectory, filesIndexFile), allFiles.ToFilteredJson(filter));
            }
            return finishedFiles.ToArray();
        }

        public async Task processFile(AddOn addon, AddOnFile file, string addonDirectory, List<AddOnFileBundle> failedFiles)
        {
            var client = await DownloadUtil.LazyAddonClient.Value;
            
            // var file_json = file.ToPrettyJson();
            var file_json = file.ToFilteredJson(filter);
            
            if(changelogs) {
                try {
                    var changelog = await client.GetChangeLogAsync(addon.Id, file.Id);
                    File.WriteAllText(Path.Combine(addonDirectory, $"{ file.Id }.changelog.html"), changelog);
                } catch (Exception e) {
                    failedFiles.Add(new AddOnFileBundle(addon,file));
                    Console.WriteLine($"failed: addon: {addon.Id} file: {file.Id} {file.FileName}");
                    if(verbose) {
                        var errorpath = Path.Combine(OUTPUT, ".errors", $"{addon.Id}");
                        Directory.CreateDirectory(errorpath);
                        File.WriteAllText(Path.Combine(errorpath, $"{ file.Id }.changelog.error.txt"), $"{e.Message}\nStaclTrace:\n{e.StackTrace}\nSource: {e.Source}");
                    }
                    //throw new Exception ($"addon: {addon.Id} file: {file.Id} {file.FileName}", e);
                }
            }
            
            File.WriteAllText(Path.Combine(addonDirectory, $"{ file.Id }.json"), file_json);
            
        }
        
        private static async Task<AddOnServiceClient> Authenticate()
        {
            AddOnServiceClient client;
            var loginClient = new ClientLoginServiceClient(ClientLoginServiceClient.EndpointConfiguration.BinaryHttpsClientLoginServiceEndpoint);
             
            var deserializer = new DeserializerBuilder()
                .IgnoreUnmatchedProperties()
                .WithNamingConvention(new CamelCaseNamingConvention())
                .Build();
            
            string path = Path.Combine(CONFIG, "curse.yaml");
           
            LoginResponse loginResponse;
            
            using (var reader = new StreamReader(File.OpenRead(path)))
            {
                var request = deserializer.Deserialize<LoginRequest>(reader);
                loginResponse = await loginClient.LoginAsync(request);
                Console.WriteLine($"Login: {loginResponse.Status}");
            }
            
            client = new AddOnServiceClient(AddOnServiceClient.EndpointConfiguration.BinaryHttpsAddOnServiceEndpoint);
            client.Endpoint.EndpointBehaviors.Add(new TokenEndpointBehavior(loginResponse));
            
            return client;
        }
    }
    
    public class AddOnFileBundle
    {
        public AddOn addon { get; set; }
        public AddOnFile file { get; set; }
        public AddOnFileBundle(AddOn addon, AddOnFile file) {
            this.addon = addon;
            this.file = file;
        }
    }
}