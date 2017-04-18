using System;
using System.Linq;
using Microsoft.Extensions.CommandLineUtils;


namespace Alpacka.Meta
{
    public class CommandGrab : CommandLineApplication
    {
        public CommandGrab()
        {
            Name = "grab";
            Description = "grabs a specific file";
            
            var argAddon = Argument("[addon]",
                "addon id");
            
            var argFile = Argument("[file]",
                "file id");
            
            var optOut = Option("-o | --out",
                "Output Directory", CommandOptionType.SingleValue);
            
            var optConfig = Option("-c | --config",
                "Config Directory", CommandOptionType.SingleValue);
            
            var optVerbose = Option("-v | --verbose",
                "save stacktraces and more info", CommandOptionType.NoValue);
                
            HelpOption("-? | -h | --help");
            
             OnExecute(async () => {
                var downloadUtil = new DownloadUtil(optOut.Value());
                downloadUtil.verbose = optVerbose.HasValue();
                DownloadUtil.CONFIG = optConfig.Value();
                
                int addonId, fileId;
                if(!int.TryParse(argAddon.Value, out addonId) || !int.TryParse(argFile.Value, out fileId)) {
                    Console.WriteLine("cannot parse arguments");
                    return -1;
                }
                downloadUtil.reset_failed();
                var ret = await downloadUtil.process_addon(addonId, fileId);
                
                if(downloadUtil.failedAddons.Count() > 0) {
                    var tmp_set = downloadUtil.reset_failed();
                    Console.WriteLine($"failed addons: \n{tmp_set.Select(a => a.Name).ToPrettyYaml()}");
                }
                return ret;
             });
        }
    }
}