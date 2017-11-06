using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Cursemeta.AddOnService;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using Cursemeta.Configs;

namespace Cursemeta {

    public class Cache {
        public static readonly Lazy<Cache> LazyCache = new Lazy<Cache> (() => new Cache ());
        private CacheConfig cacheConfig = Config.instance.Value.cache;

        private string idPath = Path.Combine (Config.instance.Value.cache.BasePath, "ids.json");

        // private readonly string addonFilesPath = Path.Combine (Constants.CachePath, "addonFiles.json");
        // private readonly string addonDescriptionsPath = Path.Combine (Constants.CachePath, "addonDescriptions.json");
        // private readonly string addonFileChangelogsPath = Path.Combine (Constants.CachePath, "addonDescriptions.json");

        // public bool verbose { get; set; }

        // private ConcurrentDictionary<int, AddOn> addonsCache = new ConcurrentDictionary<int, AddOn> ();
        // private ConcurrentDictionary<int, ConcurrentDictionary<int, AddOnFile>> addonFilesCache = new ConcurrentDictionary<int, ConcurrentDictionary<int, AddOnFile>> ();
        // private ConcurrentDictionary<int, ConcurrentDictionary<int, string>> addonFileChangelogsCache = new ConcurrentDictionary<int, ConcurrentDictionary<int, string>> ();
        // private ConcurrentDictionary<int, string> addonDescriptionsCache = new ConcurrentDictionary<int, string> ();

        private ConcurrentDictionary<int, ConcurrentDictionary<int, byte>> idCache = new ConcurrentDictionary<int, ConcurrentDictionary<int, byte>> ();

        private string GetAddonDir (int addonID) {
            return Path.Combine (cacheConfig.AddonsPath, $"{addonID}");
        }
        private string GetAddonPath (int addonID) {
            return Path.Combine (cacheConfig.AddonsPath, $"{addonID}", "index.json");
        }
        private string GetDescriptionPath (int addonID) {
            return Path.Combine (cacheConfig.AddonsPath, $"{addonID}", "description.html");
        }
        private string GetFileDir (int addonID) {
            return Path.Combine (cacheConfig.AddonsPath, $"{addonID}", "files");
        }
        private string GetFilePath (int addonID, int fileID) {
            return Path.Combine (cacheConfig.AddonsPath, $"{addonID}", "files", $"{fileID}.json");
        }
        private string GetChangelogPath (int addonID, int fileID) {
            return Path.Combine (cacheConfig.AddonsPath, $"{addonID}", "files", $"{fileID}.changelog.html");
        }

        public Cache () {
            if (File.Exists (idPath)) {
                Console.WriteLine ($"reading {idPath}");
                string text = File.ReadAllText (idPath);
                var ids = text.FromJson<Dictionary<int, IEnumerable<int>> > ();
                
                var batchSize = 1000;
                var batches = ids.Keys.ToList ().split (batchSize);
                var timer = new Stopwatch ();
                timer.Start ();
                int k = 0; //TODO: group with index as key ?
                int k_all = batches.Count ();
                Console.WriteLine ($"parsing {idPath} im {k_all} batches of {batchSize}");
                foreach (var batch in batches) {
                    var tasks = Task.WhenAll (batch.Select (
                        (id) => {
                            return Task.Run (() => loadAddon (id, ids[id]));
                        }
                    ));
                    Console.WriteLine ($"loading [{++k} / {k_all}]");
                    tasks.Wait ();
                    //await Task.Delay(TimeSpan.FromSeconds(0.1)); //testing if this causes problems or not
                }

                //TODO: add retrying

                timer.Stop ();
                Console.WriteLine ($"all targets were processed in '{ timer.Elapsed }'");
            } else {
                Console.Error.WriteLine($"cannot find file {idPath}");
            }
        }

        private void loadAddon (int addonID, IEnumerable<int> files) {
            // Console.WriteLine ($"parsing {addonID}");
            var addonPath = GetAddonPath(addonID);
            var addonIndex = Path.Combine (addonPath, "index.json");
            var addon = addonIndex.FromJsonFile<AddOn> ();
            if (addon != null) {
                // addonsCache[addon.Id] = addon;
            }
            idCache[addonID] = new ConcurrentDictionary<int, byte> ();
            // var addonDescription = Path.Combine (addonPath, "description.html");
            // var description = addonDescription.FromTextFile ();
            // if (description != null) {
            //     addonDescriptionsCache[addonID] = description;
            // }
            var fileBasePath = Path.Combine (addonPath, "files");
            if (Directory.Exists (fileBasePath)) {
                //addonFilesCache[addonID] = new ConcurrentDictionary<int, AddOnFile> ();
                //addonFileChangelogsCache[addonID] = new ConcurrentDictionary<int, string> ();
                foreach (int fileID in files) {
                    var filePath = Path.Combine (fileBasePath, $"{fileID}.json");
                    var file = filePath.FromJsonFile<AddOnFile> ();
                    if (file != null) {
                        // addonFilesCache[addonID][fileID] = file;
                        idCache[addonID][fileID] = 1;
                    }
                    // var changelogPath = Path.Combine (fileBasePath, $"{fileID}.changelog.html");
                    // var changelog = changelogPath.FromTextFile ();
                    // if (changelog != null) {
                    //     addonFileChangelogsCache[addon.Id][fileID] = changelog;
                    // }
                }
            }
        }

        private static readonly JsonSerializerSettings settings =
            new JsonSerializerSettings {
                Formatting = Formatting.Indented
            };

        public bool Save (bool write = true) {
            if (write) {
                // TODO: save ID mapping
                // File.WriteAllText (idFile, ids.ToPrettyJson ());
                File.WriteAllText (idPath, GetIDs ().ToPrettyJson ());
            }
            //TODO: move into if write block
            // ReverseMapping.ReMap (addonsCache.Values);

            return true;
        }

        public bool Add (AddOn addon, bool save = true) {
            Task.Run (() => {
                var directory = GetAddonDir (addon.Id);
                Directory.CreateDirectory (directory);
                var path = GetAddonPath (addon.Id);
                File.WriteAllText (path, addon.ToPrettyJson ());
            });

            // update addon in cache
            var changed = idCache.UpdateOrAdd (addon.Id, new ConcurrentDictionary<int, byte> ());

            if (changed) {
                Save (save);
            }
            return changed;
        }

        public AddOn GetAddon (int addonID) {
            if (idCache.ContainsKey (addonID)) {
                var path = GetAddonPath (addonID);
                return path.FromJsonFile<AddOn> ();
            }
            return null;
        }

        public bool Add (int addonID, string description, bool save = true) {
            Task.Run (() => {
                var directory = GetAddonDir (addonID);
                Directory.CreateDirectory (directory);
                File.WriteAllText (GetDescriptionPath (addonID), description);
            });

            var changed = idCache.UpdateOrAdd (addonID, new ConcurrentDictionary<int, byte> ());

            if (changed) {
                Save (save);
            }
            return changed;
        }

        public string GetDescription (int addonID) {
            if (idCache.ContainsKey (addonID)) {
                var path = GetDescriptionPath (addonID);
                return path.FromTextFile ();
            }
            return null;
        }

        public bool Add (IEnumerable<AddOn> addons, bool save = true) {
            Task.Run (() => {
                foreach (var addon in addons) {
                    var directory = GetAddonDir (addon.Id);
                    Directory.CreateDirectory (directory);
                    var path = GetAddonPath (addon.Id);
                    File.WriteAllText (path, addon.ToPrettyJson ());
                }
            });

            bool changed = false;
            foreach (var addon in addons) {
                // changed |= addonsCache.UpdateOrAdd (addon.Id, addon);
                changed |= idCache.TryAdd (addon.Id, new ConcurrentDictionary<int, byte> ());
            }
            if (changed) {
                return Save (save);
            }
            return false;
        }

        public AddOn[] Get (int[] addonIDs) {
            if (addonIDs.All (k => idCache.ContainsKey (k))) {
                var addons = addonIDs.Select (addonID => {
                    var path = GetAddonPath (addonID);
                    return path.FromJsonFile<AddOn> ();
                });
                if (addons.All (a => a != null))
                    return addons.ToArray ();
            }
            return null;
        }

        public bool Add (int addonID, AddOnFile addonFile, bool save = true) {
            idCache.TryAdd (addonID, new ConcurrentDictionary<int, byte> ());
            // now the key exists

            Task.Run (() => {
                var directory = GetFileDir (addonID);
                Directory.CreateDirectory (directory);
                var path = GetFilePath (addonID, addonFile.Id);
                File.WriteAllText (path, addonFile.ToPrettyJson ());
            });

            // update file in cache
            var changed = idCache[addonID].UpdateOrAdd (addonFile.Id, (byte) 1);

            if (changed) {
                Save (save);
            }
            return changed;
        }

        public AddOnFile Get (int addonID, int fileID) {
            if (idCache.ContainsKey (addonID)) {
                var files = idCache[addonID];
                if (files.ContainsKey (fileID)) {
                    var path = GetFilePath (addonID, fileID);
                    return path.FromJsonFile<AddOnFile> ();
                }
            }
            return null;
        }

        // for changelogs
        public bool Add (int addonID, int fileID, string changelog, bool save = true) {
            idCache.TryAdd (addonID, new ConcurrentDictionary<int, byte> ());
            // now the key exists

            Task.Run (() => {
                var directory = GetFileDir (addonID);
                Directory.CreateDirectory (directory);
                var path = GetChangelogPath (addonID, fileID);
                File.WriteAllText (path, changelog);
            });

            var changed = idCache[addonID].UpdateOrAdd (fileID, (byte) 1);

            if (changed) {
                Save (save);
            }
            return changed;
        }

        public string GetChangelog (int addonID, int fileID) {
            if (idCache.ContainsKey (addonID)) {
                var changelogs = idCache[addonID];
                if (changelogs.ContainsKey (fileID)) {
                    var path = GetChangelogPath (addonID, fileID);
                    return path.FromTextFile ();
                }
            }
            return null;
        }

        public bool Add (int addonID, IEnumerable<AddOnFile> files, bool save = true) {
            idCache.TryAdd (addonID, new ConcurrentDictionary<int, byte> ());
            // now the key exists

            Task.Run (() => {
                var directory = GetFileDir (addonID);
                Directory.CreateDirectory (directory);
                foreach (var file in files) {
                    var path = GetFilePath (addonID, file.Id);
                    File.WriteAllText (path, file.ToPrettyJson ());
                }
            });

            bool changed = false;
            foreach (var file in files) {
                // changed |= addonFilesCache[addonID].UpdateOrAdd (file.Id, file);
                changed |= idCache[addonID].UpdateOrAdd (file.Id, (byte) 1);
            }
            if (changed) {
                Save (save);
            }
            return changed;
        }

        public AddOnFile[] GetFiles (int addonID) {
            if (idCache.ContainsKey (addonID)) {
                var files = idCache[addonID].Keys.Select (fileID => {
                    var path = GetFilePath (addonID, fileID);
                    return path.FromJsonFile<AddOnFile> ();
                });
                if (files.All (a => a != null))
                    return files.OrderBy (f => f.FileDate).ToArray ();
            }
            return null;
        }

        public Dictionary<int, AddOnFile[]> Get (AddOnFileKey[] addonFileKeys) {
            var mapping = addonFileKeys.GroupBy (k => k.AddOnID).ToDictionary (k => k.Key, k => k.Select (fk => fk.FileID));

            if (mapping.Keys.All (addonID => {
                    if (!idCache.ContainsKey (addonID)) return false;
                    return mapping[addonID].All (fileID => idCache[addonID].ContainsKey (fileID));
                })) {
                var ret = new Dictionary<int, AddOnFile[]> ();
                foreach (var addonID in mapping.Keys) {
                    var files = idCache[addonID].Keys.Select (fileID => {
                        var path = GetFilePath (addonID, fileID);
                        return path.FromJsonFile<AddOnFile> ();
                    });
                    if (files.All (a => a != null))
                        ret[addonID] = files.OrderBy (f => f.FileDate).ToArray ();
                    else return null;
                }
                return ret;
            }
            return null;
        }

        // get all ids
        public Dictionary<int, IEnumerable<int>> GetIDs () {
            var ret = idCache.ToDictionary (a => a.Key, a => a.Value.Select (f => f.Key));
            return ret;
        }

        // get file ids of specific addon
        public IEnumerable<int> GetIDs (int addonID) {
            ConcurrentDictionary<int, byte> files;
            if (idCache.TryGetValue (addonID, out files)) {
                var ret = files.Select (f => f.Key);
                return ret;
            }
            return null;
        }
    }
}