using System;
using System.IO;

namespace Alpacka.Meta
{
    public static class Constants
    {
        public static string CachePath { get; } = Path.Combine(
            Environment.GetEnvironmentVariable("LocalAppData")
                ?? Environment.GetEnvironmentVariable("XDG_CACHE_HOME")
                ?? Path.Combine(Environment.GetEnvironmentVariable("HOME"), ".cache")
            , "alpacka.meta");
        
        public static string ConfigPath { get; } = Path.Combine(
            Environment.GetEnvironmentVariable("AppData")
                ?? Environment.GetEnvironmentVariable("XDG_CONFIG_HOME")
                ?? Path.Combine(Environment.GetEnvironmentVariable("HOME"), ".config")
            , "alpacka.meta");
    }
}
