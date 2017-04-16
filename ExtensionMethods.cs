using System.IO;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Alpacka.Meta
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
                Formatting = Formatting.Indented,
                NullValueHandling = NullValueHandling.Ignore,
                Converters = { new StringEnumConverter { CamelCaseText = true } }
            };

        public static string ToPrettyJson(this object obj) => JsonConvert.SerializeObject(obj, settings);
        
        private static readonly Serializer serializer = 
            new SerializerBuilder()
                .Build();
        
        public static string ToPrettyYaml(this object obj) => serializer.Serialize(obj);
        
    }
}
