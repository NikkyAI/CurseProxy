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
    public class UpdateController : Controller {
        private readonly ILogger logger;
        private static Task syncTask = null;

        public UpdateController (ILogger<ManifestController> _logger) {
            logger = _logger;
        }

        // GET api/update/sync
        // http://localhost:5000/api/update/sync
        [HttpGet ("sync")]
        async public Task<IActionResult> GetSync () {
            try {
                var config = Config.instance.Value.task.sync;
                int batchSize = Request.Query.GetInt ("batch").ElementAtOr (0, config.BatchSize);
                bool addons = Request.Query.GetBool ("addons").ElementAtOr (0, config.Addons);
                bool descriptions = Request.Query.GetBool ("descriptions").ElementAtOr (0, config.Descriptions);
                bool files = Request.Query.GetBool ("files").ElementAtOr (0, config.Files);
                bool changelogs = Request.Query.GetBool ("changelogs").ElementAtOr (0, config.Changelogs);
                bool gc = Request.Query.GetBool ("gc").ElementAtOr (0, false);

                var task = Update.Sync (batchSize, addons, descriptions, files, changelogs, gc);
                if (task == null)
                    return new ContentResult {
                        ContentType = "text/json",
                        StatusCode = (int) HttpStatusCode.Conflict,
                        Content = new {
                        status = HttpStatusCode.Conflict,
                        message = "Task:Sync failed to start or so"
                        }.ToPrettyJson ()
                    };
                // await task;
                if (syncTask != null) {
                    var oldStatus = syncTask.Status;
                    if (syncTask == task) {
                        return Json (new { message = "Task:Sync was already running", status = task.Status });
                    }
                    syncTask = task;
                    return Json (new { message = "Task:Sync was restarted", status = task.Status, previous = oldStatus });
                } else {
                    syncTask = task;
                    return Json (new { message = "Task:Sync was started", status = task.Status });
                }

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