using System;
using Microsoft.Extensions.CommandLineUtils;
using Alpacka.Meta.LoginService;


namespace Alpacka.Meta
{
    public class CommandRegister : CommandLineApplication
    {
        public CommandRegister()
        {
            Name = "register";
            Description = "register a account";
            
            var argUsername = Argument("[username]",
                "Username");
            
            var argEmail = Argument("[email]",
                "Email");
            
            var argPassword = Argument("[password]",
                "Password");
            
            HelpOption("-? | -h | --help");
            
             OnExecute(async () => {
                var loginClient = new ClientLoginServiceClient(ClientLoginServiceClient.EndpointConfiguration.BinaryHttpsClientLoginServiceEndpoint);
                 
                var registerRequest = new RegisterRequest {
                    Email = argEmail.Value, 
                    NewsletterOptIn = false, 
                    Password = argPassword.Value, 
                    Username = argUsername.Value
                };
                
                var result = await loginClient.RegisterAsync(registerRequest);
                
                Console.WriteLine(result.ToPrettyYaml());
                
                return 0;
             });
        }
    }
}