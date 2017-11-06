using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Cursemeta.AddOnService;
using Cursemeta.LoginService;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Cursemeta {
    public class CacheClient {
        private static readonly Lazy<Task<AddOnServiceClient>> LazyAddonClient = new Lazy<Task<AddOnServiceClient>> (() => Authenticate ());
        public static readonly Lazy<CacheClient> LazyClient = new Lazy<CacheClient> (() => new CacheClient ());
        
        public readonly AddOnServiceClient client = LazyAddonClient.Value.Result;
        private readonly Cache cache = Cache.LazyCache.Value;

        private CacheClient () { }

        // private Dictionary<int, AddOn> addons = new Dictionary<int, AddOn> ();
        // private Dictionary<int, Dictionary<int, AddOnFile>> addonFiles = new Dictionary<int, Dictionary<int, AddOnFile>> ();
        // private Dictionary<int, Dictionary<int, string>> addonFileChangelogs = new Dictionary<int, Dictionary<int, string>> ();
        // private Dictionary<int, string> addonDescriptions = new Dictionary<int, string> ();

        private static async Task<AddOnServiceClient> Authenticate () {
            AddOnServiceClient client;
            var loginClient = new ClientLoginServiceClient (ClientLoginServiceClient.EndpointConfiguration.BinaryHttpsClientLoginServiceEndpoint);

            var deserializer = new DeserializerBuilder ()
                .IgnoreUnmatchedProperties ()
                .WithNamingConvention (new CamelCaseNamingConvention ())
                .Build ();

            var path = Path.Combine (Config.CONFIG, "curse.yaml");

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

        async public Task<AddOn> GetAddOnAsync (int id, bool cache = true, bool save = true) {
            if (cache) {
                var addon = this.cache.GetAddon (id);
                if (addon != null) return addon;
            }
            var result = await client.GetAddOnAsync (id);
            if (result == null) return result;
            //TODO: var addon = result.filter();
            var task = Task.Run (() => {
                this.cache.Add (result, save);
            });
            return result;
        }

        async public Task<AddOn[]> v2GetAddOnsAsync (int[] ids, bool cache = true, bool save = true) {
            if (cache) {
                var addons = this.cache.Get (ids);
                if (addons != null) return addons;
            }
            var list = new List<int> (ids);
            var split = list.split (16384);
            var result = new List<AddOn> ();
            Console.WriteLine ($"v2GetAddOnsAsync {ids.Length}");
            foreach (List<int> idList in split) {
                var partResult = await client.v2GetAddOnsAsync (idList.ToArray ());
                if (result == null) continue;
                //TODO: var addon = result.filter();
                var task = Task.Run (() => {
                    this.cache.Add (partResult, false);
                });
                result.AddRange (partResult);
            }
            this.cache.Save (save);

            return result.ToArray ();
        }

        async public Task<string> v2GetChangeLogAsync (int addonID, int fileID, bool cache = true, bool save = true) {
            if (cache) {
                var changelog = this.cache.GetChangelog (addonID, fileID);
                if (changelog != null) return changelog;
            }
            var result = await client.v2GetChangeLogAsync (addonID, fileID);
            if (result == null) return result;
            var task = Task.Run (() => {
                this.cache.Add (addonID, fileID, result, save);
            });
            return result;
        }

        async public Task<string> v2GetAddOnDescriptionAsync (int id, bool cache = true, bool save = true) {
            if (cache) {
                var description = this.cache.GetDescription (id);
                if (description != null) return description;
            }
            var result = await client.v2GetAddOnDescriptionAsync (id);
            if (result == null) return result;
            var task = Task.Run (() => {
                this.cache.Add (id, result, save);
            });
            return result;
        }

        async public Task<AddOnFile> GetAddOnFileAsync (int addonID, int fileID, bool cache = true, bool save = true) {
            if (cache) {
                var file = this.cache.Get (addonID, fileID);
                if (file != null) {
                    return file;
                }
            }
            var result = await client.GetAddOnFileAsync (addonID, fileID);
            if (result == null) return result;
            //TODO: var files = result.filter();
            var task = Task.Run (() => {
                this.cache.Add (addonID, result, save);
            });
            return result;
        }

        async public Task<AddOnFile[]> GetAllFilesForAddOnAsync (int addonID, bool cache = true, bool save = true) {
            if (cache) {
                var files = this.cache.GetFiles (addonID);
                if (files != null) return files;
            }
            try {
                var result = await client.GetAllFilesForAddOnAsync (addonID);
                if (result != null) {
                    var changed = this.cache.Add (addonID, result, save);
                }
                if(!cache) return result;
            } catch (Exception e) {
                Console.WriteLine (e.ToPrettyJson ());
            }
            var ids = this.cache.GetIDs (addonID);
            if (ids == null) return null;
            var filesKeys = ids.Select (k => new AddOnFileKey { AddOnID = addonID, FileID = k }).ToArray ();
            var resultDict = await GetAddOnFilesAsync (filesKeys, cache, save);
            AddOnFile[] resultList;
            if (!resultDict.TryGetValue (addonID, out resultList)) {
                return null;
            }

            return resultList.OrderBy (f => f.FileDate).ToArray ();
        }

        async public Task<Dictionary<int, AddOnFile[]>> GetAddOnFilesAsync (AddOnFileKey[] addOnFileKeys, bool cache = true, bool save = true) {
            if (cache) {
                var filesDict = this.cache.Get (addOnFileKeys);
                if (filesDict != null) return filesDict;
            }
            var result = await client.GetAddOnFilesAsync (addOnFileKeys);
            if (result == null) return result;
            //TODO: var fileDict = result.filter();
            var task = Task.Run (() => {
                foreach (var addonID in result.Keys) {
                    this.cache.Add (addonID, result[addonID], false);
                }
                this.cache.Save (save);
            });
            return result;
        }

        async public Task<byte[]> GetAddOnDumpAsync (int id) {
            return await client.GetAddOnDumpAsync (id);
        }
    }
}