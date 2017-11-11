using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;
using Microsoft.Extensions.Logging;

namespace Cursemeta.Tasks {
    public class HourlyTask : IScheduledTask {
        private readonly ILogger logger;
        private readonly Feed feed;

        private readonly HourlyConfig config = Config.instance.Value.task.hourly;
        public string Schedule => config.Schedule;
        private int RunCount = 0;

        public HourlyTask (ILogger<HourlyTask> _logger, Feed _feed) {
            logger = _logger;
            feed = _feed;
        }

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            if (RunCount++ == 0 && !config.OnStartup) {
                logger.LogInformation ($"Skipped on startup");
                return;
            }
            logger.LogInformation ("Run {RunCount} started", RunCount);

            await feed.GetHourly ();

            logger.LogInformation ("Run {RunCount} finished", RunCount);
        }
    }
}