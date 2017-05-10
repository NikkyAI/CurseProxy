using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Alpacka.Meta.AddOnService;
using Microsoft.Extensions.CommandLineUtils;


namespace Alpacka.Meta
{
    public class CommandGet : CommandLineApplication
    {
        public CommandGet()
        {
            Name = "get";
            Description = "grabs a specific file";
            
            var optProject = Option("-p | --project",
                "Project IDs, can be specified multiple times", CommandOptionType.MultipleValue);
            
            var optFiles = Option("--file",
                "project and file id in the format 'project:file', can be specified multiple times", CommandOptionType.MultipleValue);
            
            var optOut = Option("-o | --out",
                "Output Directory", CommandOptionType.SingleValue);
            
            var optConfig = Option("-c | --config",
                "Config Directory", CommandOptionType.SingleValue);
            
            var optFilter = Option("--filter",
                "None or Default filter", CommandOptionType.SingleValue);
            
            var optVerbose = Option("-v | --verbose",
                "save stacktraces and more info", CommandOptionType.NoValue);
            
            var optPretty = Option("-p | --pretty",
                "save json files with indentation", CommandOptionType.NoValue);
            
            var optWithChangelogs = Option("--withchangelogs",
                "Include changelog files", CommandOptionType.NoValue);
            
            var optWithDescriptions = Option("--withdescriptions",
                "Include description files", CommandOptionType.NoValue);
            
            HelpOption("-? | -h | --help");
            
             OnExecute(async () => {
                var downloadUtil = new DownloadUtil(optOut.Value(), optConfig.Value());
                var client = await DownloadUtil.LazyAddonClient.Value;
                downloadUtil.verbose = optVerbose.HasValue();
                downloadUtil.pretty = optPretty.HasValue();
                downloadUtil.changelogs = optWithChangelogs.HasValue();
                downloadUtil.descriptions = optWithDescriptions.HasValue();
                
                Filter filter = FilterExtensions.parse(optFilter.Value());
                downloadUtil.filter = filter;
                Console.WriteLine($"using filter: {filter}");
                
                if (optProject.Values.Count() == 0 && optFiles.Values.Count() == 0) {
                    Console.WriteLine("no projects given");
                    return -1;
                }
                
                ProjectList feed = new ProjectList { Data = new List<AddOn>() };
                
                if (optProject.Values.Count() != 0) {
                    int[] projectIDs = new int[optProject.Values.Count()];
                    for (int i = 0; i < optProject.Values.Count(); i++) {
                        if(!int.TryParse(optProject.Values[i], out projectIDs[i])) {
                            Console.WriteLine($"cannot parse argument: { optProject.Values[i] }");
                            return -1;
                        }
                    }
                    
                    var addons = await client.v2GetAddOnsAsync(projectIDs);
                    await DownloadUtil.processAll(addons, downloadUtil.processAddon);
                    
                    //merge addons into feed
                    feed = feed.merge( new ProjectList { Data = new List<AddOn>(addons) } );
                }
                
                if (optFiles.Values.Count() != 0) {
                    var tasks = new List<Task>();
                    var keys = new List<AddOnFileKey>();
                    foreach (var value in optFiles.Values) {
                        var ids = value.Split("-".ToCharArray(), count: 2);
                        int project;
                        if(!int.TryParse(ids[0], out project)) {
                            Console.WriteLine($"cannot parse project in {value}");
                            return -1;
                        }
                        int file;
                        if(!int.TryParse(ids[1], out file)) {
                            Console.WriteLine($"cannot parse file in {value}");
                            return -1;
                        }
                        keys.Add(new AddOnFileKey{AddOnID = project, FileID = file});
                    }
                    var bundles = await downloadUtil.getFiles(keys.ToArray());
                    
                    if((bundles?.Length ?? -1) != keys.Count) {
                        return -1;
                    }
                    
                    //merge addons into feed
                    feed = feed.merge( new ProjectList{ Data = bundles.Select(b => b.addon).ToList() } );
                }
                
                var complete = await ProjectFeed.GetCompleteLocal(downloadUtil.OUTPUT);
                complete = complete.merge(feed);
                
                //TODO: FIXME: seriously.. less code duplication please
                
                Console.WriteLine($"Getting all addon data at once from the API.. please wait...");
                var _addons = await client.v2GetAddOnsAsync(feed.Data.Select(a => a.Id).ToArray());
                if(_addons.Length != feed.Data.Count()) {
                    Console.WriteLine($"addons count: { _addons.Length } != feed.data count: { feed.Data.Count() }");
                    return -1;
                }
                
                //process mods
                Console.WriteLine($"Filtering mods, please wait... old count: {_addons.Length}");
                var allMods = _addons.Where(a => a.PackageType == PackageTypes.Mod).ToArray();
                Console.WriteLine($"Finished filtering mods, new count: {allMods.Length}");
                
                if(allMods.Length > 0) {
                    Console.WriteLine($"Filtering complete mods, please wait... old count: {complete.Data.Count()}");
                    var completeMods = new ProjectList { 
                        Data = complete.Data.Where(a => a.PackageType == PackageTypes.Mod).ToList(),
                        Timestamp = complete.Timestamp
                    };
                    Console.WriteLine($"Finished filtering complete mods, new count: {completeMods.Data.Count()}");
                    ProjectFeed.SaveLocal(completeMods, filter, downloadUtil.OUTPUT, "mods");
                }
                
                //process modpacks
                Console.WriteLine($"Filtering modpacks, please wait... old count: {_addons.Length}");
                var allModpacks = _addons.Where(a => a.PackageType == PackageTypes.ModPack).ToArray();
                Console.WriteLine($"Finished filtering modpacks, new count: {allModpacks.Length}");
                
                if(allModpacks.Length > 0) {
                    Console.WriteLine($"Filtering complete modpacks, please wait... old count: {complete.Data.Count()}");
                    var completeModpacks = new ProjectList { 
                        Data = complete.Data.Where(a => a.PackageType == PackageTypes.ModPack).ToList(),
                        Timestamp = complete.Timestamp
                    };
                    Console.WriteLine($"Finished filtering complete modpacks, new count: {completeModpacks.Data.Count()}");
                    ProjectFeed.SaveLocal(completeModpacks, filter, downloadUtil.OUTPUT, "modpacks");
                }
                
                return 0;
             });
        }
    }
}