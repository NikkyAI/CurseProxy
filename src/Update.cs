using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Cursemeta.AddOnService;
using Cursemeta;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace Cursemeta {
    public class Update {
        async public static Task Sync (int batchSize = 500, bool addons = true, bool descriptions = false, bool files = true, bool changelogs = false, bool gc = false) {
            try {
                Console.WriteLine (batchSize);
                if (gc) {
                    GC.Collect ();
                    GC.WaitForPendingFinalizers ();
                }
                var client = CacheClient.LazyClient.Value;
                var cache = Cache.LazyCache.Value;
                var ids = cache.GetIDs ();
                Random r = new Random ();
                var batches = ids.Keys.OrderBy (x => r.Next ()).ToList ().split (batchSize);
                var totalTimer = new Stopwatch ();
                totalTimer.Start ();
                int b = 0;
                int b_all = batches.Count ();
                TimeSpan totalSum = TimeSpan.FromSeconds (0);
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
                    //Console.WriteLine ($"{string.Join(", ", batch)}");
                    await tasks;
                    timer.Stop ();
                    processed += batch.Count;
                    Console.WriteLine ($"batch [{++b} / {b_all}] [{processed}/{all}]");
                    timer.Stop ();
                    var batchElapsed = timer.Elapsed;
                    timer.Restart ();
                    cache.Save ();
                    timer.Stop ();
                    var saveElapsed = timer.Elapsed;
                    timer.Restart ();
                    totalSum += batchElapsed + saveElapsed;
                    var average = totalSum / b;
                    var predicted = average * (b_all - b);
                    var currentPrediction = (batchElapsed + saveElapsed) * (b_all - b);
                    Console.WriteLine ($"batch: {batchElapsed} save: {saveElapsed} total:{totalSum} average: {average}  predicted: average {predicted} currrent {currentPrediction}");
                    if (gc) {
                        GC.Collect ();
                        GC.WaitForPendingFinalizers ();
                    }
                    var memory = GC.GetTotalMemory (true);
                    Console.WriteLine ($"total memory: {memory / 1000.0} MB ");
                    //await Task.Delay(TimeSpan.FromSeconds(0.1)); //testing if this causes problems or not
                }
                timer.Stop ();

                //TODO: add retrying

                totalTimer.Stop ();
                if (gc) {

                    GC.Collect ();
                    GC.WaitForPendingFinalizers ();
                }

                Console.WriteLine ($"all targets were processed in '{ totalTimer.Elapsed }'");
            } catch (Exception e) {
                Console.Error.WriteLine(e);
            }
        }

    }
}