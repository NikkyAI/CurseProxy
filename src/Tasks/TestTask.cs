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
    public class TestTask : IScheduledTask {
        Config config = Config.instance.Value;
        public string Schedule => config.task.test.Schedule;

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            var httpClient = new HttpClient ();

            var randomCommitMessage = await httpClient.GetStringAsync ("http://whatthecommit.com/index.txt");

            Console.WriteLine ($"'{randomCommitMessage.Replace("\n", "")}'");
        }
    }
}