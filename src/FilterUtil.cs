using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Threading.Tasks;
using Cursemeta.AddOnService;

namespace Cursemeta {

    public static class FilterExtension {
        private static int mods_id = 6;
        private static int texture_packs_id = 12;
        private static int worlds_id = 17;
        private static int modpacks_id = 4471;
        
        public static IEnumerable<AddOn> filter(this IEnumerable<AddOn> source, bool mods, bool texture_packs, bool worlds, bool modpacks) {
            //Console.WriteLine($"filter {mods} {texture_packs} {worlds} {modpacks}");
            return source.Where (a => 
                (mods && (a.CategorySection.ID == mods_id)) || 
                (texture_packs && (a.CategorySection.ID == texture_packs_id)) || 
                (worlds && (a.CategorySection.ID == worlds_id)) ||
                (modpacks && (a.CategorySection.ID == modpacks_id))
            );
        }
    }
}