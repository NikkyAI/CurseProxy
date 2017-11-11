using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.AddOnService;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

namespace Cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class FeedController : Controller {
        private readonly ILogger logger;
        private readonly Feed feed;

        public FeedController (ILogger<FeedController> _logger, Feed _feed) {
            logger = _logger;
            feed = _feed;
        }

        // GET api/feed
        [HttpGet]
        async public Task<IActionResult> Get () {
            try {
                return await GetHourly ();
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/feed/hourly
        // http://localhost:5000/api/feed/hourly
        [HttpGet ("hourly")]
        async public Task<IActionResult> GetHourly () {
            try {
                var hourly = await feed.GetHourly ();
                return Json (hourly); //addon.ToFilteredJson(Filter.Default, true);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

        // GET api/feed/complete
        [HttpGet ("complete")]
        async public Task<IActionResult> GetComplete () {
            try {
                var complete = await feed.GetComplete ();
                return Json (complete); //addon.ToFilteredJson(Filter.Default, true);
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }

    }
}