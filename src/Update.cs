using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Threading;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.AddOnService;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace Cursemeta {
    public class Update {
        private static Task syncTask = null;
        private static int SyncRunCounter = 0;

        public static Task Sync (int batchSize = 500, bool addons = true, bool descriptions = false, bool files = true, bool changelogs = false, bool gc = false) {
            if (syncTask == null || syncTask.IsCompleted.Equals (true)) {
                syncTask = _Sync (batchSize, addons, descriptions = false, files, changelogs, gc);
                return syncTask;
            }
            return syncTask;
        }

        async private static Task _Sync (int batchSize = 500, bool addons = true, bool descriptions = false, bool files = true, bool changelogs = false, bool gc = false) {
            try {
                Console.WriteLine ($"run {SyncRunCounter++}");
                Console.WriteLine (batchSize);
                if (gc) {
                    GC.Collect ();
                    GC.WaitForPendingFinalizers ();
                }
                var client = CacheClient.LazyClient.Value;
                var cache = Cache.LazyCache.Value;
                var ids = cache.GetIDs ();
                Random r = new Random ();
                var batches = ids.Keys.OrderBy (x => r.Next ()).Split (batchSize);
                var totalTimer = new Stopwatch ();
                totalTimer.Start ();
                int b = 0;
                int b_all = batches.Count ();
                TimeSpan totalElapsed = TimeSpan.FromSeconds (0);
                int processed = 0;
                var all = ids.Count;

                var timer = new Stopwatch ();
                if (addons) {
                    timer.Start ();
                    await client.v2GetAddOnsAsync (ids.Keys.ToArray (), false, false);
                    timer.Stop ();
                    var addonElapsed = timer.Elapsed;
                    Console.WriteLine ($"{ids.Keys.Count} addons processed in {addonElapsed}");
                    timer.Reset ();
                }
                timer.Start ();
                foreach (var batch in batches) {
                    var tasks = Task.WhenAll (batch.Select (
                        async addonID => {
                            if (descriptions) await client.v2GetAddOnDescriptionAsync (addonID, false, false);

                            if (files) await client.GetAllFilesForAddOnAsync (addonID, false, false);

                            if (changelogs) {
                                foreach (var fileID in ids[addonID]) {
                                    await client.v2GetChangeLogAsync (addonID, fileID, false, false);
                                }
                            }
                        }
                    ));
                    await tasks;
                    processed += batch.Count ();
                    ++b;
                    Console.WriteLine ($"batch [{b} / {b_all}] [{processed}/{all}]");
                    cache.Save ();
                    timer.Stop ();
                    var batchElapsed = timer.Elapsed;
                    totalElapsed += batchElapsed;
                    timer.Restart ();
                    var average = totalElapsed / b;
                    var averagePrediction = average * (b_all - b);
                    var currentPrediction = (batchElapsed) * (b_all - b);
                    Console.WriteLine (new {
                        elapsed = batchElapsed,
                            average = average, total = totalElapsed, prediction = new { average = averagePrediction, current = currentPrediction }
                    }.ToPrettyJson (false));
                    if (gc) {
                        GC.Collect ();
                        GC.WaitForPendingFinalizers ();
                    }
                    //var memory = GC.GetTotalMemory (true);
                    // Console.WriteLine ($"total memory: {memory / 1000.0} MB ");
                    //await Task.Delay(TimeSpan.FromSeconds(0.1)); //testing if this causes problems or not
                }
                timer.Stop ();

                totalTimer.Stop ();
                if (gc) {

                    GC.Collect ();
                    GC.WaitForPendingFinalizers ();
                }

                Console.WriteLine ($"all targets were processed in '{ totalTimer.Elapsed }'");
            } catch (Exception e) {
                Console.Error.WriteLine (e);
            }
        }
    }
}