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


[GET `/api/addon`](http://localhost:5000/api/addon?mods=1&modpacks=true&texturepacks=0&worlds=false) WIP

omitting all filters will default them to `true`

- `bool` mods
- `bool` texturepacks
- `bool` worlds
- `bool` modpacks

property retriever may be in in the format `object.property.value`
may be used more than once
WARNING: uses reflection code, use at your own risk, has to be enabled in config

- `string` property

[GET `/api/addon/{addonID}`](http://localhost:5000/api/addon/257572)

[GET `/api/addon/{addonID}/description`](http://localhost:5000/api/addon/257572/desription)

[GET `/api/addon/{addonID}/files`](http://localhost:5000/api/addon/257572/files)

[GET `/api/addon/{addonID}/files/{fileID}`](http://localhost:5000/api/addon/257572/files/2382299)

[GET `/api/addon/{addonID}/files/{fileID}/changelog`](http://localhost:5000/api/addon/257572/files/2382299/changelog)


[GET `/api/update/sync`](http://localhost:5000/api/update/sync)

parameters:

- `bool` addons
- `bool` descriptions
- `bool` files
- `bool` changelogs (enabling WILL cripple performance)
- `bool` gc (testing effects on memory and performance)

## startup

WIP

at first start no ids are known to cursemeta, you need to load `/api/feed/complete` once and any other hidden addons and files you may want to expose

## cron jobs

WIP

currently no jobs are run regulary, so executing `api/feed/hourly` regularly and preferably in less than 1h intervals is recommended
also regularly getting all public ids from `/api/feed/complete`


## Config

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
output:
  files: files
  
  json: json
  
  base: /home/user/.cache/cursemeta/output
  
reformat: true # default: true
  # serializes the config again
  # will remove invalid properties and add defaults for nonexisting properties
  # warning: also removes comments

```