using System.IO;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using YamlDotNet.Serialization;

namespace cursemeta.Utility
{
    public static class ExtensionMethods
    {
        public static void ClearReadOnly(this DirectoryInfo parentDirectory)
        {
            if (parentDirectory != null)
            {
                parentDirectory.Attributes = FileAttributes.Normal;
                foreach (FileInfo fi in parentDirectory.GetFiles())
                {
                    fi.Attributes = FileAttributes.Normal;
                }
                foreach (DirectoryInfo di in parentDirectory.GetDirectories())
                {
                    di.ClearReadOnly();
                }
            }
        }
        
        private static readonly JsonSerializerSettings settings =
            new JsonSerializerSettings
            {
                Formatting = Formatting.None,
                NullValueHandling = NullValueHandling.Ignore,
                Converters = { new StringEnumConverter { CamelCaseText = true } }
            };
        
        private static readonly JsonSerializerSettings settingsPretty =
            new JsonSerializerSettings
            {
                Formatting = Formatting.Indented,
                NullValueHandling = NullValueHandling.Ignore,
                Converters = { new StringEnumConverter { CamelCaseText = true } }
            };
        
        public static string ToPrettyJson(this object obj, bool pretty = true) => JsonConvert.SerializeObject(obj, pretty ? settingsPretty : settings);
        
        private static readonly Serializer serializer = 
            new SerializerBuilder()
                .Build();
        
        public static string ToPrettyYaml(this object obj) => serializer.Serialize(obj);
        
    }
}
