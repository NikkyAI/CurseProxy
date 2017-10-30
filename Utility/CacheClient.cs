using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using cursemeta.AddOnService;
using cursemeta.LoginService;
using cursemeta.Utility;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace cursemeta.Utility {
    public class CacheClient {
        private static readonly Lazy<Task<AddOnServiceClient>> LazyAddonClient = new Lazy<Task<AddOnServiceClient>> (() => Authenticate ());
        public static readonly Lazy<CacheClient> LazyClient = new Lazy<CacheClient> (() => new CacheClient ());
        public static string CONFIG { get; private set; } = Constants.ConfigPath;
        public string OUTPUT { get; private set; } = Path.Combine (Constants.CachePath, "output");

        public AddOnServiceClient client { get; private set; }
        private IdCache idCache = IdCache.LazyIdCache.Value;
        
        private CacheClient () {
            client = LazyAddonClient.Value.Result;
        }

        private static async Task<AddOnServiceClient> Authenticate () {
            AddOnServiceClient client;
            var loginClient = new ClientLoginServiceClient (ClientLoginServiceClient.EndpointConfiguration.BinaryHttpsClientLoginServiceEndpoint);

            var deserializer = new DeserializerBuilder ()
                .IgnoreUnmatchedProperties ()
                .WithNamingConvention (new CamelCaseNamingConvention ())
                .Build ();

            var path = Path.Combine (CONFIG, "curse.yaml");

            LoginResponse loginResponse;

            using (var reader = new StreamReader (File.OpenRead (path))) {
                var request = deserializer.Deserialize<LoginRequest> (reader);
                loginResponse = await loginClient.LoginAsync (request);
                Console.WriteLine ($"Login: {loginResponse.Status}");
            }

            client = new AddOnServiceClient (AddOnServiceClient.EndpointConfiguration.BinaryHttpsAddOnServiceEndpoint);
            client.Endpoint.EndpointBehaviors.Add (new TokenEndpointBehavior (loginResponse));

            return client;
        }

        private static readonly JsonSerializerSettings jsonSerializerSettings = new JsonSerializerSettings {
            Formatting = Formatting.None,
            MissingMemberHandling = MissingMemberHandling.Error,
            ContractResolver = new CamelCasePropertyNamesContractResolver (),
            NullValueHandling = NullValueHandling.Ignore
        };

        public static IEnumerable<List<T>> splitList<T> (List<T> locations, int nSize = 30) {
            for (int i = 0; i < locations.Count; i += nSize) {
                yield return locations.GetRange (i, Math.Min (nSize, locations.Count - i));
            }
        }

        async public Task<AddOn> GetAddOnAsync (int id) {
            var result = await client.GetAddOnAsync (id);
            if (result == null) return result;
            //TODO: var addon = result.filter();
            var task = Task.Run (() => {
                idCache.Add(id);
                var directory = Path.Combine (OUTPUT, $"{id}");
                Directory.CreateDirectory (directory);
                File.WriteAllText (Path.Combine (directory, "index.json"), result.ToPrettyJson ());
            });
            return result;
        }

        async public Task<AddOn[]> v2GetAddOnsAsync (int[] ids) {
            var list = new List<int> (ids);
            var split = splitList (list, 16384);
            var result = new List<AddOn> ();
            Console.WriteLine ($"v2GetAddOnsAsync {ids.Length}");
            foreach (List<int> idList in split) {
                var partResult = await client.v2GetAddOnsAsync (idList.ToArray ());
                if (result == null) continue;
                //TODO: var addon = result.filter();
                var task = Task.Run (() => {
                    idCache.Add(partResult);
                    foreach (var addon in partResult) {
                        var directory = Path.Combine (OUTPUT, $"{ addon.Id }");
                        Directory.CreateDirectory (directory);
                        File.WriteAllText (Path.Combine (directory, "index.json"), addon.ToPrettyJson ());
                    }
                });
                result.AddRange (partResult);
                //TODO: append to result
            }

            return result.ToArray ();
        }

        async public Task<string> v2GetChangeLogAsync (int addonID, int fileID) {
            var result = await client.v2GetChangeLogAsync (addonID, fileID);
            if (result == null) return result;
            var task = Task.Run (() => {
                idCache.Add(addonID, fileID);
                var directory = Path.Combine (OUTPUT, $"{addonID}", "files");
                Directory.CreateDirectory (directory);
                File.WriteAllText (Path.Combine (directory, $"{fileID}.changelog.txt"), result);
            });
            return result;
        }

        async public Task<string> v2GetAddOnDescriptionAsync (int id) {
            var result = await client.v2GetAddOnDescriptionAsync (id);
            if (result == null) return result;
            var task = Task.Run (() => {
                idCache.Add(id);
                var directory = Path.Combine (OUTPUT, $"{id}");
                Directory.CreateDirectory (directory);
                File.WriteAllText (Path.Combine (directory, $"description.html"), result);
            });
            return result;
        }

        async public Task<AddOnFile> GetAddOnFileAsync (int addonID, int fileID) {
            var result = await client.GetAddOnFileAsync (addonID, fileID);
            if (result == null) return result;
            //TODO: var files = result.filter();
            var task = Task.Run (() => {
                idCache.Add(addonID, fileID);
                var directory = Path.Combine (OUTPUT, $"{addonID}", "files");
                Directory.CreateDirectory (directory);
                File.WriteAllText (Path.Combine (directory, $"{fileID}.json"), result.ToPrettyJson ());
            });
            return result;
        }

        async public Task<AddOnFile[]> GetAllFilesForAddOnAsync (int addOnID) {
            var directory = Path.Combine (OUTPUT, $"{addOnID}", "files");
            try {
                var result = await client.GetAllFilesForAddOnAsync (addOnID);
                if (result != null) {
                    //TODO: var files = result.filter();
                    var task = Task.Run (() => {
                        Directory.CreateDirectory (directory);
                        var changed = idCache.Add(addOnID, result);
                        foreach (var addonFile in result) {
                            File.WriteAllText (Path.Combine (directory, $"{ addonFile.Id }.json"), addonFile.ToPrettyJson ());
                        }
                    });
                    //read all files from directory 
                    await task;
                }
            } catch (Exception e) {
                Console.WriteLine(e.ToPrettyJson());
            }
            var ids = idCache.Get(addOnID);
            if(ids == null) return null;
            var filesKeys = ids.Select(k => new AddOnFileKey{AddOnID = addOnID, FileID=k}).ToArray();
            var resultDict = await GetAddOnFilesAsync(filesKeys);
            AddOnFile[] resultList;
            if(!resultDict.TryGetValue(addOnID, out resultList)) {
                return null;
            }
            
            return resultList.OrderBy (f => f.FileDate).ToArray ();
            
            
            // if(!Directory.Exists(directory)) return null;
            // string[] files = Directory.GetFiles (directory, "*.json");
            // //Console.WriteLine($"files: {string.Join(", ", files)}");
            // if(files.Length == 0) return null;
            
            // var resultList = files.Select (fileName => {
            //     string content = System.IO.File.ReadAllText (fileName);
            //     //TODO: filter here ?
            //     var addonFile = JsonConvert.DeserializeObject<AddOnFile> (content, jsonSerializerSettings);
            //     return addonFile;
            // });
            // return resultList.OrderBy (f => f.FileDate).ToArray ();
        }

        async public Task<Dictionary<int, AddOnFile[]>> GetAddOnFilesAsync (AddOnFileKey[] addOnFileKeys) {
            var result = await client.GetAddOnFilesAsync (addOnFileKeys);
            if (result == null) return result;
            //TODO: var fileDict = result.filter();
            var task = Task.Run (() => {
                foreach (KeyValuePair<int, AddOnFile[]> entry in result) {
                    int addOnID = entry.Key;
                    AddOnFile[] addOnFiles = entry.Value;
                    idCache.Add(addOnID, addOnFiles);
                    foreach (var addonFile in addOnFiles) {
                        var directory = Path.Combine (OUTPUT, $"{ addOnID }", "files");
                        Directory.CreateDirectory (directory);
                        File.WriteAllText (Path.Combine (directory, $"{ addonFile.Id }.json"), addonFile.ToPrettyJson ());
                    }
                }
            });
            return result;
        }

        async public Task<byte[]> GetAddOnDumpAsync (int id) {
            return await client.GetAddOnDumpAsync (id);
        }
    }
}