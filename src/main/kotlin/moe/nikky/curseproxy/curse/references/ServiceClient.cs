// Decompiled with JetBrains decompiler
// Type: Curse.Radium.Addons.ServiceClient.AddonServiceClient
// Assembly: Curse.Radium.Addons.ServiceClient, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null
// MVID: 14BDD679-7DBE-4016-BD2E-165A6693E414
// Assembly location: C:\Projects\Curse_Twitch\Twitch\Bin\Curse.Radium.Addons.ServiceClient.dll
using Curse.ServiceClients;
using System;
using System.Collections.Generic;
using Twitch.Elerium.AddonClientService.Data;
using Twitch.Elerium.AddonClientService.Data.Contracts;
using Twitch.Elerium.AddonClientService.Data.Enums;
using Twitch.Elerium.AddonClientService.Data.Models;
namespace Curse.Radium.Addons.ServiceClient
{
  public class AddonServiceClient : BaseWebServiceClient
  {
    private static Lazy<AddonServiceClient> _lazyInstance = new Lazy<AddonServiceClient>(new Func<AddonServiceClient>((object) AddonServiceClient.\u003C\u003Ec.\u003C\u003E9, __methodptr(\u003C\u002Ecctor\u003Eb__43_0)));
    private static string _baseUrl;
    public static AddonServiceClient Instance { get; private set; }
    public static void RegisterClient(string baseUrl)
    {
      AddonServiceClient._baseUrl = string.Format("https://{0}", (object) baseUrl);
      AddonServiceClient.Instance = AddonServiceClient._lazyInstance.get_Value();
    }
    public AddonServiceClient(string url)
      : base(url)
    {
    }
    public ServiceResponse<Addon> GetAddon(int addonId)
    {
      return this.Get<Addon>(string.Format("api/addon/{0}", (object) addonId));
    }
    public ServiceResponse<List<Addon>> GetAddons(int[] addonIds)
    {
      return this.Post<List<Addon>>("api/addon", (object) addonIds);
    }

    public enum AddonSortMethod
    {
      Featured,
      Popularity,
      LastUpdated,
      Name,
      Author,
      TotalDownloads,
      Category,
      GameVersion,
    }

    public ServiceResponse<List<Addon>> GetAddonsByCriteria(int gameId, int sectionId = -1, int categoryId = -1, AddonSortMethod sort = AddonSortMethod.Featured, bool isSortDescending = true, string gameVersion = null, int index = 0, int pageSize = 50, string searchFilter = null)
    {
      return this.Get<List<Addon>>(string.Format("api/addon/search?gameId={0}&sectionId={1}&categoryId={2}&gameVersion={3}&index={4}&pageSize={5}&searchFilter={6}&sort={7}&sortDescending={8}", (object) gameId, (object) sectionId, (object) categoryId, (object) gameVersion, (object) index, (object) pageSize, (object) searchFilter, (object) sort, (object) isSortDescending.ToString().ToLower()));
    }
    public ServiceResponse<string> GetAddonDescription(int addonId)
    {
      return this.Get<string>(string.Format("api/addon/{0}/description", (object) addonId));
    }
    public ServiceResponse<string> GetAddonChangelog(int addonId, int fileId)
    {
      return this.Get<string>(string.Format("api/addon/{0}/file/{1}/changelog", (object) addonId, (object) fileId));
    }
    public ServiceResponse<AddonFile> GetAddonFile(int addonId, int fileId)
    {
      return this.Get<AddonFile>(string.Format("api/addon/{0}/file/{1}", (object) addonId, (object) fileId));
    }
    public ServiceResponse<List<AddonFile>> GetAddonFiles(int addonId)
    {
      return this.Get<List<AddonFile>>(string.Format("api/addon/{0}/files", (object) addonId));
    }
    public ServiceResponse<Dictionary<int, List<AddonFile>>> GetAddonFiles(AddonFileKey[] keys)
    {
      return this.Post<Dictionary<int, List<AddonFile>>>("api/addon/files");
    }
    public ServiceResponse<RepositoryMatch> GetRepositoryMatchFromSlug(string gameSlug, string addonSlug)
    {
      return this.Get<RepositoryMatch>(string.Format("api/addon/slug?gameSlug={0}&addonSlug={1}", (object) gameSlug, (object) addonSlug));
    }
    public ServiceResponse<Dictionary<FeaturedAddonType, List<Addon>>> GetFeaturedAddons(int gameId, int featuredCount = 6, int popularCount = 14, int updatedCount = 14, int[] addonIds = null)
    {
      return this.Post<Dictionary<FeaturedAddonType, List<Addon>>>("api/addon/featured", (object) new GetFeaturedAddonsContract()
      {
        GameId = gameId,
        FeaturedCount = featuredCount,
        PopularCount = popularCount,
        UpdatedCount = updatedCount,
        ExcludedAddons = addonIds
      });
    }
    public ServiceResponse<DateTime> GetAddonsDatabaseTimestamp()
    {
      return this.Get<DateTime>("api/addon/timestamp");
    }
    public ServiceResponse<List<SyncedGameInstance>> GetSyncProfile()
    {
      return this.Get<List<SyncedGameInstance>>("api/addonsync");
    }
    public ServiceResponse<JoinSyncGroupStatus> JoinSyncGroup(int instanceId, string computerName, string instanceGuid, string instanceLabel)
    {
      return this.Post<JoinSyncGroupStatus>("api/addonsync/group/join", (object) new JoinSyncGroupContract()
      {
        InstanceId = instanceId,
        ComputerName = computerName,
        InstanceGuid = instanceGuid,
        InstanceLabel = instanceLabel
      });
    }
    public ServiceResponse<LeaveSyncGroupStatus> LeaveSyncGroup(int instanceId, int computerId, string instanceGuid)
    {
      return this.Post<LeaveSyncGroupStatus>("api/addonsync/group/leave", (object) new LeaveSyncGroupContract()
      {
        InstanceId = instanceId,
        ComputerId = computerId,
        InstanceGuid = instanceGuid
      });
    }
    public ServiceResponse<CreateSyncGroupResult> CreateSyncGroup(string instanceName, int gameId, string computerName, string instanceGuid, string instanceLabel)
    {
      return this.Post<CreateSyncGroupResult>("api/addonsync/group/create", (object) new CreateSyncGroupContract()
      {
        InstanceName = instanceName,
        GameId = gameId,
        ComputerName = computerName,
        InstanceGuid = instanceGuid,
        InstanceLabel = instanceLabel
      });
    }
    public ServiceResponse<SaveSyncSnapshotStatus> SaveSyncSnapshot(int instanceId, SyncedAddon[] addons)
    {
      return this.Post<SaveSyncSnapshotStatus>("api/addonsync/snapshot", (object) new SaveSyncSnapshotContract()
      {
        InstanceId = instanceId,
        Addons = addons
      });
    }
    public ServiceResponse<SaveSyncTransactionsStatus> SaveSyncTransactions(int instanceId, SyncTransaction[] transactions)
    {
      return this.Post<SaveSyncTransactionsStatus>("api/addonsync/transactions", (object) new SaveSyncTransactionsContract()
      {
        InstanceId = instanceId,
        Transactions = transactions
      });
    }
    public ServiceResponse<SaveUserBackupStatus> SaveUserBackup(int instanceId, long fingerprint, int screenWidth, int screenHeight, byte[] data)
    {
      return this.Post<SaveUserBackupStatus>("api/addonsync/backup", (object) new SaveUserBackupContract()
      {
        InstanceId = instanceId,
        Fingerprint = fingerprint,
        ScreenWidth = screenWidth,
        ScreenHeight = screenHeight,
        FileBytes = data
      });
    }
    public ServiceResponse<FingerprintMatchResult> GetFingerprintMatches(long[] fingerprints)
    {
      return this.Post<FingerprintMatchResult>("/api/fingerprint", (object) fingerprints);
    }
    public ServiceResponse<List<FuzzyMatch>> GetFuzzyMatches(int gameId, FolderFingerprint[] fingerprints)
    {
      return this.Post<List<FuzzyMatch>>("/api/fingerprint/fuzzy", (object) new GetFuzzyMatchesContract()
      {
        GameId = gameId,
        Fingerprints = fingerprints
      });
    }
    public ServiceResponse<MinecraftModLoaderVersion> GetModloader(string key)
    {
      return this.Get<MinecraftModLoaderVersion>(string.Format("/api/minecraft/modloader/{0}", (object) key));
    }
    public ServiceResponse<List<MinecraftModLoaderIndex>> GetModloaders()
    {
      return this.Get<List<MinecraftModLoaderIndex>>("/api/minecraft/modloader");
    }
    public ServiceResponse<List<MinecraftModLoaderIndex>> GetModloadersForGameVersion(string gameVersion)
    {
      return this.Get<List<MinecraftModLoaderIndex>>(string.Format("/api/minecraft/modloader/{0}", (object) gameVersion));
    }
    public ServiceResponse<DateTime> GetModloadersDatabaseTimestamp()
    {
      return this.Get<DateTime>("api/minecraft/modloader/timestamp");
    }
    public ServiceResponse<List<MinecraftGameVersion>> GetMinecraftVersions()
    {
      return this.Get<List<MinecraftGameVersion>>("/api/minecraft/version");
    }
    public ServiceResponse<List<MinecraftGameVersion>> GetMinecraftVersion(string gameVersion)
    {
      return this.Get<List<MinecraftGameVersion>>(string.Format("/api/minecraft/version/{0}", (object) gameVersion));
    }
    public ServiceResponse<DateTime> GetMinecraftVersionsDatabaseTimestamp()
    {
      return this.Get<DateTime>("api/minecraft/version/timestamp");
    }
    public ServiceResponse<Game> GetGame(int gameId)
    {
      return this.Get<Game>(string.Format("api/game/{0}", (object) gameId));
    }
    public ServiceResponse<List<Game>> GetGames(bool supportsAddons = false)
    {
      return this.Get<List<Game>>(supportsAddons ? string.Format("api/game?supportsAddons={0}", (object) supportsAddons) : "api/game");
    }
    public ServiceResponse<DateTime> GetGameDatabaseTimestamp()
    {
      return this.Get<DateTime>("api/game/timestamp");
    }
    public ServiceResponse<Category> GetCategoryByID(int categoryId)
    {
      return this.Get<Category>(string.Format("api/category/{0}", (object) categoryId));
    }
    public ServiceResponse<Category> GetCategoryBySlug(string slug)
    {
      return this.Get<Category>(string.Format("api/category?slug={0}", (object) slug));
    }
    public ServiceResponse<List<Category>> GetCategories()
    {
      return this.Get<List<Category>>("api/category");
    }
    public ServiceResponse<DateTime> GetCategoryDatabaseTimestamp()
    {
      return this.Get<DateTime>("api/category/timestamp");
    }
    public ServiceResponse<List<Category>> GetCategoriesBySection(int sectionId)
    {
      return this.Get<List<Category>>(string.Format("api/category/section/{0}", (object) sectionId));
    }
  }
}