# CurseMeta

## About

Curse Meta is a tool to proxy the `SOAP API` of curse and make the data available as `REST` and in simple json files

### Features
- get info and urls of **deleted** files (as long as you have the project and file id)
- get all addons from the curse hourly and complete feed
- filter to get mods or modpacks only (WIP)

You just need to understand the [api endpoints](#api-enpoints) and json files

Files are hosted on https://cursemeta.nikky.moe/ and updated approximately every hour (assuming nothing goes horribly wrong)  
Files are also mirrored to https://github.com/NikkyAI/alpacka-meta-file every 12 hours

This tool does not interact with the curse website at all so any changes in html cannot break it

## Setup

for running the tool you need the [dotnet core](https://www.microsoft.com/net/core#linuxdebian) runtime (packagename: `dotnet-sdk-2.0`)

and if you want to build and compile on the same server you will also require the dotnet sdk (packagename: `dotnet-sdk`)

```
git clone git@github.com:NikkyAI/cursemeta.git
cd cursemeta
dotnet restore
dotnet build
dotnet publish -c release
dotnet run -c Release
```

## api enpoints

Example URLs using Wearable Backpacks for the project and file ids
the example host is `http://localhost:5000`

https://www.curseforge.com/projects/257572/

[GET `/api/feed`](http://localhost:5000/api/feed)

[GET `/api/feed/hourly`](http://localhost:5000/api/feed/hourly)

[GET `/api/feed/complete`](http://localhost:5000/api/feed/complete)

[GET `/api/addon/`](http://localhost:5000/api/addon) WIP

[GET `/api/addon/{addonID}`](http://localhost:5000/api/addon/257572)

[GET `/api/addon/{addonID}/description`](http://localhost:5000/api/addon/257572/desription)

[GET `/api/addon/{addonID}/files`](http://localhost:5000/api/addon/257572/files)

[GET `/api/addon/{addonID}/files/{fileID}`](http://localhost:5000/api/addon/257572/files/2382299)

[GET `/api/addon/{addonID}/files/{fileID}/changelog`](http://localhost:5000/api/addon/257572/files/2382299/changelog)

  * [complete.json](https://cursemeta.nikky.moe/complete.json)
  * [complete.json.bz2](https://cursemeta.nikky.moe/complete.json.bz2)
  * [mods.json](https://cursemeta.nikky.moe/mods.json)
  * [mods.json.bz2](https://cursemeta.nikky.moe/mods.json.bz2)
  * [modpacks.json](https://cursemeta.nikky.moe/modpacks.json)
  * [modpacks.json.bz2](https://cursemeta.nikky.moe/modpacks.json.bz2)


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