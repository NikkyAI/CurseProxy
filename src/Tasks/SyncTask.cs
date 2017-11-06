using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;

namespace Cursemeta.Tasks {
    public class SyncTask : IScheduledTask {
        SyncConfig config = Config.instance.Value.task.sync;
        public string Schedule => config.Schedule;
        private int RunCount = 0;

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0 && config.SkipStartup) {
                Console.WriteLine ($"Task:Sync skipped on startup");
                return;
            }
            Console.WriteLine ($"Task:Sync {RunCount} started");

            await Update.Sync (config.BatchSize, config.Addons, config.Descriptions, config.Files, config.Changelogs);

            Console.WriteLine ($"Task:Sync {RunCount} finished");
        }
    }
}