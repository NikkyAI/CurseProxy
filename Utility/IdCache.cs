using System;
using System.IO;
using System.Linq;
using System.Text;
using System.Collections.Generic;
using cursemeta.AddOnService;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;

namespace cursemeta.Utility {

    public class IdCache {
        public static readonly Lazy<IdCache> LazyIdCache = new Lazy<IdCache> (() => new IdCache ());
        private Dictionary<int, HashSet<int>> data = new Dictionary<int, HashSet<int>>();
        private string idFile = Path.Combine(Constants.CachePath, "ids.json");
        public IdCache() {
            if(File.Exists(idFile)) {
                string text = File.ReadAllText(idFile);
                data = JsonConvert.DeserializeObject<Dictionary<int, HashSet<int>>> (text);
            }
        }
        
        private static readonly JsonSerializerSettings settings =
            new JsonSerializerSettings
            {
                Formatting = Formatting.Indented
            };
            
        private bool save() {
            File.WriteAllText(idFile, data.ToPrettyJson());
            return true;
        }
        
        public void forceSave() {
            File.WriteAllText(idFile, data.ToPrettyJson());
        }
        
        public bool Add (int addonID) {
            if(data.TryAdd(addonID, new HashSet<int>())) {
                return save();
            }
            return false;
        }
        
        public bool Add (IEnumerable<int> addonIDs) {
            bool changed = false;
            foreach(var addonID in addonIDs) {
                changed |= data.TryAdd(addonID, new HashSet<int>());
            }
            if(changed) {
                return save();
            }
            return false;
        }
        
        public bool Add (IEnumerable<AddOn> addons) {
            return Add(addons.Select(f => f.Id));
        }

        public bool Add (int addonID, int fileID) {
            data.TryAdd(addonID, new HashSet<int>());
            // now the key exists
            
            HashSet<int> set;
            if(data.TryGetValue(addonID, out set)) {
                if(set.Add(fileID)) {
                   return save();
                }
                return false;
            } else {
                throw new Exception("unexpected state of HashSet data");
            }
        }
        
        public bool Add (int addonID, IEnumerable<AddOnFile> addonFile) {
            return Add(addonID, addonFile.Select(f => f.Id));
        }

        public bool Add (int addonID, IEnumerable<int> fileIDs) {
            data.TryAdd(addonID, new HashSet<int>());
            // now the key exists
            
            HashSet<int> set;
            if(data.TryGetValue(addonID, out set)) {
                bool changed = false;
                foreach(var fileID in fileIDs) {
                    changed |= set.Add(fileID);
                }
                if(changed) {
                   return save();
                }
                return false;
            } else {
                throw new Exception("unexpected state of HashSet data");
            }
        }
        
        public Dictionary<int, HashSet<int>> Get() {
            var ret = new Dictionary<int, HashSet<int>>();
            foreach(KeyValuePair<int, HashSet<int>> entry in data) {
                ret.Add(entry.Key, new HashSet<int>(entry.Value));
            }
            return ret;
        }
        
        public HashSet<int> Get(int addonID) {
            HashSet<int> original;
            if(data.TryGetValue(addonID, out original)) {
                return new HashSet<int>(original);
            }
            return null;
        }
    }
}