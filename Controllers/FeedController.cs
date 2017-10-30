using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using cursemeta.AddOnService;
using cursemeta.Utility;

namespace cursemeta.Controllers
{
    
    [Route("api/[controller]")]
    public class FeedController : Controller
    {
        // GET api/feed
        [HttpGet]
        async public Task<JsonResult> Get()
        {
            try {
                return await GetHourly();
            } catch(Exception e) {
                return Json(e.ToString());
            }
        }
        
        // GET api/feed/hourly
        [HttpGet("hourly")]
        async public Task<JsonResult> GetHourly()
        {
            try {
                var client = await DownloadUtil.LazyAddonClient.Value;
                var hourly = await ProjectFeed.GetHourly();
                return Json(hourly);//addon.ToFilteredJson(Filter.Default, true);
            } catch(Exception e) {
                return Json(e.ToString());
            }
        }
        
        // GET api/feed/complete
        [HttpGet("complete")]
        async public Task<JsonResult> GetComplete()
        {
            try {
                var client = await DownloadUtil.LazyAddonClient.Value;
                var complete = await ProjectFeed.GetComplete();
                return Json(complete); //addon.ToFilteredJson(Filter.Default, true);
            } catch(Exception e) {
                return Json(e.ToString());
            }
        }
        
    }
}
