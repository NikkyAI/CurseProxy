using System;
using System.Reflection;
using Microsoft.Extensions.CommandLineUtils;

namespace Alpacka.Meta
{
    class Program : CommandLineApplication
    {
        public static int Main(string[] args) => new Program().Run(args);

        public static readonly string Version = typeof(Program).GetTypeInfo().Assembly
            .GetCustomAttribute<AssemblyInformationalVersionAttribute>().InformationalVersion;

        public Program()
        {
            Name = "alpacka-meta";
            FullName = "Curse Metadata utility for Alpacka";
            Description = "Generates cursed JSONs";
            
            Commands.Add(new CommandDownload());
            
            VersionOption("-v | --version", Version);
            HelpOption("-? | -h | --help");

            OnExecute(() => {
                ShowHelp();
                return 0;
            });
        }
        
        public int Run(string[] args)
        {
            try { return Execute(args); }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                Console.WriteLine(ex.StackTrace);
                Console.WriteLine(ex.Source);
                return 0;
            }
        }
    }
}