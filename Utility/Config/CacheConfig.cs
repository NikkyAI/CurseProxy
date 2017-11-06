using System;
using System.IO;
using System.Linq;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace cursemeta.Utility.Configs {
    public class CacheConfig {
        public static readonly Lazy<CacheConfig> instance = new Lazy<CacheConfig> (() => new CacheConfig ());

        [YamlIgnore]
        private readonly Lazy<string> LazyAddonsCachePath;
        [YamlIgnore]
        public string AddonsPath {
            get {
                return LazyAddonsCachePath.Value;
            }
        }
        [YamlIgnore]
        private readonly Lazy<string> LazyBasePath;
        [YamlIgnore]
        public string BasePath {
            get {
                return LazyBasePath.Value;
            }
        }
        
        public string Base { get; set; } = null;
        public string Addons { get; set; } = "addons";

        public CacheConfig () {
            LazyBasePath = new Lazy<string> (() => {
                return Base ?? Constants.CachePath;
            });
            LazyAddonsCachePath = new Lazy<string> (() => {
                if (Path.IsPathRooted (Addons))
                    return Addons;
                else
                    return Path.Combine (BasePath, Addons);
            });
        }
        
        public void test() {
            Console.WriteLine(Directory.Exists(AddonsPath));
        }
    }
}