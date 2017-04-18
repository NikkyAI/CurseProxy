using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;
using Alpacka.Meta.AddOnService;
using Alpacka.Meta.LoginService;

namespace Alpacka.Meta 
{
    public class DownloadUtil
    {
        public static readonly Lazy<Task<AddOnServiceClient>> LazyAddonClient = new Lazy<Task<AddOnServiceClient>>( () => Authenticate() );
        public string OUTPUT { get; private set; }
        public HashSet<AddOn> failedAddons { get; private set; } = new HashSet<AddOn>();
        public bool verbose { get; set; }
        private static string CONFIG { get; set; }
        
        public DownloadUtil(string output, string config = null) {
            CONFIG = config ?? Constants.ConfigPath;
            OUTPUT = output ?? Path.Combine(Constants.CachePath, "output");
            if(!Directory.Exists(OUTPUT))
                Directory.CreateDirectory(OUTPUT);
                
            Console.WriteLine($"output: { OUTPUT }");
        }
        
        public HashSet<AddOn> reset_failed() {
            var tmp_set = new HashSet<AddOn>(failedAddons);
            failedAddons = new HashSet<AddOn>();
            return tmp_set;
        }
        
        public async Task process_addon(AddOn addon)
        {
            var client = await DownloadUtil.LazyAddonClient.Value;
            
            var directory = Path.Combine(OUTPUT, "addon");
            var addonFilesDirectory = Path.Combine(directory, $"{ addon.Id }");
            Directory.CreateDirectory(addonFilesDirectory);

            // var addon = await client.GetAddOnAsync(addon_id);

            Console.WriteLine($"[{addon.Name}] ({addon.Id}) {addon.Stage} {addon.Status}");
            var addon_json = addon.ToPrettyJson();
            File.WriteAllText(Path.Combine(directory, $"{ addon.Id }.json"), addon_json);

            var description = await client.v2GetAddOnDescriptionAsync(addon.Id);
            File.WriteAllText(Path.Combine(directory, $"{ addon.Id }/description.html"), description);

            var files = await client.GetAllFilesForAddOnAsync(addon.Id);
            //TODO go though unknown files in the directory and merge them in the files list
            File.WriteAllText(Path.Combine(directory, $"{ addon.Id }/files.json"), files.ToPrettyJson());
            await Task.WhenAll(files.Select( f => process_file(addon, f, addonFilesDirectory) ));
            // Console.WriteLine($"[{addon.Name}] finished");
        }
        
        public async Task<int> process_addon(int addonId, int fileId)
        {
            var client = await DownloadUtil.LazyAddonClient.Value;
            
            var directory = Path.Combine(OUTPUT, "addon");
            var addonFilesDirectory = Path.Combine(directory, $"{ addonId }");
            Directory.CreateDirectory(addonFilesDirectory);

            var addon = await client.GetAddOnAsync(addonId);
            
            Console.WriteLine($"[{addon.Name}] ({addon.Id}) {addon.Stage} {addon.Status}");
            
            var file = await client.GetAddOnFileAsync(addon.Id, fileId);
            if(file == null) {
                Console.WriteLine($"cannot find file {fileId} for [{addon.Name}]");
                return 1;
            }
            Console.WriteLine($"processing file: {file.FileName}");
            await process_file(addon, file, addonFilesDirectory);
            
            return 0;
            // var addon_json = addon.ToPrettyJson();
            // File.WriteAllText(Path.Combine(directory, $"{ addon.Id }.json"), addon_json);

            // var description = await client.v2GetAddOnDescriptionAsync(addon.Id);
            // File.WriteAllText(Path.Combine(directory, $"{ addon.Id }/description.html"), description);

            // var files = await client.GetAllFilesForAddOnAsync(addon.Id);
            // File.WriteAllText(Path.Combine(directory, $"{ addon.Id }/files.json"), files.ToPrettyJson());
            // await Task.WhenAll(files.Select( f => process_file(addon, f, addonFilesDirectory) ));
            // Console.WriteLine($"[{addon.Name}] finished");
        }

        public async Task process_file(AddOn addon, AddOnFile file, string addonDirectory)
        {
            var client = await DownloadUtil.LazyAddonClient.Value;
            
            Console.WriteLine($"directory: {addonDirectory}");
            
            //var file = await client.GetAddOnFileAsync(addon, file.Id);
           
            //Console.WriteLine($"{file.Id} {file.FileName} {file.FileStatus} { file.ReleaseType } {file.FileDate}");
            var file_json = file.ToPrettyJson();
            // if (file_json != expected_json)
            // {
            //     Console.WriteLine($"addon {addon} file: {file.Id}");
            //     throw new Exception($"addon {addon} file: {file.Id}");
            // }
            try {
                var changelog = await client.GetChangeLogAsync(addon.Id, file.Id);
                File.WriteAllText(Path.Combine(addonDirectory, $"{ file.Id }.changelog.html"), changelog);
            } catch (Exception e) {
                failedAddons.Add(addon);
                Console.WriteLine($": addon: {addon.Id} file: {file.Id} {file.FileName}");
                if(verbose) {
                    var errorpath = Path.Combine(OUTPUT, ".", $"{addon.Id}");
                    Directory.CreateDirectory(errorpath);
                    File.WriteAllText(Path.Combine(errorpath, $"{ file.Id }.changelog.error.txt"), $"{e.Message}\nStaclTrace:\n{e.StackTrace}\nSource: {e.Source}");
                }
                //throw new Exception ($"addon: {addon.Id} file: {file.Id} {file.FileName}", e);
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
}