using System;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;

namespace Cursemeta.Tasks {
    public class CompleteTask : IScheduledTask {
        CompleteConfig config = Config.instance.Value.task.complete;
        public string Schedule => config.Schedule;
        private int RunCount = 0;

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0 && !config.OnStartup) {
                Console.WriteLine ($"Task:Complete skipped on startup");
            }
            Console.WriteLine ($"Task:Complete {RunCount} started");

            await ProjectFeed.GetComplete ();

            Console.WriteLine ($"Task:Complete {RunCount} finished");
        }
    }
}