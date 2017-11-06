using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;
using Microsoft.Extensions.Caching.Memory;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Cursemeta.Tasks {
    public class HourlyTask : IScheduledTask {
        Config config = Config.instance.Value;
        public string Schedule => config.task.hourly.Schedule;
        private int RunCount = 0;

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0) {
                Console.WriteLine ($"Task:Hourly skipped on startup");
                return;
            }
            Console.WriteLine ($"Task:Hourly started");

            await ProjectFeed.GetHourly ();

            Console.WriteLine ($"Task:Hourly finished");
        }
    }
}