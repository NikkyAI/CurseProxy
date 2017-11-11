using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Serilog;
using Serilog.Configuration;
using Serilog.Core;
using Serilog.Enrichers;
// using Serilog.Exceptions;
using Serilog.Formatting.Display;


namespace Cursemeta {

    public class Program {
        public static int Main (string[] args) {
            var configuration = new ConfigurationBuilder ()
                .SetBasePath (Directory.GetCurrentDirectory ())
                .AddJsonFile ("appsettings.json", optional : false, reloadOnChange : true)
                .AddJsonFile ($"appsettings.{Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") ?? "Production"}.json", optional : true)
                .Build ();

            Log.Logger = new LoggerConfiguration ()
                .ReadFrom.Configuration (configuration)
                .Enrich.FromLogContext ()
                // .Enrich.WithExceptionDetails ()
                .Enrich.WithThreadId ()
                // .WriteTo.Console(new MessageTemplateTextFormatter("{Timestamp:yyyy-MM-dd HH:mm:ss} [{Level}] {Message}{NewLine}{Exception}",null))
                //.WriteTo.RollingFile ("log-{Date}.txt")
                .CreateLogger ();

            var logger = Log.ForContext<Program> ();
            try {
                logger.Information ("Starting web host");
                BuildWebHost (args, configuration).Run ();
                return 0;
            } catch (Exception ex) {
                logger.Fatal (ex, "Host terminated unexpectedly");
                return 1;
            } finally {
                Log.CloseAndFlush ();
            }
        }

        public static IWebHost BuildWebHost (string[] args, IConfiguration configuration) =>
            WebHost.CreateDefaultBuilder (args)
            .UseStartup<Startup> ()
            .UseConfiguration (configuration)
            .UseSerilog ()
            .Build ();
    }
}