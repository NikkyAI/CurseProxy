using System;
using System.IO;
using System.Linq;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace cursemeta.Utility.Configs {
    public class OutputConfig {
        public static readonly Lazy<OutputConfig> instance = new Lazy<OutputConfig>(() => new OutputConfig());
        
        [YamlIgnore]
        private readonly Lazy<string> LazyJsonPath;
        [YamlIgnore]
        public string JsonPath {
            get {
                return LazyJsonPath.Value;
            }
        }
        [YamlIgnore]
        private readonly Lazy<string> LazyFilesPath;
        [YamlIgnore]
        public string FilesPath {
            get {
                return LazyFilesPath.Value;
            }
        }
        
        public string OutputBase { get; set; } = null;
        [YamlIgnore]
        private readonly Lazy<string> LazyBasePath;
        [YamlIgnore]
        public string BasePath {
            get {
                return LazyBasePath.Value;
            }
        }
        public string Files { get; private set; } = "files";
        public string Json { get; private set; } = "json";
        
        
        public OutputConfig () {
            LazyBasePath = new Lazy<string> (() => {
                return OutputBase ?? Path.Combine (Constants.CachePath, "output");
            });
            LazyFilesPath = new Lazy<string> (() => {
                if (Path.IsPathRooted (Files))
                    return Files;
                else
                    return Path.Combine (BasePath, Files);
            });
            LazyJsonPath = new Lazy<string> (() => {
                if (Path.IsPathRooted (Json))
                    return Json;
                else
                    return Path.Combine (BasePath, Json);
            });
        }
    }
}