using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Cursemeta;
using Cursemeta.AddOnService;
using Cursemeta.LoginService;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Primitives;

namespace Cursemeta.Controllers {

    [Route ("api/[controller]")]
    public class RegisterController : Controller {
        private Config config => Config.instance.Value;
        private readonly ILogger logger;
        private readonly Client client;

        public RegisterController (ILogger<RegisterController> _logger, Client _client) {
            logger = _logger;
            client = _client;
        }

        // POST api/register
        // http://localhost:5000/api/register

        [HttpPost]
        async public Task<IActionResult> PostRegister ([FromBody] RegisterRequest registerRequest) {
            try {
                if (config.registration) {
                    var result = await client.Register (registerRequest);
                    return Json (result);
                } else {
                    return BadRequest ("registration is disabled");
                }
            } catch (Exception e) {
                logger.LogError ("{@Exception}", e);
                throw;
            }
        }
    }
}