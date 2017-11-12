using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.AddOnService;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace Cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class AddonController : Controller {
        private Config config = Config.instance.Value;
        private readonly ILogger logger;
        private readonly Client client;
        private readonly Cache cache;

        public AddonController (ILogger<AddonController> _logger, Client _client, Cache _cache) {
            logger = _logger;
            client = _client;
            cache = _cache;
        }

        // GET api/Addon
        // http://localhost:5000/api/addon
        // http://localhost:5000/api/addon?limit=100
        // http://localhost:5000/api/addon?worlds=1&property=name&property=categories.name&property=CategorySection.id
        // http://localhost:5000/api/addon?modpacks=1&property=name&propertiy=id&property=categories.name
        // http://localhost:5000/api/addon?mods=1&order=downloadcount&reverse=1&limit=1000&property=id,name,gamePopularityRank,primaryauthorname,websiteurl&property=downloadcount
        [HttpGet]
        async public Task<IActionResult> Get () {
            try {
                var ids = cache.GetIDs ();
                var keys = ids.Keys.ToArray ();
                await Task.Run (() => { });
                IEnumerable<AddOn> addons = await client.v2GetAddOnsAsync (keys);

                // category filter

                bool mods = Request.Query.GetBool ("mods").ElementAtOr (0, false);
                bool texturePacks = Request.Query.GetBool ("texturepacks").ElementAtOr (0, false);
                bool worlds = Request.Query.GetBool ("worlds").ElementAtOr (0, false);
                bool modpacks = Request.Query.GetBool ("modpacks").ElementAtOr (0, false);
                if (new string[] { "mods", "texturepacks", "worlds", "modpacks" }.Any (s => Request.Query.Keys.Contains (s)))
                    addons = addons.filter (mods, texturePacks, worlds, modpacks);

                // order by

                var orderBy = Request.Query.GetString ("order").ElementAtOr (0, null);
                var orderReverse = Request.Query.GetBool ("reverse").ElementAtOr (0, false);
                if (orderBy != null) {
                    if (orderReverse)
                        addons = addons.OrderByDescending (a => a.GetPropValue (orderBy));
                    else
                        addons = addons.OrderBy (a => a.GetPropValue (orderBy));
                }

                // limit results

                var limit = Request.Query.GetInt ("limit").ElementAtOr (0, -1);
                if (limit > 0) {
                    addons = addons.Take (limit);
                }

                // group results

                var groupBy = Request.Query.GetString ("group").ElementAtOr (0, null);

                // 

                var properties = Request.Query.GetString ("property").SelectMany (s => s.Split (","));

                if (groupBy != null) {
                    if (properties.Count () > 0) {
                        var groupedAddons = addons.GroupBy (a => a.GetPropValue (groupBy), a => {
                            var x = new Dictionary<string, Object> ();
                            foreach (String property in properties) {
                                object value = a.GetPropValue (property);
                                x.Add (property, value);
                            }
                            return x;
                        }).ToDictionary (x => x.Key, y => y.ToArray ());
                        return Json (groupedAddons);
                    } else {
                        var groupedAddons = addons.GroupBy (a => a.GetPropValue (groupBy), a => a).ToDictionary (x => x.Key, y => y.ToArray ());
                        return Json (groupedAddons);
                    }
                }

                if (properties.Count () > 0) {
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
                        var err = new {
                            error = $"unsuported request parameters",
                            invalid_parameters = Request.Query["property"].Select (s => $"property={s}"),
                            solution = "enable reflection in the configuration"
                        };
                        logger.LogWarning (err.ToPrettyJson ());
                        return BadRequest (Json (err));
                    }
                }

                return Json (addons);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/Addon/228756
        // http://localhost:5000/api/addon/228756
        [HttpGet ("{addonID}")]
        async public Task<IActionResult> GetAddOn (int addonID) {
            try {
                logger.LogInformation ("GetAddon ({addonID})", addonID);
                var addon = await client.GetAddOnAsync (addonID);
                if (addon == null) return NotFound ();
                return Json (addon);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }
        // GET api/Addon/228756
        // http://localhost:5000/api/addon/228756/description
        [HttpGet ("{addonID}/description")]
        async public Task<IActionResult> GetAddOnDescription (int addonID) {
            try {
                var description = await client.v2GetAddOnDescriptionAsync (addonID);
                if (description == null) return NotFound ();
                return new ContentResult {
                    ContentType = "text/html",
                    StatusCode = (int) HttpStatusCode.OK,
                    Content = description
                };
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/Addon/5
        // http://localhost:5000/api/addon/228756/files
        [HttpGet ("{addonID}/files")]
        async public Task<IActionResult> GetAllFilesForAddOn (int addonID) {
            try {
                var addonFiles = await client.GetAllFilesForAddOnAsync (addonID);
                if (addonFiles == null) return NotFound ();
                return Json (addonFiles);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/Addon/5
        // http://localhost:5000/api/addon/228756/files/2230896
        [HttpGet ("{addonID}/files/{fileID}")]
        async public Task<IActionResult> GetAddOnFile (int addonID, int fileID) {
            try {
                var addonFile = await client.GetAddOnFileAsync (addonID, fileID);
                if (addonFile == null) return NotFound ();
                return Json (addonFile);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/Addon/5
        // http://localhost:5000/api/addon/59652/files/712640/changelog
        [HttpGet ("{addonID}/files/{fileID}/changelog")]
        async public Task<IActionResult> GetChangeLog (int addonID, int fileID) {
            try {
                var changelog = await client.v2GetChangeLogAsync (addonID, fileID);
                if (changelog == null) return NotFound ();
                return new ContentResult {
                    ContentType = "text/html",
                    StatusCode = (int) HttpStatusCode.OK,
                    Content = changelog
                };
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/addon/ids
        // http://localhost:5000/api/addon/ids

        [HttpGet ("ids")]
        public IActionResult GetIDs () {
            try {
                var ids = cache.GetIDs ();

                return Json (ids);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/addon/ids
        // http://localhost:5000/api/addon/hidden

        [HttpGet ("hidden")]
        async public Task<IActionResult> GetHidden () {
            try {
                var ids = cache.GetIDs ();

                var fileKeys = ids.SelectMany (kv =>
                    kv.Value.Select (fileID =>
                        new AddOnFileKey { AddOnID = kv.Key, FileID = fileID }
                    )
                ).ToArray ();

                var fileDict = await client.GetAddOnFilesAsync (fileKeys);

                var flatFiles = fileDict.Values.SelectMany (f => f);
                flatFiles = flatFiles.Where (f => f.FileStatus != FileStatus.Normal && f.FileStatus != FileStatus.SemiNormal);

                var properties = Request.Query.GetString ("property").SelectMany (s => s.Split (","));

                if (properties.Count () > 0) {
                    var groupedFiles = flatFiles.GroupBy (f => f.FileStatus, f => {
                        var x = new Dictionary<string, Object> ();
                        foreach (String property in properties) {
                            object value = f.GetPropValue (property);
                            x.Add (property, value);
                        }
                        return x;
                    }).ToDictionary (f => f.Key, f => f.ToArray ());
                    return Json (groupedFiles);
                } else {
                    var groupedAddons = flatFiles.GroupBy (f => f.FileStatus, f => f).ToDictionary (f => f.Key, f => f.ToArray ());
                    return Json (groupedAddons);
                }

                //var groupedFiles = flatFiles.GroupBy (f => f.FileStatus).ToDictionary (f => f.Key, f => f.ToArray ());

                //return Json (groupedFiles);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // POST api/addon/files
        // http://localhost:5000/api/addon/files?p=id&p=downloadurl&p=addon.id&p=addon.name&p=addon.categorysection.name&p=addon.categorysection.packagetype&p=addon.categorysection.path

        [HttpPost ("files")]
        async public Task<IActionResult> PostFiles ([FromBody] AddOnFileKey[] keys) {
            try {
                var files = await client.GetAddOnFilesAsync (keys);
                var addons = (await client.v2GetAddOnsAsync (files.Keys.ToArray ())).ToDictionary (a => a.Id, a => a);

                var merged = files.SelectMany (x => x.Value.Select (y => {
                    var bundle = new AddonFileBundle (y, addons[x.Key]);
                    return bundle;
                }));

                var p1 = Request.Query.GetString ("property");
                var p2 = Request.Query.GetString ("p");
                var properties = new String[p1.Length + p2.Length];
                p1.CopyTo (properties, 0);
                p2.CopyTo (properties, p1.Length);

                if (properties.Length > 0) {
                    var result = merged.Select (a => {
                        var x = new Dictionary<string, Object> ();
                        foreach (String property in properties) {
                            object value = a.GetPropValue (property);
                            x.Add (property, value);
                        }
                        return x;
                    });
                    return Json (result);
                }
                return Json (merged);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

    }
}