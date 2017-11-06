using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using cursemeta.AddOnService;

namespace Cursemeta {

    public class ReverseMapping {
        private static string mappingsDir = Path.Combine (Constants.CachePath, "mappings");
        
        public static object ReMap (IEnumerable<AddOn> addons) {
            if(!Directory.Exists(mappingsDir))
                Directory.CreateDirectory(mappingsDir);
            
            string file = Path.Combine(mappingsDir, "featured.json");
            
            var mapping = addons.GroupBy(a => a.IsFeatured).ToDictionary(a => a.Key, a => a.ToList());;
            File.WriteAllText (file, mapping.ToPrettyJson ());

            //TODO: trigger reverse mappings

            return mapping;
        }

    }
}