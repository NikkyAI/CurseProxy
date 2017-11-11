using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;
using Microsoft.Extensions.Logging;

namespace Cursemeta.Tasks {
    public class TestTask : IScheduledTask {
        private readonly ILogger logger;

        private readonly TestConfig config = Config.instance.Value.task.test;
        public string Schedule => config.Schedule;
        private int RunCount = 0;
        HttpClient httpClient = new HttpClient ();

        public TestTask (ILogger<CompleteTask> _logger, Config _config) {
            logger = _logger;
        }

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            RunCount++;

            var randomCommitMessage = await httpClient.GetStringAsync ("http://whatthecommit.com/index.txt");

            logger.LogInformation ("Run {RunCount}: \n{randomCommitMessage}", RunCount, randomCommitMessage);
        }
    }
}