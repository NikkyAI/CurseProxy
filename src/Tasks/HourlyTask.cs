using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;

namespace Cursemeta.Tasks {
    public class HourlyTask : IScheduledTask {
        HourlyConfig config = Config.instance.Value.task.hourly;
        public string Schedule => config.Schedule;
        private int RunCount = 0;

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0 && !config.OnStartup) {
                Console.WriteLine ($"Task:Hourly skipped on startup");
                return;
            }
            Console.WriteLine ($"Task:Hourly {RunCount} started");

            await ProjectFeed.GetHourly ();

            Console.WriteLine ($"Task:Hourly {RunCount} finished");
        }
    }
}