using System;
using System.IO;
using System.Linq;
using System.Diagnostics;
using System.Threading.Tasks;
using Microsoft.Extensions.CommandLineUtils;

namespace Alpacka.Meta
{
    public class CommandDownload : CommandLineApplication
    {
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
            
            var optConfig = Option("-c | --config",
                "Config Directory", CommandOptionType.SingleValue);
            
            var optTest = Option("-t | --test",
                "Test flag", CommandOptionType.NoValue);
                
            HelpOption("-? | -h | --help");
            
            OnExecute(async () => {
                var client = await DownloadUtil.LazyAddonClient.Value;
                
                var downloadUtil = new DownloadUtil(optOut.Value());
                
                var test = optTest.HasValue();
                downloadUtil.verbose = optVerbose.HasValue();
                
                DownloadUtil.CONFIG = optConfig.Value();
                
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
                        
                        complete = await ProjectFeed.GetLocalComplete(downloadUtil.OUTPUT);
                        //merge hourly into complete
                        complete = complete.merge(feed);
                        
                        
                        break;
                    default:
                        throw new NotImplementedException("Mode: {mode}");
                }
                
                // save complete.json.bz2
                Console.WriteLine($"recompressing complete.json");
                ProjectFeed.SaveLocalComplete(complete, downloadUtil.OUTPUT);
                
                Console.WriteLine($"Getting all addon data at once from the API.. please wait...");
                var addons = await client.v2GetAddOnsAsync(feed.Data.Select(a => a.Id).ToArray());
                if(addons.Count() != feed.Data.Count) {
                    Console.WriteLine($"addons count: { addons.Count() } != feed.data count: { feed.Data.Count }");
                    return -1;
                }
                
                if (test) {
                    // addons = await client.v2GetAddOnsAsync(new int[] { 62242, 221641, 225861, 237275, 238856 });
                    
                    var test_addons = feed.Data.ToArray();
                    var diff_dir = Path.Combine(downloadUtil.OUTPUT, "diff");
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
                
                downloadUtil.reset_failed();
                var timer = new Stopwatch();
                timer.Start();
                int k = 0;
                int k_all = all.Count();
                foreach (var batch in all) {
                    var tasks = Task.WhenAll(batch.Select(downloadUtil.process_addon));
                    Console.WriteLine($"batch [{++k} / {k_all}]");
                    await tasks;
                    await Task.Delay(TimeSpan.FromSeconds(1));
                }
                
                while(downloadUtil.failedAddons.Count() > 0) {
                    var tmp_set = downloadUtil.reset_failed();
                    Console.WriteLine($"retrying addons: \n{tmp_set.Select(a => a.Name).ToPrettyYaml()}");
                    await Task.WhenAll(tmp_set.Select(downloadUtil.process_addon));
                }
                
                timer.Stop();
                Console.WriteLine($"all projects were processed in '{ timer.Elapsed }'");
                 
                return 0;
             });
        }
        
        
        
        
        
        
        
        enum Mode {
            Complete,
            Hourly
        }
    }
}