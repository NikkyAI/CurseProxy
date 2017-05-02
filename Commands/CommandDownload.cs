using System;
using System.IO;
using System.Linq;
using System.Diagnostics;
using System.Threading.Tasks;
using Microsoft.Extensions.CommandLineUtils;
using Newtonsoft.Json;
using System.Collections.Generic;
using Alpacka.Meta.AddOnService;

namespace Alpacka.Meta
{
    public class CommandDownload : CommandLineApplication
    {
        public CommandDownload()
        {
            Name = "download";
            Description = "Downloads curse meta information";
            
            var argMode = Argument("[mode]",
                $"'{ String.Join(" | ", Enum.GetNames(typeof(Mode))) }'");
            
            var optOut = Option("-o | --out",
                "Output Directory", CommandOptionType.SingleValue);
            
            var optVerbose = Option("-v | --verbose",
                "save stacktraces and more info", CommandOptionType.NoValue);
            
            var optPretty = Option("-v | --pretty",
                "save json files with indentation", CommandOptionType.NoValue);
            
            var optConfig = Option("-c | --config",
                "Config Directory", CommandOptionType.SingleValue);
            
            var optFilter = Option("--filter",
                "None or Default filter", CommandOptionType.SingleValue);
            
            var optWithChangelogs = Option("--withchangelogs",
                "Include changelog files", CommandOptionType.NoValue);
            
            var optWithDescriptions = Option("--withdescriptions",
                "Include description files", CommandOptionType.NoValue);
            
            var optDisableMods = Option("--nomods",
                "Do not download mods", CommandOptionType.NoValue);
            
            var optDisableModPacks = Option("--nomodpacks",
                "Do not download modpacks", CommandOptionType.NoValue);
            
            HelpOption("-? | -h | --help");
            
            OnExecute(async () => {
                if(optDisableMods.HasValue() && optDisableModPacks.HasValue()) {
                    Console.WriteLine("all download options are disabled, not executing download");
                    return -1;
                }
                var client = await DownloadUtil.LazyAddonClient.Value;
                
                var downloadUtil = new DownloadUtil(optOut.Value());
                
                downloadUtil.verbose = optVerbose.HasValue();
                downloadUtil.pretty = optPretty.HasValue();
                downloadUtil.changelogs = optWithChangelogs.HasValue();
                downloadUtil.descriptions = optWithDescriptions.HasValue();
                
                DownloadUtil.CONFIG = optConfig.Value();
                
                Mode mode;
                if (!Enum.TryParse(argMode.Value, true, out mode)) {
                    Console.WriteLine($"{argMode.Value} is not one of \n{Enum.GetNames(typeof(Mode)).ToPrettyYaml()}");
                    return -1;
                }
                
                Filter filter = FilterExtensions.parse(optFilter.Value());
                downloadUtil.filter = filter;
                Console.WriteLine($"using filter: {filter}");
                
                ProjectList feed;
                ProjectList complete;
                
                switch(mode) {
                    case Mode.Complete:
                        feed = await ProjectFeed.GetComplete();
                        var hourly = await ProjectFeed.GetHourly();
                        //merge
                        feed = feed.merge(hourly);
                        complete = feed.clone();
                        
                        break;
                    case Mode.Hourly:
                        feed = await ProjectFeed.GetHourly();
                        
                        complete = await ProjectFeed.GetCompleteLocal(downloadUtil.OUTPUT);
                        //merge hourly into complete
                        complete = complete.merge(feed);
                        
                        break;
                    case Mode.Locally:
                        //construct complete modlist from files
                        
                        var timer = new Stopwatch();
                        
                        timer.Start();
                        
                        var allAddons = new List<AddOn>();
                        var outputDirectory = new DirectoryInfo(downloadUtil.ADDONPATH);
                        foreach (var directoryinfo in outputDirectory.EnumerateDirectories().OrderBy(f => f.Name)) {
                            Console.WriteLine($"{directoryinfo.Name}");
                            var indexFile = Path.Combine(directoryinfo.FullName, "index.json");
                            if (File.Exists(indexFile)) {
                                var addon = JsonConvert.DeserializeObject<AddOn>(File.ReadAllText(indexFile), DownloadUtil.serializerSettings);
                                allAddons.Add(addon);
                            } else {
                                Console.WriteLine($"file {indexFile} does nto exist");
                            }
                        }
                        
                        timer.Stop();
                        Console.WriteLine($"local addons processed in '{ timer.Elapsed }'");
                        
                        feed = new ProjectList {
                            Data = allAddons,
                            Timestamp = 0
                        };
                        complete = feed.clone();
                        break;
                    default:
                        throw new NotImplementedException("Mode: {mode}");
                }
                
                Console.WriteLine($"sorting complete.json");
                complete.Data = complete.Data
                    .OrderBy(a => a.Id).ToList();
                
                // save complete.json.bz2
                Console.WriteLine($"recompressing complete.json");
                ProjectFeed.SaveLocal(complete, filter, downloadUtil.OUTPUT, "complete");
                
                Console.WriteLine($"Getting all addon data at once from the API.. please wait...");
                var addons = await client.v2GetAddOnsAsync(feed.Data.Select(a => a.Id).ToArray());
                if(addons.Length != feed.Data.Count()) {
                    Console.WriteLine($"addons count: { addons.Length } != feed.data count: { feed.Data.Count() }");
                    return -1;
                }
                
                if(!optDisableMods.HasValue()) {
                    //process mods
                    Console.WriteLine($"Filtering mods, please wait... old count: {addons.Length}");
                    var allMods = addons.Where(a => a.PackageType == PackageTypes.Mod).ToArray();
                    Console.WriteLine($"Finished filtering mods, new count: {allMods.Length}");
                    
                    //in case of hourly this may be empty, there was no change then
                    if(allMods.Length > 0) {
                        Console.WriteLine($"Filtering complete mods, please wait... old count: {complete.Data.Count()}");
                        var completeMods = new ProjectList { 
                            Data = complete.Data.Where(a => a.PackageType == PackageTypes.Mod).ToList(),
                            Timestamp = complete.Timestamp
                        };
                        Console.WriteLine($"Finished filtering complete mods, new count: {completeMods.Data.Count()}");
                        ProjectFeed.SaveLocal(completeMods, filter, downloadUtil.OUTPUT, "mods");
                    }
                    
                    await DownloadUtil.processAll(allMods, downloadUtil.processAddon, "mods");
                }
                
                if(!optDisableModPacks.HasValue()) {
                    //process modpacks
                    Console.WriteLine($"Filtering modpacks, please wait... old count: {addons.Length}");
                    var allModpacks = addons.Where(a => a.PackageType == PackageTypes.ModPack).ToArray();
                    Console.WriteLine($"Finished filtering modpacks, new count: {allModpacks.Length}");
                    
                    //in case of hourly this may be empty, there was no change then
                    if(allModpacks.Length > 0) {
                        Console.WriteLine($"Filtering complete modpacks, please wait... old count: {complete.Data.Count()}");
                        var completeModpacks = new ProjectList { 
                            Data = complete.Data.Where(a => a.PackageType == PackageTypes.ModPack).ToList(),
                            Timestamp = complete.Timestamp
                        };
                        Console.WriteLine($"Finished filtering complete modpacks, new count: {completeModpacks.Data.Count()}");
                        ProjectFeed.SaveLocal(completeModpacks, filter, downloadUtil.OUTPUT, "modpacks");
                    }
                    
                    await DownloadUtil.processAll(allModpacks, downloadUtil.processAddon, "modpacks");
                }
                
                return 0;
             });
        }
        
        public enum Mode {
            Complete,
            Hourly,
            Locally
        }
        
    }
}