move logic to in-memory database https://docs.microsoft.com/en-us/aspnet/core/tutorials/web-api-vsc#create-the-database-context

cache addon and file IDs

filter for mods / modpacks / texturepacks etc in feed

?mods=1&&texturepacks=0&worlds=true&modpacks=false
  {
    "gameID": 432,
    "id": 6,
    "initialInclusionPattern": ".",
    "name": "Mods",
    "packageType": "mod",
    "path": "mods"
  },
  {
    "extraIncludePattern": "([^\\/\\\\]+\\.zip)$",
    "gameID": 432,
    "id": 12,
    "initialInclusionPattern": "([^\\/\\\\]+\\.zip)$",
    "name": "Texture Packs",
    "packageType": "singleFile",
    "path": "resourcepacks"
  },
  {
    "gameID": 432,
    "id": 17,
    "initialInclusionPattern": ".",
    "name": "Worlds",
    "packageType": "folder",
    "path": "saves"
  },
  {
    "gameID": 432,
    "id": 4471,
    "initialInclusionPattern": "$^",
    "name": "Modpacks",
    "packageType": "modPack",
    "path": "downloads"
  },

reverse mapping
depends on: in memeory data cache
  cache.write(id, data)
  AddOn cache.get(id)
  bool cache.exists()

cacheClient: default: cache=true
  allow disabling cache lookup

CONFIG
  reverse_mapping
  downloading, etc


POST register

* cronjobs:
  * 30m-1h pull hourly and process
  * 1d-2d pull complete and process
  * concat file/*.json to file/index.json
