using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.AddOnService;
using Cursemeta.Modpacks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class ModpackController : Controller {
        private Config config = Config.instance.Value;
        private readonly ILogger logger;

        public ModpackController (ILogger<ModpackController> _logger) {
            logger = _logger;
        }

        // GET api/update
        // http://localhost:5000/api/modpack

        [HttpPost]
        async public Task<IActionResult> Post ([FromBody] Manifest manifest) {
            try {
                var client = CacheClient.LazyClient.Value;

                await Task.Run (() => { });

                var addonIDs = manifest.Files.Select (file => file.ProjectID).Distinct ().ToArray ();
                var keys = manifest.Files.Select (file => new AddOnFileKey () { AddOnID = file.ProjectID, FileID = file.FileID }).ToArray ();

                var addonsDict = (await client.v2GetAddOnsAsync (addonIDs)).ToDictionary (a => a.Id, a => a);
                var files = await client.GetAddOnFilesAsync (keys);
                
                var test = files.SelectMany (x => x.Value.Select (y => {
                    var bundle = new AddonFileBundle(y, addonsDict[x.Key]);
                    return bundle;
                }));

                // WARNING: UNSAFE REFLECTION CODE
                var p1 = Request.Query.GetString ("property");
                var p2 = Request.Query.GetString ("p");
                var properties = new String[p1.Length+p2.Length];
                p1.CopyTo(properties, 0);
                p2.CopyTo(properties, p1.Length);
                
                if (properties.Length > 0) {
                    var result = test.Select (a => {
                        var x = new Dictionary<string, Object> ();
                        foreach (String property in properties) {
                            object value = a.GetPropValue (property);
                            x.Add (property, value);
                        }
                        return x;
                    });
                    return Json (result);
                }
                return Json (test);
            } catch (Exception e) {
                return new ContentResult {
                    ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.InternalServerError,
                        Content = e.ToPrettyJson ()
                };
            }
        }
    }
}