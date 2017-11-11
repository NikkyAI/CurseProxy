using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Cursemeta.AddOnService;
using ICSharpCode.SharpZipLib.BZip2;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;

namespace Cursemeta {
    public class Feed {
        private readonly ILogger logger;
        private readonly Cache cache;

        public Feed (ILogger<Feed> _logger, Cache _cache) {
            logger = _logger;
            cache = _cache;
        }

        private static readonly string COMPLETE_URL = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/complete.json.bz2";
        private static readonly string COMPLETE_URL_TIMESTAMP = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/complete.json.bz2.txt";
        private static readonly string HOURLY_URL = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/hourly.json.bz2";
        private static readonly string HOURLY_URL_TIMESTAMP = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10/hourly.json.bz2.txt";
        private static readonly JsonSerializerSettings settings = new JsonSerializerSettings {
            Formatting = Formatting.Indented,
            MissingMemberHandling = MissingMemberHandling.Error,
            ContractResolver = new CamelCasePropertyNamesContractResolver (),
            // NullValueHandling = NullValueHandling.Ignore,
        };
        private static readonly string cachePath = Path.Combine (Constants.CachePath, "feed");
        private static HttpClient client = new HttpClient ();

        public async Task<ProjectList> GetComplete () {
            var completeFile = Path.Combine (cachePath, "complete.json");
            var completeFileTimestamp = Path.Combine (cachePath, "complete.txt");
            // read timestamp
            var timestamp = await client.GetStringAsync (COMPLETE_URL_TIMESTAMP);
            String uncompressedString = null;
            if (File.Exists (completeFileTimestamp) && File.Exists (completeFile)) {
                var localTimestamp = File.ReadAllText (completeFileTimestamp);
                if (localTimestamp == timestamp) // if complete.json exists, read it
                    uncompressedString = File.ReadAllText (completeFile);
            }

            // download and decompress
            if (uncompressedString == null) {
                uncompressedString = await uncompress (COMPLETE_URL, completeFile, "complete");
            }

            var allProjects = JsonConvert.DeserializeObject<ProjectList> (uncompressedString, settings);
            //TODO: cache IDs
            cache.Add (allProjects.Data);

            Directory.CreateDirectory (Path.GetDirectoryName (completeFileTimestamp));
            File.WriteAllText (completeFileTimestamp, allProjects.Timestamp.ToString ());

            return allProjects;
        }

        public async Task<ProjectList> GetHourly () {
            var hourlyFile = Path.Combine (cachePath, "hourly.json");
            var hourlyFileTimestamp = Path.Combine (cachePath, "hourly.txt");
            // read timestamp
            var timestamp = await client.GetStringAsync (HOURLY_URL_TIMESTAMP);
            String uncompressedString = null;
            if (File.Exists (hourlyFileTimestamp) && File.Exists (hourlyFile)) {
                var localTimestamp = File.ReadAllText (hourlyFileTimestamp);
                if (localTimestamp == timestamp) // if complete.json exists, read it
                    uncompressedString = File.ReadAllText (hourlyFile);
            }

            // download and decompress
            if (uncompressedString == null) {
                uncompressedString = await uncompress (HOURLY_URL, hourlyFile, "hourly");
            }

            var allProjects = JsonConvert.DeserializeObject<ProjectList> (uncompressedString, settings);
            //TODO: cache IDs
            cache.Add (allProjects.Data);

            Directory.CreateDirectory (Path.GetDirectoryName (hourlyFileTimestamp));
            File.WriteAllText (hourlyFileTimestamp, allProjects.Timestamp.ToString ());

            return allProjects;
        }

        public async Task<ProjectList> GetCompleteLocal (string directory) {
            var compressedFile = Path.Combine (directory, "complete.json.bz2");
            String uncompressedString = null;
            if (File.Exists (compressedFile)) {
                //decompress
                using (var filestream = File.OpenRead (compressedFile))
                using (var target = new MemoryStream ()) {
                    BZip2.Decompress (filestream, target, true);
                    uncompressedString = Encoding.UTF8.GetString (target.ToArray ());
                }
            } else {
                //fallback
                return await GetComplete ();
            }

            var allProjects = JsonConvert.DeserializeObject<ProjectList> (uncompressedString, settings);

            return allProjects;
        }

        private async Task<string> uncompress (string url, string file, string mode = "") {
            string uncompressedString;
            logger.LogInformation ("Downloading {mode} project list from Curse. This could take a while ...", mode); // TODO: verbose logging
            using (var stream = await client.GetStreamAsync (url))
            using (var target = new MemoryStream ()) {
                BZip2.Decompress (stream, target, true);
                uncompressedString = Encoding.UTF8.GetString (target.ToArray ());
            }
            // save to cache
            Directory.CreateDirectory (Path.GetDirectoryName (file));
            File.WriteAllText (file, uncompressedString);
            return uncompressedString;
        }

        public static void SaveLocalComplete (ProjectList allProjects, string directory, /* Filter filter, */ bool compressed = true, bool uncompressed = true) {
            // var json_string = allProjects.ToFilteredJson(filter); 
            var json_string = allProjects.ToPrettyJson ();

            var currentFile = Path.Combine (cachePath, "current.json");
            var completeFile = Path.Combine (directory, "complete.json");
            var compressedFile = Path.Combine (directory, "complete.json.bz2");

            //uncompressed
            if (uncompressed) {
                File.WriteAllText (completeFile, json_string);
            }
            File.WriteAllText (currentFile, json_string);

            //compressed
            if (compressed) {
                using (var fileOutStream = File.OpenWrite (compressedFile)) {
                    byte[] byteArray = Encoding.ASCII.GetBytes (json_string);
                    MemoryStream stream = new MemoryStream (byteArray);
                    BZip2.Compress (stream, fileOutStream, true, 4096);
                }
            }
        }

        public static void SaveLocal (ProjectList allProjects, /* Filter filter, */ string output, string basename, bool compressed = true, bool uncompressed = true) {
            // var json_string = allProjects.ToFilteredJson(filter);
            var json_string = allProjects.ToPrettyJson ();
            // var currentFile = Path.Combine(cache, "current.json");
            var completeFile = Path.Combine (output, $"{basename}.json");
            var compressedFile = Path.Combine (output, $"{basename}.json.bz2");

            //uncompressed
            if (uncompressed) {
                File.WriteAllText (completeFile, json_string);
            }
            // File.WriteAllText(currentFile, json_string);

            //compressed
            if (compressed) {
                using (var fileOutStream = File.OpenWrite (compressedFile)) {
                    byte[] byteArray = Encoding.ASCII.GetBytes (json_string);
                    MemoryStream stream = new MemoryStream (byteArray);
                    BZip2.Compress (stream, fileOutStream, true, 4096);
                }
            }
        }
    }

    public class ProjectList {
        public long Timestamp { get; set; }
        public List<AddOn> Data { get; set; }

        public ProjectList merge (ProjectList patch) {
            var newlist = this.Data.Where (s => !patch.Data.Any (p => p.Id == s.Id)).ToList ();
            newlist.AddRange (patch.Data);
            this.Data = newlist.OrderBy (o => o.Id).ToList ();
            var newProjectList = new ProjectList {
                Timestamp = patch.Timestamp,
                Data = newlist.OrderBy (o => o.Id).ToList ()
            };
            return newProjectList;
        }

        public ProjectList clone () {
            return (ProjectList) this.MemberwiseClone ();
        }
    }
}