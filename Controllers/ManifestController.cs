using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.AddOnService;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace Cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class ManifestController : Controller {
        private Config config => Config.instance.Value;
        private readonly ILogger logger;
        private readonly Client client;

        public ManifestController (ILogger<ManifestController> _logger, Client _client) {
            logger = _logger;
            client = _client;
        }

        // POST api/manifest
        // http://localhost:5000/api/manifest?p=id&p=downloadurl&p=addon.id&p=addon.name&p=addon.categorysection.name&p=addon.categorysection.packagetype&p=addon.categorysection.path

        [HttpPost]
        async public Task<IActionResult> PostManifest ([FromBody] Manifest manifest) {
            try {
                var keys = manifest.Files.Select (file => new AddOnFileKey () { AddOnID = file.ProjectID, FileID = file.FileID }).ToArray ();
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