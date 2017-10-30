using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using cursemeta.AddOnService;
using cursemeta.Utility;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

namespace cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class AddonController : Controller {
        //private readonly ITodoRepository _todoRepository;
        private readonly ILogger _logger;
        private readonly JsonResult Json404 = new JsonResult (404) { StatusCode = (int) HttpStatusCode.NotFound };

        public AddonController ( /* ITodoRepository todoRepository, */ ILogger<AddonController> logger) {
            //_todoRepository = todoRepository;
            _logger = logger;
        }

        // // GET api/Addon
        // [HttpGet]
        // async public Task<JsonResult> Get()
        // {
        //     // var client = await DownloadUtil.LazyAddonClient.Value;
        //     // ProjectList feed = new ProjectList { Data = new List<AddOn>() };
        //     return Json(null);
        // }

        // GET api/addon/process
        // http://localhost:5000/api/addon/process
        [HttpGet ("process")]
        async public Task<IActionResult> GetProcess () {
            try {
                var client = await DownloadUtil.LazyAddonClient.Value;
                var downloadUtil = new DownloadUtil (null);
                downloadUtil.verbose = true;
                downloadUtil.pretty = true;
                downloadUtil.changelogs = true;
                downloadUtil.descriptions = true;
                var complete = await ProjectFeed.GetComplete ();
                var addonIDs = complete.Data.Select (addon => addon.Id).ToArray ();
                var addons = await client.v2GetAddOnsAsync (addonIDs);
                var task = DownloadUtil.processAll (addons, downloadUtil.processAddon);
                return Json ("its running");
            } catch (Exception e) {
                return new ContentResult {
                    ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.InternalServerError,
                        Content = e.ToPrettyJson ()
                };
            }
        }

        [HttpGet ("about")]
        async public Task<IActionResult> GetAbout () {
                try {
                    // Console.WriteLine(this.Request.Method);
                    // Console.WriteLine(this.Request.Protocol);
                    // Console.WriteLine(this.Request.IsHttps);
                    // Console.WriteLine(this.Request.PathBase);
                    string url = $"{this.Request.Scheme}://{this.Request.Host}{this.Request.Path}";
                    // get all known addon ids
                    string html = "";
                    return new ContentResult {
                        ContentType = "text/html",
                        StatusCode = (int) HttpStatusCode.OK,
                        Content = html
                    };
                } catch (Exception e) {
                    return new ContentResult {
                        ContentType = "text/json",
                            StatusCode = (int) HttpStatusCode.InternalServerError,
                            Content = e.ToPrettyJson ()
                    };
                }
            }
            [HttpGet ("test")]
        async public Task<IActionResult> GetTest () {
            try {
                var client = CacheClient.LazyCacheClient.Value;
                var idCache = IdCache.LazyIdCache.Value;
                var ids = idCache.Get ();
                var targetIds = new List<int> ();
                foreach (KeyValuePair<int, HashSet<int>> entry in ids) {
                    // if (entry.Value.Count == 0) {
                        targetIds.Add (entry.Key);
                    // }
                }
                Console.WriteLine ($"targets {string.Join(", ", targetIds)}");
                var batchSize = 500;
                var all = targetIds //.Take(1000)
                    .Select ((addon, index) => new { addon, index })
                    .GroupBy (e => (e.index / batchSize), e => e.addon);
                var timer = new Stopwatch ();
                timer.Start ();
                int k = 0;
                int k_all = all.Count ();
                foreach (var batch in all) {
                    var tasks = Task.WhenAll (batch.Select (
                        (a) => {
                            return client.GetAllFilesForAddOnAsync(a);
                        }
                    ));
                    Console.WriteLine ($"batch [{++k} / {k_all}]");
                    await tasks;
                    idCache.forceSave ();
                    //await Task.Delay(TimeSpan.FromSeconds(0.1)); //testing if this causes problems or not
                }

                //TODO: add retrying

                timer.Stop ();
                Console.WriteLine ($"all targets were processed in '{ timer.Elapsed }'");
                return Json(targetIds);
            } catch (Exception e) {
                return new ContentResult {
                    ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.InternalServerError,
                        Content = e.ToPrettyJson ()
                };
            }
        }

        // GET api/Addon/228756
        // http://localhost:5000/api/addon/228756
        [HttpGet ("{addonID}")]
        async public Task<IActionResult> GetAddOn (int addonID) {
            try {
                var client = CacheClient.LazyCacheClient.Value;
                var addon = await client.GetAddOnAsync (addonID);
                if (addon == null) return NotFound ();
                return Json (addon);
            } catch (Exception e) {
                return new ContentResult {
                    ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.InternalServerError,
                        Content = e.ToPrettyJson ()
                };
            }
        }
        // GET api/Addon/228756
        // http://localhost:5000/api/addon/228756/description
        [HttpGet ("{addonID}/description")]
        async public Task<IActionResult> GetAddOnDescription (int addonID) {
            try {
                var client = CacheClient.LazyCacheClient.Value;
                var description = await client.v2GetAddOnDescriptionAsync (addonID);
                if (description == null) return NotFound ();
                return new ContentResult {
                    ContentType = "text/html",
                    StatusCode = (int) HttpStatusCode.OK,
                    Content = description
                };
            } catch (Exception e) {
                return new ContentResult {
                    ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.InternalServerError,
                        Content = e.ToPrettyJson ()
                };
            }
        }

        // GET api/Addon/5
        // http://localhost:5000/api/addon/228756/files
        [HttpGet ("{addonID}/files")]
        async public Task<IActionResult> GetAllFilesForAddOn (int addonID) {
            try {
                var client = CacheClient.LazyCacheClient.Value;
                var addonFiles = await client.GetAllFilesForAddOnAsync (addonID);
                if (addonFiles == null) return NotFound ();
                return Json (addonFiles);
            } catch (Exception e) {
                return Json (e);
            }
        }

        // GET api/Addon/5
        // http://localhost:5000/api/addon/228756/files/2230896
        [HttpGet ("{addonID}/files/{fileID}")]
        async public Task<IActionResult> GetAddOnFile (int addonID, int fileID) {
            try {
                var client = CacheClient.LazyCacheClient.Value;
                var addonFile = await client.GetAddOnFileAsync (addonID, fileID);
                if (addonFile == null) return NotFound ();
                return Json (addonFile);
            } catch (Exception e) {
                return Json (e);
            }
        }

        // GET api/Addon/5
        // http://localhost:5000/api/addon/59652/files/712640/changelog
        [HttpGet ("{addonID}/files/{fileID}/changelog")]
        async public Task<IActionResult> GetChangeLog (int addonID, int fileID) {
            try {
                var client = CacheClient.LazyCacheClient.Value;
                var changelog = await client.v2GetChangeLogAsync (addonID, fileID);
                if (changelog == null) return NotFound ();
                return new ContentResult {
                    ContentType = "text/html",
                    StatusCode = (int) HttpStatusCode.OK,
                    Content = changelog
                };
            } catch (Exception e) {
                return new ContentResult {
                    ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.InternalServerError,
                        Content = e.ToPrettyJson ()
                };
            }
        }

        // // GET api/Addon/5
        // [HttpGet("{addonID}/{fileID}")]
        // async public Task<JsonResult> GetAddOnFileShort(int addonID, int fileID)
        // {
        //     try {
        //         return await GetAddOnFile(addonID, fileID);
        //     } catch(Exception e) {
        //         return Json(e);
        //     }
        // }

    }
}