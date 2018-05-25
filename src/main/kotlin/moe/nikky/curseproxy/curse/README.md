# curse api

ref: https://github.com/modmuss50/CAV2

```
So if we wanted to get all ids we would need to do
https://addons-v2.forgesvc.net/api/addon/search?gameId=432&sectionId=6&index=0
and increment the index by 1000 each time?
```

dnSpy can edit obsfucated code to add popups etc

https://cdn.discordapp.com/attachments/355447537278386187/449388307282132992/TwitchUI_2018-05-25_02-46-18.png

cache expiries

```cs
[JsonIgnore]
    public DateTime CacheDate

    [JsonIgnore]
    public bool IsCacheExpired
    {
      get
      {
        return this.CacheDate.AddDays(7.0) < DateTime.UtcNow;
      }
    }

    [JsonIgnore]
    public DateTime FilesCacheDate
    
    [JsonIgnore]
    public bool IsFilesCacheExpired
    {
      get
      {
        return this.FilesCacheDate.AddMinutes(10.0) < DateTime.UtcNow;
      }
    }

    public DateTime DescriptionCacheDate
    
    public bool IsDescriptionCacheExpired
    {
      get
      {
        return this.DescriptionCacheDate.AddMinutes(10.0) < DateTime.UtcNow;
      }
    }
```