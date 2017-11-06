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
    public class CompleteTask : IScheduledTask {
        Config config = Config.instance.Value;
        public string Schedule => config.task.complete.Schedule;

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            Console.WriteLine ($"Task:Complete started");

            await ProjectFeed.GetComplete ();

            Console.WriteLine ($"Task:Complete finished");
        }
    }
}