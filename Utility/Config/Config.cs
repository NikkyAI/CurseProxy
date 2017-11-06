using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using cursemeta.Utility.Configs;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace cursemeta.Utility {

    public class Config {
        public static Lazy<Config> instance { get; private set; } = new Lazy<Config> (() => Load ());
        public static string CONFIG { get; private set; } = Constants.ConfigPath;
        private static string CONFIG_FILE = Path.Combine (CONFIG, "meta.yaml");

        public CacheConfig cache { get; private set; }
        public OutputConfig output { get; private set; }
        public bool reformat { get; private set; } = true;
        public bool reflection { get; private set; } = false;
        
        public Config () {
            cache = CacheConfig.instance.Value;
            output = OutputConfig.instance.Value;
        }

        static private Config Load () {
            // deserialize config
            Config config;
            var deserializer = new DeserializerBuilder ()
                .IgnoreUnmatchedProperties ()
                .WithNamingConvention (new CamelCaseNamingConvention ())
                .Build ();
            if (File.Exists (CONFIG_FILE)) {
                using (var reader = new StreamReader (File.OpenRead (CONFIG_FILE))) {
                    config = deserializer.Deserialize<Config> (reader);
                }
            } else {
                Console.WriteLine ($"Config file does not exist: {CONFIG_FILE}");
                config = new Config ();
            }
            if(config.reformat)
                config.Save ();
            return config;
        }
        
        public void Save () {
            // serialize config
            var serializer = new SerializerBuilder ()
                .WithNamingConvention (new CamelCaseNamingConvention ())
                .Build ();
            Directory.CreateDirectory (Config.CONFIG);
            var text = serializer.Serialize (this);
            File.WriteAllText (CONFIG_FILE, text);
        }
        
        public static Config Reload() {
            instance = new Lazy<Config> (() => Load ());
            return instance.Value;
        }
    }

}