using System;
using System.Buffers;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Diagnostics;
using Cursemeta.Scheduling;
using Cursemeta.Tasks;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Formatters;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Serialization;
using Serilog;

namespace Cursemeta {

    public class Startup {
        public Startup (IConfiguration configuration) {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices (IServiceCollection services) {
            services.AddMvc (options => {
                options.RequireHttpsPermanent = true; // does not affect api requests
                options.RespectBrowserAcceptHeader = true; // false by default
                //options.OutputFormatters.RemoveType<HttpNoContentOutputFormatter>();
            }).AddJsonOptions (options => {
                options.SerializerSettings.Formatting = Formatting.Indented;
                options.SerializerSettings.NullValueHandling = NullValueHandling.Ignore;
                options.SerializerSettings.ContractResolver = new CamelCasePropertyNamesContractResolver ();
                options.SerializerSettings.Converters.Add (new StringEnumConverter { CamelCaseText = true });
            });

            // Add scheduled tasks & scheduler
            Config config = Config.instance.Value;

            if (config.task.complete.Enabled)
                services.AddSingleton<IScheduledTask, CompleteTask> ();
            if (config.task.hourly.Enabled)
                services.AddSingleton<IScheduledTask, HourlyTask> ();
            if (config.task.sync.Enabled)
                services.AddSingleton<IScheduledTask, SyncTask> ();
            if (config.task.test.Enabled)
                services.AddSingleton<IScheduledTask, TestTask> ();

            services.AddScheduler ((sender, args) => {
                Console.Error.Write (args.Exception.Message);
                args.SetObserved ();
            });

            var logger = Log.ForContext<Startup>();
            logger.Information ("registered tasks");
            
            services.AddSingleton<Cache> ();
            services.AddSingleton<Client> ();
            services.AddSingleton<Feed> ();
            services.AddSingleton<Update> ();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure (IApplicationBuilder app, IHostingEnvironment env) {
            if (env.IsDevelopment ()) {
                app.UseDeveloperExceptionPage ();
            }

            app.UseMiddleware<SerilogMiddleware> ();
            app.UseWhen (x => (x.Request.Path.StartsWithSegments ("/api/update", StringComparison.OrdinalIgnoreCase)),
                builder => {
                    builder.UseMiddleware<BasicAuthenticationMiddleWare> ();
                });
            app.UseMvc ();
        }
    }
}