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

for running the tool you need the [dotnet core](https://www.microsoft.com/net/core#linuxdebian) runtime (packagename: `dotnet-runtime-2.0`)

and if you want to build and compile on the same server you will also require the dotnet sdk (packagename: `dotnet-sdk-2.0`)

compile

```sh
dotnet restore
dotnet build
dotnet publish -c Release # copies dll into release folder

dotnet bin/Release/netcoreapp2.0/cursemeta.dll # run

dotnet run -c Release  # compile and run
```


## api enpoints

Example URLs using Wearable Backpacks for the project and file ids
the example host is `https://cursemeta.nikky.moe`

[GET `/api/feed`](https://cursemeta.nikky.moe/api/feed)

[GET `/api/feed/hourly`](https://cursemeta.nikky.moe/api/feed/hourly)

[GET `/api/feed/complete`](https://cursemeta.nikky.moe/api/feed/complete)

[GET `/api/addon`](https://cursemeta.nikky.moe/api/addon?mods=1&modpacks=true&texturepacks=0&worlds=false) WIP

omitting all filters will default them to `true`

- `bool` mods
- `bool` texturepacks
- `bool` worlds
- `bool` modpacks

property retriever may be in in the format `object.property.value`
may be used more than once
WARNING: uses reflection code, use at your own risk, has to be enabled in config

- `string` property

[GET `/api/addon/{addonID}`](https://cursemeta.nikky.moe/api/addon/257572)

[GET `/api/addon/{addonID}/description`](https://cursemeta.nikky.moe/api/addon/257572/desription)

[GET `/api/addon/{addonID}/files`](https://cursemeta.nikky.moe/api/addon/257572/files)

[GET `/api/addon/{addonID}/files/{fileID}`](https://cursemeta.nikky.moe/api/addon/257572/files/2382299)

[GET `/api/addon/{addonID}/files/{fileID}/changelog`](https://cursemeta.nikky.moe/api/addon/257572/files/2382299/changelog)

[POST `/api/modpack`](https://cursemeta.nikky.moe/api/addon/files)

```sh
curl -X POST \
  'https://cursemeta.nikky.moe/api/addon/files?p=id&p=downloadurl&p=addon.id&p=addon.name&p=addon.categorysection.name&p=addon.categorysection.packagetype&p=addon.categorysection.path' \
  -H 'content-type: application/json' \
  -d @ids.json

# ids.json
[
  {  
    "addonID":229316,
    "fileID":2233250
  },
  {  
    "addonID":223794,
    "fileID":2245762
  }
]
```


[POST `/api/modpack`](https://cursemeta.nikky.moe/api/manifest)

```sh
curl -X POST \
  'https://cursemeta.nikky.moe/api/manifest?p=id&p=downloadurl&p=addon.id&p=addon.name&p=addon.categorysection.name&p=addon.categorysection.packagetype&p=addon.categorysection.path' \
  -H 'content-type: application/json' \
  -d @manifest.json
```



## Config

example config

```yaml
cache:
  addons: addons
    # relative or absolute path
    # default = addons
  base: /home/user/.cache/cursemeta
    # is only used when `addonsCache` is relative
    # hidden default =
    #   win: %LocalAppdata%\cursemeta
    #  !win: $XDG_CACHE_HOME or $HOME/.cache/cursemeta
task:
  hourly:
    schedule: '*/30 * * * *' # see crontab
    enabled: true
  complete:
    schedule: '* */12 * * *' # see crontab
    enabled: true
  test:
    schedule: '*/1 * * * *' # see crontab
    enabled: false

reformat: true # default: true
  # re-serializes the config again
  # warning: also removes comments

```

## crontab

see [crontab guru](https://crontab.guru/#*/30_*_*_*_*) for reference
schedule string is required to be exactly 5 fields
