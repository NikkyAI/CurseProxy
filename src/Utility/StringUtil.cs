using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using Cursemeta.AddOnService;

namespace Cursemeta.Utility {

    public class StringUtil {
        private static Random random = new Random ();
        private static char[] alphabet = Enumerable.Range ('A', 26).Concat (Enumerable.Range ('a', 26)).Concat (Enumerable.Range ('0', 10)).Select (x => (char) x).ToArray ();
        public static string RandomString (int length) {
            return new string (Enumerable.Repeat (alphabet, length)
                .Select (s => s[random.Next (s.Length)]).ToArray ());
        }
    }
}