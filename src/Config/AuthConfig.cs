using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Cursemeta.Utility;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Cursemeta.Configs {
    public class AuthConfig {
        public static readonly Lazy<AuthConfig> instance = new Lazy<AuthConfig> (() => new AuthConfig ());

        public Dictionary<string, string> users { get; private set; } = new Dictionary<string, string> () { { "user", StringUtil.RandomString (16) }, { "admin", StringUtil.RandomString (16) }
        };
    }
}