using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Threading.Tasks;
using cursemeta.AddOnService;
using cursemeta.Utility;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class AddonController : Controller {
        private Config config = Config.instance.Value;
        //private readonly ITodoRepository _todoRepository;
        private readonly ILogger _logger;

        public AddonController ( /* ITodoRepository todoRepository, */ ILogger<AddonController> logger) {
            //_todoRepository = todoRepository;
            _logger = logger;
        }

        // GET api/Addon
        // http://localhost:5000/api/addon
        // http://localhost:5000/api/addon?worlds=1&property=categorie.names&property=categories.name&property=CategorySection.id
        [HttpGet]
        async public Task<IActionResult> Get () {
            try {
                var cache = Cache.LazyCache.Value;
                var ids = cache.GetIDs ();
                var keys = ids.Keys.ToArray ();
                await Task.Run (() => { });
                var client = CacheClient.LazyClient.Value;
                IEnumerable<AddOn> addons = await client.v2GetAddOnsAsync (keys);

                bool mods = Request.Query.GetBool ("mods").ElementAtOr (0, false);
                bool texturePacks = Request.Query.GetBool ("texturepacks").ElementAtOr (0, false);
                bool worlds = Request.Query.GetBool ("worlds").ElementAtOr (0, false);
                bool modpacks = Request.Query.GetBool ("modpacks").ElementAtOr (0, false);
                if (new string[] { "mods", "texturepacks", "worlds", "modpacks" }.Any (s => Request.Query.Keys.Contains (s)))
                    addons = addons.filter (mods, texturePacks, worlds, modpacks);

                var categoryIDs = new List<int> ();
                var categoryNames = new List<string> ();
                StringValues categoryValues;
                if (Request.Query.TryGetValue ("category", out categoryValues)) {

                    foreach (String category in categoryValues) {
                        int categoryID;
                        if (int.TryParse (category, out categoryID)) {
                            categoryIDs.Add (categoryID);
                        } else {
                            categoryNames.Add (category);
                        }
                    }
                    addons = addons.Where (a => categoryIDs.Contains (a.CategorySection.ID) || categoryNames.Contains (a.CategorySection.Name));
                }

                // WARNING: UNSAFE REFLECTION CODE
                var properties = Request.Query.GetString ("property");
                if (properties.Length > 0) {
                    if (config.reflection) {
                        var result = addons.Select (a => {
                            var x = new Dictionary<string, Object> ();
                            foreach (String property in properties) {
                                object value = a.GetPropValue (property);
                                x.Add (property, value);
                            }
                            return x;
                        });
                        return Json (result);
                    } else {
                        // reflection is disabled
                        var e = new {
                            error = $"unsuported request parameters",
                            invalid_parameters = Request.Query["property"].Select(s => $"property={s}"),
                            solution = "enable reflection in the configuration"
                        };
                        return new ContentResult {
                            ContentType = "text/json",
                                StatusCode = (int) HttpStatusCode.BadRequest,
                                Content = e.ToPrettyJson ()
                        };
                    }
                }

                return Json (addons);
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
                Console.WriteLine($"addon {addonID}");
                var client = CacheClient.LazyClient.Value;
                var addon = await client.GetAddOnAsync (addonID);
                //if (addon == null) return NotFound ();
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