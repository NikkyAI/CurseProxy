using System;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;

namespace Cursemeta {
    public class BasicAuthenticationMiddleWare {

        private readonly RequestDelegate _next;

        public BasicAuthenticationMiddleWare (RequestDelegate next) {
            _next = next;
        }
        
        public async Task Invoke (HttpContext context) {
            string authHeader = context.Request.Headers["Authorization"];
            if (authHeader != null && authHeader.StartsWith ("Basic")) {
                //Extract credentials
                string encodedUsernamePassword = authHeader.Substring ("Basic ".Length).Trim ();
                Encoding encoding = Encoding.GetEncoding ("iso-8859-1");
                string usernamePassword = encoding.GetString (Convert.FromBase64String (encodedUsernamePassword));

                int seperatorIndex = usernamePassword.IndexOf (':');

                var username = usernamePassword.Substring (0, seperatorIndex);
                var password = usernamePassword.Substring (seperatorIndex + 1);
                
                var config = Config.instance.Value.auth;
                if(config.users.ContainsKey(username) && password == config.users[username]) {
                    await _next.Invoke (context);
                } else {
                    context.Response.StatusCode = 401; //Unauthorized
                    context.Response.Headers.Add("WWW-Authenticate", $"Basic"); // realm=\"{context.Request.Host}\"
                    return;
                }
            } else {
                // no authorization header
                context.Response.StatusCode = 401; //Unauthorized
                context.Response.Headers.Add("WWW-Authenticate", $"Basic"); // realm=\"{context.Request.Host}\"
                return;
            }
        }
    }
}