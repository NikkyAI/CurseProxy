namespace Cursemeta.AddOnService {
    
    public class Manifest {
        
        public Minecraft Minecraft;
        public string ManifestType;
        public int ManifestVersion;
        public string Name;
        public string Version;
        public string Author;
        public ModpackFile[] Files;
    }
    
    public class Minecraft {
        public string Version;
        public Modloader[] Modloaders;
    }
    
    public class Modloader {
        public string Id;
        public bool Primary;
    }
    
    public class ModpackFile {
        public int ProjectID;
        public int FileID;
        public bool Required;
    }
}