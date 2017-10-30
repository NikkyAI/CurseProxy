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
        public static readonly Lazy<CacheClient> LazyCacheClient = new Lazy<CacheClient> (() => new CacheClient ());
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

        //TODO: implement better searchable index
        // async public Task<RepositoryMatch> GetRepositoryMatchFromSlugAsync(string gameSlug, string addonSlug)
        // {
        //     return await client.GetRepositoryMatchFromSlugAsync(gameSlug, addonSlug);
        // }

        // async public Task<string> GetAddOnDescriptionAsync(int id)
        // {
        //     return await client.GetAddOnDescriptionAsync(id);
        // }

        // async public Task<string> GetChangeLogAsync(int addonID, int fileID)
        // {
        //     var result = await client.GetChangeLogAsync(addonID, fileID);
        //     var task = Task.Run(() => {
        //         Console.WriteLine($"GetChangeLogAsync {addonID} {fileID} started");
        //         var directory = Path.Combine(OUTPUT, $"{addonID}", "files");
        //         Directory.CreateDirectory (directory);
        //         File.WriteAllText (Path.Combine (directory,  $"{fileID}.changelog.txt"), result);
        //         Console.WriteLine($"GetChangeLogAsync {addonID} {fileID} finished");
        //     });
        //     return result;
        // }

        // async public Task<AddOn[]> GetAddOnsAsync(int[] ids)
        // {
        //     var result = await client.GetAddOnsAsync(ids);
        //     var task = Task.Run(() => {
        //         Console.WriteLine($"GetAddOnsAsync {ids} started");
        //         foreach(var addon in result) {
        //             var directory = Path.Combine(OUTPUT, $"{ addon.Id }");
        //             Directory.CreateDirectory (directory);
        //             File.WriteAllText (Path.Combine (directory, "index.json"), addon.ToPrettyJson());
        //         }
        //         Console.WriteLine($"GetAddOnsAsync {ids} finished");
        //     });

        //     return result;
        // }

        // async public Task<FingerprintMatchResult> GetFingerprintMatchesAsync(long[] fingerprints)
        // {
        //     return await client.GetFingerprintMatchesAsync(fingerprints);
        // }

        // async public Task<FingerprintMatchResult> v2GetFingerprintMatchesAsync(long[] fingerprints)
        // {
        //     return await client.v2GetFingerprintMatchesAsync(fingerprints);
        // }

        // async public Task<FuzzyMatch[]> GetFuzzyMatchesAsync(int gameID, FolderFingerprint[] folderFingerprints)
        // {
        //     return await client.GetFuzzyMatchesAsync(gameID, folderFingerprints);
        // }

        // async public Task<DownloadToken> GetDownloadTokenAsync(int fileID)
        // {
        //     return await client.GetDownloadTokenAsync(fileID);
        // }

        // async public Task<DownloadToken> GetSecureDownloadTokenAsync(int fileID, int userID, bool hasPremium, int subscriptionToken)
        // {
        //     return await client.GetSecureDownloadTokenAsync(fileID, userID, hasPremium, subscriptionToken);
        // }

        // async public Task<ServiceResponseOfArrayOfSyncedGameInstanceeheogrl4> GetSyncProfileAsync()
        // {
        //     return await client.GetSyncProfileAsync();
        // }

        // async public Task<ServiceResponseOfSyncedGameInstanceeheogrl4> CreateSyncGroupAsync(string instanceName, int gameID, string computerName, string instanceGUID, string instanceLabel)
        // {
        //     return await client.CreateSyncGroupAsync(instanceName, gameID, computerName, instanceGUID, instanceLabel);
        // }

        // async public Task<ServiceResponse> JoinSyncGroupAsync(int instanceID, string computerName, string instanceGUID, string instanceLabel)
        // {
        //     return await client.JoinSyncGroupAsync(instanceID, computerName, instanceGUID, instanceLabel);
        // }

        // async public Task<ServiceResponse> LeaveSyncGroupAsync(int instanceID, int computerID, string instanceGUID)
        // {
        //     return await client.LeaveSyncGroupAsync(instanceID, computerID, instanceGUID);
        // }

        // async public Task<ServiceResponse> SaveSyncSnapshotAsync(int instanceID, SyncedAddon[] syncedAddons)
        // {
        //     return await client.SaveSyncSnapshotAsync(instanceID, syncedAddons);
        // }

        // async public Task<ServiceResponse> SaveSyncTransactionsAsync(int instanceID, SyncTransaction[] transactions)
        // {
        //     return await client.SaveSyncTransactionsAsync(instanceID, transactions);
        // }

        // async public Task<ServiceResponseOfArrayOfSavedGameeheogrl4> GetSavedGamesAsync()
        // {
        //     return await client.GetSavedGamesAsync();
        // }

        // async public Task<ServiceResponse> DeleteSavedGameAsync(int savedGameId)
        // {
        //     return await client.DeleteSavedGameAsync(savedGameId);
        // }

        // async public Task<ServiceResponse> DeleteSavedGameRevisionAsync(int savedGameId, int savedGameRevisionId)
        // {
        //     return await client.DeleteSavedGameRevisionAsync(savedGameId, savedGameRevisionId);
        // }

        // async public Task<ServiceResponse> SetSavedGameStatusAsync(int savedGameId, ESavedGameStatus status)
        // {
        //     return await client.SetSavedGameStatusAsync(savedGameId, status);
        // }

        // async public Task<ServiceResponse> SetSavedGameNameAsync(int savedGameId, string name)
        // {
        //     return await client.SetSavedGameNameAsync(savedGameId, name);
        // }

        // async public Task<ServiceResponse> SetSavedGameDescriptionAsync(int savedGameId, string description)
        // {
        //     return await client.SetSavedGameDescriptionAsync(savedGameId, description);
        // }

        // async public Task<ServiceResponse> SetSavedGameDefaultRevisionAsync(int savedGameId, int revisionId)
        // {
        //     return await client.SetSavedGameDefaultRevisionAsync(savedGameId, revisionId);
        // }

        // async public Task<ServiceResponse> SetSavedGameFavoriteRevisionAsync(int savedGameId, int revisionId)
        // {
        //     return await client.SetSavedGameFavoriteRevisionAsync(savedGameId, revisionId);
        // }

        // async public Task<ServiceResponseOfSavedGameConstraintseheogrl4> GetSavedGameConstraintsAsync()
        // {
        //     return await client.GetSavedGameConstraintsAsync();
        // }

        // async public Task<ServiceResponseOfESavedGameRestrictionLeveleheogrl4> GetSavedGameRestrictionLevelAsync()
        // {
        //     return await client.GetSavedGameRestrictionLevelAsync();
        // }

        // async public Task<ServiceResponse> UploadAvailableForUserAsync()
        // {
        //     return await client.UploadAvailableForUserAsync();
        // }

        // async public Task<string> ResetAllAddonCacheAsync()
        // {
        //     return await client.ResetAllAddonCacheAsync();
        // }

        // async public Task<string> ResetSingleAddonCacheAsync(int id)
        // {
        //     return await client.ResetSingleAddonCacheAsync(id);
        // }

        // async public Task<ServiceResponse> SetSavedGameRestrictionLevelAsync(ESavedGameRestrictionLevel restrictionLevel)
        // {
        //     return await client.SetSavedGameRestrictionLevelAsync(restrictionLevel);
        // }

        // async public Task<string> HealthCheckAsync()
        // {
        //     return await client.HealthCheckAsync();
        // }

        // async public Task<string> CacheHealthCheckAsync()
        // {
        //     return await client.CacheHealthCheckAsync();
        // }
    }
}