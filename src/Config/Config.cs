using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Cursemeta.Configs;
using Cursemeta.Tasks;
using Serilog;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Cursemeta {

    public class Config {
        public static Lazy<Config> instance { get; private set; } = new Lazy<Config> (() => Load ());
        public static string CONFIG { get; private set; } = Constants.ConfigPath;
        private static string CONFIG_FILE = Path.Combine (CONFIG, "meta.yaml");
        private static ILogger logger = Log.ForContext<Config> ();

        public CacheConfig cache { get; private set; }
        public OutputConfig output { get; private set; }
        public TaskConfig task { get; private set; }
        public AuthConfig auth { get; private set; }
        public bool reformat { get; private set; } = true;
        public bool reflection { get; private set; } = false;
        public bool registration { get; private set; } = false;

        public Config () {
            cache = CacheConfig.instance.Value;
            output = OutputConfig.instance.Value;
            task = TaskConfig.instance.Value;
            auth = AuthConfig.instance.Value;
            var r = new Random ();
        }

        static private Config Load () {
            // deserialize config
            Config config;
            var deserializer = new DeserializerBuilder ()
                .IgnoreUnmatchedProperties ()
                .WithNamingConvention (new CamelCaseNamingConvention ())
                .Build ();
            if (File.Exists (CONFIG_FILE)) {
                logger.Information ("loading...");
                using (var reader = new StreamReader (File.OpenRead (CONFIG_FILE))) {
                    config = deserializer.Deserialize<Config> (reader);
                }
            } else {
                logger.Error ($"Config file does not exist: {CONFIG_FILE}");
                config = new Config ();
            }
            if (config.reformat) {
                logger.Information ("reformatting...");
                config.Save ();
            }
            return config;
        }

        public void Save () {
            logger.Information ("serializing...");
            // serialize config
            var serializer = new SerializerBuilder ()
                .WithNamingConvention (new CamelCaseNamingConvention ())
                .EmitDefaults ()
                .Build ();
            Directory.CreateDirectory (Config.CONFIG);
            var text = serializer.Serialize (this);
            File.WriteAllText (CONFIG_FILE, text);;
        }

        public static Config Reload () {
            logger.Information ("reloading...");
            instance = new Lazy<Config> (() => Load ());

            return instance.Value;
        }
    }

}