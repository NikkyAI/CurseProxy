cache addon and file IDs

filter for mods / modpacks / texturepacks etc in feed

reverse mapping using hardcoded properties only (reflection lookup is too slow)

CONFIG
  reverse mapping
  downloading / output

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

TODO: read file tree when adding output and downloading


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
