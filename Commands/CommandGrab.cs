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
                var downloadUtil = new DownloadUtil(optOut.Value(), optConfig.Value());
                downloadUtil.verbose = optVerbose.HasValue();
                
                int addonId, fileId;
                if(!int.TryParse(argAddon.Value, out addonId) || !int.TryParse(argFile.Value, out fileId)) {
                    Console.WriteLine("cannot parse arguments");
                    return -1;
                }
                downloadUtil.reset_failed();
                await downloadUtil.process_addon(addonId, fileId);
                
                if(downloadUtil.failedAddons.Count() > 0) {
                    var tmp_set = downloadUtil.reset_failed();
                    Console.WriteLine($"failed addons: \n{tmp_set.Select(a => a.Name).ToPrettyYaml()}");
                }
                return 0;
             });
        }
    }
}