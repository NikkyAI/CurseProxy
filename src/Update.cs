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

        private readonly ILogger logger;
        private readonly Client client;
        private readonly Cache cache;

        public Update (ILogger<Update> _logger, Client _client, Cache _cache) {
            logger = _logger;
            client = _client;
            cache = _cache;
        }

        private Task syncTask = null;
        private int SyncRunCounter = 0;

        public Task Sync (int batchSize = 500, bool addons = true, bool descriptions = false, bool files = true, bool changelogs = false, bool gc = false) {
            if (syncTask == null || syncTask.IsCompleted.Equals (true)) {
                syncTask = _Sync (batchSize, addons, descriptions = false, files, changelogs, gc);
                return syncTask;
            }
            return syncTask;
        }

        async private Task _Sync (int batchSize = 500, bool addons = true, bool descriptions = false, bool files = true, bool changelogs = false, bool gc = false) {
            try {
                SyncRunCounter++;
                logger.LogInformation ("run {SyncRunCounter}", SyncRunCounter);
                logger.LogTrace ("batch size: {batchSize}", batchSize);
                if (gc) {
                    GC.Collect ();
                    GC.WaitForPendingFinalizers ();
                }

                var ids = cache.GetIDs ();
                Random r = new Random ();
                var batches = ids.Keys.OrderBy (x => r.Next ()).Batch (batchSize);
                var totalTimer = new Stopwatch ();
                totalTimer.Start ();
                int b = 0;
                int batchCount = batches.Count ();
                TimeSpan totalTimeElapsed = TimeSpan.FromSeconds (0);
                int processedAddons = 0;
                var totalAddons = ids.Keys.Count;

                var timer = new Stopwatch ();
                if (addons) {
                    timer.Start ();
                    await client.v2GetAddOnsAsync (ids.Keys.ToArray (), false, false);
                    timer.Stop ();
                    var addonElapsed = timer.Elapsed;
                    logger.LogInformation ("processed {totalAddons} addons in {timeElapsed}", totalAddons, addonElapsed);
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
                    processedAddons += batch.Count ();
                    ++b;
                    logger.LogInformation ("batch {batchprogress}% [{batchNumber} / {batchCount}] [{processedAddons}/{totalAddons}]", b / batchCount * 100, b, batchCount, processedAddons, totalAddons);
                    cache.Save ();
                    timer.Stop ();
                    var elapsed = timer.Elapsed;
                    totalTimeElapsed += elapsed;
                    timer.Restart ();
                    var average = totalTimeElapsed / b;
                    var averagePrediction = average * (batchCount - b);
                    var currentPrediction = (elapsed) * (batchCount - b);

                    logger.LogInformation ("elapsed: {elapsed}, average: {average}, total: {total}", elapsed, average, totalTimeElapsed);
                    logger.LogInformation ("progress: {progress}% [{processedAddons} /{totalAddons}]", b / (float) batchCount * 100, b, batchCount);
                    logger.LogInformation ("prediction [ current: {predictionCurrent}, average: {averagePrediction} ]", currentPrediction, averagePrediction);
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

                logger.LogInformation ("all targets were processed in { totalTime }", totalTimer.Elapsed);
            } catch (Exception e) {
                logger.LogError ("Exception {@Exception}", e);
                throw e;
            }
        }

        private static Task<object> scanTask = null;
        private static CancellationTokenSource findTaskSource = new CancellationTokenSource ();
        private static int ScanRunCounter = 0;

        public Task<object> ScanFiles (bool kill = false, int batchSize = 262144) {
            bool startTask = false;
            if (kill && scanTask != null && scanTask.IsCompleted.Equals (false)) {
                findTaskSource.Cancel ();
                startTask = true;
            } else if (scanTask == null || scanTask.IsCompleted.Equals (true)) {
                startTask = true;
            }
            if (startTask) {
                findTaskSource = new CancellationTokenSource ();
                var token = findTaskSource.Token;
                scanTask = _ScanFiles (token, batchSize);
            }
            return scanTask;
        }

        async private Task<object> _ScanFiles (CancellationToken token, int batchSize) {
            try {
                ScanRunCounter++;
                var rng = new Random ();
                var ids = cache.GetIDs ();
                var addonIDs = ids.Select (kv => kv.Key); /* .OrderBy (a => rng.Next ()); */
                var flatFileIDs = ids.Values.SelectMany (f => f);

                var hiddenFileIDs = new Dictionary<int, IEnumerable<int>> ();
                int processedAddons = 0;
                int totalAddons = addonIDs.Count ();

                logger.LogInformation ($"totalAddonCount: {totalAddons}");
                logger.LogInformation ($"batchSize: {batchSize}");

                Stopwatch totalTimer = new Stopwatch ();
                Stopwatch timer = new Stopwatch ();
                totalTimer.Start ();
                timer.Restart ();

                foreach (var addonID in addonIDs) {
                    token.ThrowIfCancellationRequested ();
                    // timer.Restart();

                    var fileIDs = ids[addonID];
                    if (fileIDs.Count () == 0) {
                        //TODO: set keys to full range here
                        logger.LogInformation ($"[{addonID}] skipping empty file list");
                        processedAddons++;
                        timer.Restart ();
                        continue;
                    }
                    var minId = fileIDs.Min ();
                    var maxId = fileIDs.Max ();
                    var distance = maxId - minId;
                    distance = distance > 10000 ? distance : 10000;
                    var rangeFrom = minId - (distance) / 4;
                    var rangeTo = maxId + (distance) / 4;
                    var rangeCount = rangeTo - rangeFrom;

                    logger.LogInformation ($"[{addonID}] min: {minId} max: {maxId} from: {rangeFrom} to: {rangeTo} length: {rangeCount}");
                    var keys = Enumerable.Range (rangeFrom, rangeCount)
                        .Except (fileIDs)
                        .Select (fileID =>
                            new AddOnFileKey { AddOnID = addonID, FileID = fileID }
                        ).ToArray ();
                    var batches = keys.Batch (batchSize);
                    int b = 0;
                    int processedTotal = 0;
                    int totalCount = keys.Count ();
                    int totalBatches = batches.Count ();
                    logger.LogInformation ($"[{addonID}] {totalCount} possible files");
                    var resultIDs = new List<int> ();
                    foreach (var batch in batches) {
                        token.ThrowIfCancellationRequested ();
                        b++;
                        var partResult = (client.GetAddOnFilesAsync (batch.ToArray (), false, false).Result).SelectMany (pair => pair.Value);
                        processedTotal += batch.Count ();
                        logger.LogInformation ($"[{addonID}] [{b}/{totalBatches}] [{processedTotal}/{totalCount}] found: {partResult.Count()}");
                        resultIDs.AddRange (partResult.Select (f => f.Id));
                    }
                    cache.Save ();

                    logger.LogInformation ($"[{addonID}] hidden files: {resultIDs.ToPrettyJson (false)}");
                    hiddenFileIDs[addonID] = resultIDs.ToArray ();

                    processedAddons++;
                    var elapsed = timer.Elapsed;
                    var totalTimeElapsed = totalTimer.Elapsed;
                    var average = totalTimeElapsed / processedAddons;
                    var averagePrediction = average * (totalAddons - processedAddons);
                    var currentPrediction = elapsed * (totalAddons - processedAddons);

                    // logger.LogInformation (report.ToPrettyJson ());
                    logger.LogInformation ("elapsed: {elapsed}, average: {average}, total: {total}", elapsed, average, totalTimeElapsed);
                    logger.LogInformation ("progress: {progress}% [{processedAddons} / {totalAddons}]", processedAddons / (float) totalAddons * 100, processedAddons, totalAddons);
                    logger.LogInformation ("prediction [ current: {predictionCurrent}, average: {averagePrediction} ]", currentPrediction, averagePrediction);
                    timer.Restart ();

                }
                timer.Stop ();

                var hiddenKeys = hiddenFileIDs.SelectMany (kv =>
                    kv.Value.Select (fileID =>
                        new AddOnFileKey { AddOnID = kv.Key, FileID = fileID }
                    )
                ).ToArray ();

                var hiddenFiles = await client.GetAddOnFilesAsync (hiddenKeys);

                var response = new {
                    uncoveredCount = hiddenFileIDs.Count (),
                    uncovered = hiddenFiles
                };

                var config = Config.instance.Value.output;
                Directory.CreateDirectory (config.BasePath);
                var path = Path.Combine (config.BasePath, "report.json");
                System.IO.File.WriteAllText (path, response.ToPrettyJson ());

                return response;
            } catch (OperationCanceledException ex) {
                logger.LogInformation ("task cancelled");
                return ex;
            } catch (Exception e) {
                logger.LogInformation (e.ToPrettyJson ());
                return e;
            }
        }

    }
}