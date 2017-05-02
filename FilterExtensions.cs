using System;
using System.Collections.Generic;
using Alpacka.Meta.AddOnService;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;

namespace Alpacka.Meta
{
    public enum Filter {
        None,
        Default
    }
    
    public static class FilterExtensions
    {
        public static Filter parse(string value) {
            Filter filter;
            if (value != null) {
                if (!Enum.TryParse(value, true, out filter)) {
                    throw new ArgumentException($"{value} is not one of \n{Enum.GetValues(typeof(Filter)).ToPrettyYaml()}");
                }
            } else {
                filter = Filter.Default;
            }
            return filter;
        }
        
        private static readonly JsonSerializerSettings settings = new JsonSerializerSettings {
            Formatting = Formatting.Indented,
            MissingMemberHandling = MissingMemberHandling.Error,
            ContractResolver = new CamelCasePropertyNamesContractResolver(),
            NullValueHandling = NullValueHandling.Ignore            
        };
        
        public static string ToFilteredJson(this AddOn addon, Filter filter = Filter.Default) {
            switch(filter) {
                case Filter.None:
                    return addon.ToPrettyJson();
                case Filter.Default:
                    var tmp = addon.ToPrettyJson();
                    var sane = JsonConvert.DeserializeObject<AddOn_default>(tmp, settings);
                    return sane.ToPrettyJson();
                default:
                    throw new ArgumentException(filter.ToString());
            }
        }
        
        public static string ToFilteredJson(this AddOnFile file, Filter filter = Filter.Default) {
            switch(filter) {
                case Filter.None:
                    return file.ToPrettyJson();
                case Filter.Default:
                    var tmp = file.ToPrettyJson();
                    var sane = JsonConvert.DeserializeObject<AddOnFile_default>(tmp, settings);
                    return sane.ToPrettyJson();
                default:
                    throw new ArgumentException(filter.ToString());
            }
        }
        
        public static string ToFilteredJson(this AddOnFile[] fileList, Filter filter = Filter.Default) {
            switch(filter) {
                case Filter.None:
                    return fileList.ToPrettyJson();
                case Filter.Default:
                    var tmp = fileList.ToPrettyJson();
                    var sane = JsonConvert.DeserializeObject<AddOnFile[]>(tmp, settings);
                    return sane.ToPrettyJson();
                default:
                    throw new ArgumentException(filter.ToString());
            }
        }
        
        public static string ToFilteredJson(this List<AddOnFile> fileList, Filter filter = Filter.Default) {
            switch(filter) {
                case Filter.None:
                    return fileList.ToPrettyJson();
                case Filter.Default:
                    var tmp = fileList.ToPrettyJson();
                    var sane = JsonConvert.DeserializeObject<AddOnFile[]>(tmp, settings);
                    return sane.ToPrettyJson();
                default:
                    throw new ArgumentException(filter.ToString());
            }
        }
        
        public static string ToFilteredJson(this ProjectList list, Filter filter = Filter.Default) {
            switch(filter) {
                case Filter.None:
                    return list.ToPrettyJson();
                case Filter.Default:
                    var tmp = list.ToPrettyJson();
                    var sane = JsonConvert.DeserializeObject<ProjectList_default>(tmp, settings);
                    return sane.ToPrettyJson();
                default:
                    throw new ArgumentException(filter.ToString());
            }
        }
        
        private class AddOn_default {
            public AddOnAttachment[] Attachments { get; set; }
            public AddOnAuthor[] Authors { get; set; }
            public string AvatarUrl { get; set; }
            public AddOnCategory[] Categories { get; set; }
            public CategorySection CategorySection { get; set; }
            [JsonIgnore]
            public int CommentCount { get; set; }
            public int DefaultFileId { get; set; }
            public string DonationUrl { get; set; }
            [JsonIgnore]
            public double DownloadCount { get; set; }
            public string ExternalUrl { get; set; }
            public int GameId { get; set; }
            [JsonIgnore]
            public int GamePopularityRank { get; set; }
            public GameVersionLatestFile[] GameVersionLatestFiles { get; set; }
            public int IconId { get; set; }
            public int Id { get; set; }
            [JsonIgnore]
            public int InstallCount { get; set; }
            [JsonIgnore]
            public int IsFeatured { get; set; }
            public AddOnFile_default[] LatestFiles { get; set; }
            [JsonIgnore]
            public int Likes { get; set; }
            public string Name { get; set; }
            public PackageTypes PackageType { get; set; }
            [JsonIgnore]
            public double PopularityScore { get; set; }
            public string PrimaryAuthorName { get; set; }
            [JsonIgnore]
            public string PrimaryCategoryAvatarUrl { get; set; }
            public int PrimaryCategoryId { get; set; }
            [JsonIgnore]
            public string PrimaryCategoryName { get; set; }
            [JsonIgnore]
            public int Rating { get; set; }
            public ProjectStage Stage { get; set; }
            public ProjectStatus Status { get; set; }
            public string Summary { get; set; }
            public string WebSiteURL { get; set; }
        }
        
        public class AddOnFile_default : object
        {
            public int AlternateFileId { get; set; }
            public AddOnService.AddOnFileDependency[] Dependencies { get; set; }
            public string DownloadURL { get; set; }
            public System.DateTime FileDate { get; set; }
            public string FileName { get; set; }
            public string FileNameOnDisk { get; set; }
            public AddOnService.FileStatus FileStatus { get; set; }
            public string[] GameVersion { get; set; }
            public int Id { get; set; }
            public bool IsAlternate { get; set; }
            public bool IsAvailable { get; set; }
            [JsonIgnore]
            public AddOnService.AddOnModule[] Modules { get; set; }
            [JsonIgnore]
            public long PackageFingerprint { get; set; }
            public AddOnService.FileType ReleaseType { get; set; }
        }
        
        private class ProjectList_default
        {
            public long Timestamp { get; set; }
            public List<AddOn_default> Data { get; set; }
        }
    }
}
