move logic to in-memory database https://docs.microsoft.com/en-us/aspnet/core/tutorials/web-api-vsc#create-the-database-context

cache addon and file IDs

filter for mods / modpacks / texturepacks etc in feed

?mods=1&modpacks=true&texturepacks=0&worlds=false
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

parse existing data on startup into cache 
OR execute update on ids

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




TODO: reimplement generating these files ?
  * [complete.json](https://cursemeta.nikky.moe/complete.json)
  * [complete.json.bz2](https://cursemeta.nikky.moe/complete.json.bz2)
  * [mods.json](https://cursemeta.nikky.moe/mods.json)
  * [mods.json.bz2](https://cursemeta.nikky.moe/mods.json.bz2)
  * [modpacks.json](https://cursemeta.nikky.moe/modpacks.json)
  * [modpacks.json.bz2](https://cursemeta.nikky.moe/modpacks.json.bz2)

TODO: readd file tree when adding output and downloading


### tree
```
cursemeta.nikky.moe/
├── addon/
│   ├── $addon_id$/
│   │   ├── description.html
│   │   ├── files/
│   │   │   ├── $file_id$.changelog.html
│   │   │   ├── $file_id$.json
│   │   │   │   ...
│   │   │   ├── $file_id$.changelog.html
│   │   │   ├── $file_id$.json
│   │   │   └── index.json
│   │   └── index.json
│   └── $addon_id$/
│       ├── description.html
│       ├── files/
│       │   ├── $file_id$.changelog.html
│       │   ├── $file_id$.json
│       │   │   ...
│       │   └── index.json
│       └── index.json
├── complete.json
├── complete.json.bz2
├── mods.json
├── mods.json.bz2
├── modpacks.json
└── modpacks.json.bz2
```


`/api/update/sync`


parameters:

- `bool` addons
- `bool` descriptions
- `bool` files
- `bool` changelogs (enabling WILL cripple performance)
- `bool` gc (testing effects on memory and performance) 