using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;
using Microsoft.Extensions.Logging;

namespace Cursemeta.Tasks {
    public class SyncTask : IScheduledTask {
        private readonly ILogger logger;
        private readonly Update update;

        SyncConfig config = Config.instance.Value.task.sync;
        public string Schedule => config.Schedule;

        private int RunCount = 0;

        public SyncTask (ILogger<CompleteTask> _logger, Update _update) {
            logger = _logger;
            update = _update;
        }

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0 && !config.OnStartup) {
                logger.LogInformation ("Task:Sync skipped on startup");
                return;
            }
            logger.LogInformation ("Run {RunCount} started", RunCount);

            await update.Sync (config.BatchSize, config.Addons, config.Descriptions, config.Files, config.Changelogs);

            logger.LogInformation ("Run {RunCount} finished", RunCount);
        }
    }
}