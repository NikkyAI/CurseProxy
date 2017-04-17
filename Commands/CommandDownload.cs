using System;
using System.IO;
using System.Linq;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Collections.Generic;
using Microsoft.Extensions.CommandLineUtils;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;
using Alpacka.Meta.AddOnService;
using Alpacka.Meta.LoginService;


namespace Alpacka.Meta
{
    public class CommandDownload : CommandLineApplication
    {
        private AddOnServiceClient client;
        private static string OUTPUT;
        private static HashSet<AddOn> failedAddons;
        private static bool verbose;
        public CommandDownload()
        {
            Name = "download";
            Description = "Downloads curse meta information";
            
            var argMode = Argument("[mode]",
                "complete | hourly");
                
            var optOut = Option("-o | --out",
                "Output Directory", CommandOptionType.SingleValue);
                
            var optVerbose = Option("-v | --verbose",
                "save stacktraces and more info", CommandOptionType.NoValue);
                
            var optTest = Option("-t | --test",
                "Test flag", CommandOptionType.NoValue);
                
            HelpOption("-? | -h | --help");
            
             OnExecute(async () => {
                client = await Authenticate();
                
                OUTPUT = optOut.HasValue() ? optOut.Value() : Path.Combine(Constants.CachePath, "output");
                
                if(!Directory.Exists(OUTPUT))
                    Directory.CreateDirectory(OUTPUT);
                
                Console.WriteLine($"output: {OUTPUT}");
                
                var test = optTest.HasValue();
                verbose = optVerbose.HasValue();
                
                Mode mode;
                if(!Enum.TryParse(argMode.Value, true, out mode)) {
                    Console.WriteLine($"{argMode.Value} is not one of \n{Enum.GetValues(typeof(Mode)).ToPrettyYaml()}");
                    return -1;
                }
                
                ProjectList feed;
                ProjectList complete;
                
                switch(mode) {
                    case Mode.Complete:
                        feed = await ProjectFeed.GetComplete();
                        var hourly = await ProjectFeed.GetHourly();
                        //merge
                        feed = feed.merge(hourly);
                        complete = feed;
                        
                        break;
                    case Mode.Hourly:
                        feed = await ProjectFeed.GetHourly();
                        
                        complete = await ProjectFeed.GetLocalComplete(OUTPUT);
                        //merge hourly into complete
                        complete = complete.merge(feed);
                        
                        
                        break;
                    default:
                        throw new NotImplementedException("Mode: {mode}");
                }
                
                // save complete.json.bz2
                Console.WriteLine($"recompressing complete.json");
                ProjectFeed.SaveLocalComplete(complete, OUTPUT);
                
                Console.WriteLine($"Getting all addon data at once from the API.. please wait...");
                var addons = await client.v2GetAddOnsAsync(feed.Data.Select(a => a.Id).ToArray());
                if(addons.Count() != feed.Data.Count) {
                    Console.WriteLine($"addons count: { addons.Count() } != feed.data count: { feed.Data.Count }");
                    return -1;
                }
                
                if (test) {
                    // addons = await client.v2GetAddOnsAsync(new int[] { 62242, 221641, 225861, 237275, 238856 });
                    
                    var test_addons = feed.Data.ToArray();
                    var diff_dir = Path.Combine(OUTPUT, "diff");
                    Directory.CreateDirectory(diff_dir);
                    for(int i = 0; i < addons.Count(); i++) {
                        var a = test_addons[i];
                        var b = addons[i];
                        a.PrimaryCategoryAvatarUrl = null;
                        a.PrimaryCategoryName = null;
                        var json_a = a.ToPrettyJson();
                        var json_b = b.ToPrettyJson();
                        //Console.WriteLine($"comparing [{a.Id} - {a.Name}]");
                        if(json_a != json_b) {
                            var file_a = Path.Combine(diff_dir, $"{a.Id}_from_list.json");
                            File.WriteAllText(file_a, json_a);
                            var file_b = Path.Combine(diff_dir, $"{b.Id}_original.json");
                            File.WriteAllText(file_b, json_b);
                            Console.WriteLine($"[{a.Id} - {a.Name}] differs:");
                            var ret = await ThreadUtil.RunProcessAsync("git", $"diff --histogram {file_a} {file_b}");
                            //return -1;
                        }
                    }
                    
                    return 0;
                }
                // await Task.WhenAll(addons.Select(a => process_addon(a)));
                
                var rnd = new Random();
                var randomData = addons.OrderBy(x => rnd.Next()).ToList();
                
                var batchSize = 100;
                var all = randomData//.Take(1000)
                    .Select((addon, index) => new { addon, index })
                    .GroupBy(e => (e.index / batchSize), e => e.addon);
                
                failedAddons = new HashSet<AddOn>();
                var timer = new Stopwatch();
                timer.Start();
                int k = 0;
                int k_all = all.Count();
                foreach (var batch in all) {
                    var tasks = Task.WhenAll(batch.Select(process_addon));
                    Console.WriteLine($"batch [{++k} / {k_all}]");
                    await tasks;
                    await Task.Delay(TimeSpan.FromSeconds(1));
                }
                
                while(failedAddons.Count() > 0) {
                    var tmp_set = new HashSet<AddOn>(failedAddons);
                    failedAddons = new HashSet<AddOn>();
                    Console.WriteLine($"retrying addons: \n{tmp_set.Select(a => a.Name).ToPrettyYaml()}");
                    await Task.WhenAll(tmp_set.Select(a => process_addon(a)));
                }
                
                timer.Stop();
                Console.WriteLine($"all projects were processed in '{ timer.Elapsed }'");
                 
                return 0;
             });
        }
        
        
        
        public async Task<AddOnServiceClient> Authenticate()
        {
            var loginClient = new ClientLoginServiceClient(ClientLoginServiceClient.EndpointConfiguration.BinaryHttpsClientLoginServiceEndpoint);
             
            var deserializer = new DeserializerBuilder()
                .IgnoreUnmatchedProperties()
                .WithNamingConvention(new CamelCaseNamingConvention())
                .Build();
            
            string path = Path.Combine(Constants.ConfigPath, "curse.yaml");
           
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
        
        public async Task process_addon(AddOn addon)
        {
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
            File.WriteAllText(Path.Combine(directory, $"{ addon.Id }/files.json"), files.ToPrettyJson());
            await Task.WhenAll(files.Select( f => process_file(addon, f, addonFilesDirectory) ));
            // Console.WriteLine($"[{addon.Name}] finished");
        }

        public async Task process_file(AddOn addon, AddOnFile file, string addonDirectory)
        {
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
        
        enum Mode {
            Complete,
            Hourly
        }
    }
}