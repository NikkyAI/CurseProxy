using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using ICSharpCode.SharpZipLib.BZip2;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using Alpacka.Meta.AddOnService;

namespace Alpacka.Meta
{
    public class ProjectFeed
    {
        private static readonly string COMPLETE_URL = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/complete.json.bz2"; 
        private static readonly string COMPLETE_URL_TIMESTAMP = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/complete.json.bz2.txt";
        private static readonly string HOURLY_URL = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/hourly.json.bz2"; 
        private static readonly string HOURLY_URL_TIMESTAMP = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/hourly.json.bz2.txt";
        private static readonly JsonSerializerSettings settings = new JsonSerializerSettings {
            Formatting = Formatting.Indented,
            MissingMemberHandling = MissingMemberHandling.Error,
            ContractResolver = new CamelCasePropertyNamesContractResolver(),
            // NullValueHandling = NullValueHandling.Ignore,
        };
        private static readonly string cache = Path.Combine(Constants.CachePath, "curse");
        private static HttpClient client = new HttpClient();
        
        public static async Task<ProjectList> GetComplete()
        {
            var completeFile = Path.Combine(cache, "complete.json");
            var completeFileTimestamp = Path.Combine(cache, "complete.txt");
            // read timestamp
            var timestamp = await client.GetStringAsync(COMPLETE_URL_TIMESTAMP);
            String uncompressedString = null;
            if (File.Exists(completeFileTimestamp) && File.Exists(completeFile)) {
                var localTimestamp = File.ReadAllText(completeFileTimestamp);
                if (localTimestamp == timestamp) // if complete.json exists, read it
                    uncompressedString = File.ReadAllText(completeFile);
            }
            
            // download and decompress
            if (uncompressedString == null) {
                uncompressedString = await uncompress(COMPLETE_URL, completeFile);
            }
            
            var allProjects = JsonConvert.DeserializeObject<ProjectList>(uncompressedString, settings);
            
            Directory.CreateDirectory(Path.GetDirectoryName(completeFileTimestamp));
            File.WriteAllText(completeFileTimestamp, allProjects.Timestamp.ToString());
            
            return allProjects;
        }
        
        
        public static async Task<ProjectList> GetHourly()
        {
            var hourlyFile = Path.Combine(cache, "hourly.json");
            var hourlyFileTimestamp = Path.Combine(cache, "hourly.txt");
            // read timestamp
            var timestamp = await client.GetStringAsync(HOURLY_URL_TIMESTAMP);
            String uncompressedString = null;
            if (File.Exists(hourlyFileTimestamp) && File.Exists(hourlyFile)) {
                var localTimestamp = File.ReadAllText(hourlyFileTimestamp);
                if (localTimestamp == timestamp) // if complete.json exists, read it
                    uncompressedString = File.ReadAllText(hourlyFile);
            }
            
            // download and decompress
            if (uncompressedString == null) {
                uncompressedString = await uncompress(HOURLY_URL, hourlyFile);
            }
            
            var allProjects = JsonConvert.DeserializeObject<ProjectList>(uncompressedString, settings);
            
            Directory.CreateDirectory(Path.GetDirectoryName(hourlyFileTimestamp));
            File.WriteAllText(hourlyFileTimestamp, allProjects.Timestamp.ToString());
            
            return allProjects;
        }
        
        private static async Task<string> uncompress(string url, string file)
        {
            string uncompressedString;
            Console.WriteLine($"Downloading Curse project database. This could take a while ..."); // TODO: verbose logging
            using (var stream = await client.GetStreamAsync(url))
            using (var target = new MemoryStream()) {
                BZip2.Decompress(stream, target, true);
                uncompressedString = Encoding.UTF8.GetString(target.ToArray());
            }
            // save to cache
            Directory.CreateDirectory(Path.GetDirectoryName(file));
            File.WriteAllText(file, uncompressedString);
            return uncompressedString;
        }
    }
    
    
    public class ProjectList
    {
        public long Timestamp { get; set; }
        public List<AddOn> Data { get; set; }
    }
}
