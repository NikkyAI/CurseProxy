using System;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;
using Microsoft.Extensions.Logging;

namespace Cursemeta.Tasks {
    public class CompleteTask : IScheduledTask {
        private readonly ILogger logger;
        private readonly Feed feed;

        private readonly CompleteConfig config = Config.instance.Value.task.complete;
        public string Schedule => config.Schedule;
        private int RunCount = 0;

        public CompleteTask (ILogger<CompleteTask> _logger, Feed _feed) {
            logger = _logger;
            feed = _feed;
        }

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0 && !config.OnStartup) {
                logger.LogInformation ($"Skipped on startup");
            }
            logger.LogInformation ("Run {RunCount} started", RunCount);

            await feed.GetComplete ();

            logger.LogInformation ("Run {RunCount} finished", RunCount);
        }
    }
}