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

        public AddonController ( /* ITodoRepository todoRepository, */ ILogger<AddonController> logger) {
            //_todoRepository = todoRepository;
            _logger = logger;
        }

        // GET api/Addon
        // http://localhost:5000/api/addon
        [HttpGet]
        async public Task<IActionResult> Get()
        {
            try {
                var idCache = IdCache.LazyIdCache.Value;
                var ids = idCache.Get();
                var keys = ids.Keys.ToArray();
                var client = await DownloadUtil.LazyAddonClient.Value;
                // var addons = await client.v2GetAddOnsAsync(keys);
                
                return Json(keys);
            } catch (Exception e) {
                return new ContentResult {
                ContentType = "text/json",
                    StatusCode = (int) HttpStatusCode.InternalServerError,
                    Content = e.ToPrettyJson ()
                };
            }
        }
        
        // Test
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
                await Task.Run(() => Console.WriteLine("satisfy the async warning"));
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

        

        // GET api/Addon/228756
        // http://localhost:5000/api/addon/228756
        [HttpGet ("{addonID}")]
        async public Task<IActionResult> GetAddOn (int addonID) {
            try {
                var client = CacheClient.LazyClient.Value;
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
                var client = CacheClient.LazyClient.Value;
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
                var client = CacheClient.LazyClient.Value;
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
                var client = CacheClient.LazyClient.Value;
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
                var client = CacheClient.LazyClient.Value;
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