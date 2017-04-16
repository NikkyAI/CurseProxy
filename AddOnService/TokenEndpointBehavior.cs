using System;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Description;
using System.Runtime.Serialization;
using Alpacka.Meta.LoginService;

namespace Alpacka.Meta.AddOnService
{
    public class TokenEndpointBehavior : IEndpointBehavior, IClientMessageInspector
    {
        private AuthenticationToken Token { get; set; }
        
        public TokenEndpointBehavior(LoginResponse loginResponse)
        { 
            Token = new AuthenticationToken
            {
                Token = loginResponse.Session.Token,
                UserID = loginResponse.Session.UserID
            };
        }

        public TokenEndpointBehavior(String token, int userid)
        {
            Token = new AuthenticationToken
            {
                Token = token,
                UserID = userid
            };

        }

        public object BeforeSendRequest(ref Message request, IClientChannel channel)
        {
            var header = MessageHeader.CreateHeader("AuthenticationToken", "urn:Curse.FriendsService:v1", Token);
            request.Headers.Add(header);
            return null;
        }

        public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(this);
        }

        public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
        {
        }

        public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        {
        }

        public void AfterReceiveReply(ref Message reply, object correlationState)
        {
        }

        public void Validate(ServiceEndpoint endpoint)
        {
        }

        [DataContract(Namespace = "urn:Curse.FriendsService:v1")]
        private class AuthenticationToken
        {
            [DataMember]
            public int UserID { get; set; }

            [DataMember]
            public string Token { get; set; }

            [DataMember]
            public string ApiKey { get; set; }
        }
    }
}
