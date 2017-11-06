using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.Scheduling;

namespace Cursemeta.Tasks {
    public class TestTask : IScheduledTask {
        TestConfig config = Config.instance.Value.task.test;
        public string Schedule => config.Schedule;
        private int RunCount = 0;
        HttpClient httpClient = new HttpClient ();

        public async Task ExecuteAsync (CancellationToken cancellationToken) {
            RunCount++;

            var randomCommitMessage = await httpClient.GetStringAsync ("http://whatthecommit.com/index.txt");

            Console.WriteLine ($"Task:Test {RunCount}: \n{randomCommitMessage.Replace("\n", "")}");
        }
    }
}