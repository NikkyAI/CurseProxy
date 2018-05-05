# CurseProxy

## About

CurseProxy is a tool to proxy the `SOAP API` of curse(forge) and make the data available as `REST` api

### Features
- get info and urls of **deleted** files (as long as you have the project and file id)
- get all addons from the curse hourly and complete feed
- filter to get mods or modpacks only (WIP)

You just need to understand the [api endpoints](#api-enpoints) and json files

API is hosted on https://curse.nikky.moe/ and files may be cached (nyi) for approximately ten minutes to reduce api requests

This tool does not interact with the curse website at all so any changes in html cannot break it

## api enpoints

Example URLs using Wearable Backpacks for the project and file ids
the example host is `https://curse.nikky.moe`

[GET `/api/addon`](https://curse.nikky.moe/api/addon)

[GET `/api/addon/{addonID}`](https://curse.nikky.moe/api/addon/287323)

[GET `/api/addon/{addonID}/description`](https://curse.nikky.moe/api/addon/287323/description)

[GET `/api/addon/{addonID}/files`](https://curse.nikky.moe/api/addon/287323/files)

[GET `/api/addon/{addonID}/files/{fileID}`](https://curse.nikky.moe/api/addon/287323/files/2535294)

[GET `/api/addon/{addonID}/files/{fileID}/changelog`](https://curse.nikky.moe/api/addon/287323/files/2535294/changelog)

[POST `/api/modpack`](https://curse.nikky.moe/api/addon/files) TBD / planned

```sh
curl -X POST \
  'https://curse.nikky.moe/api/addon/files?p=id&p=downloadurl&p=addon.id&p=addon.name&p=addon.categorysection.name&p=addon.categorysection.packagetype&p=addon.categorysection.path' \
  -H 'content-type: application/json' \
  -d @ids.json

# ids.json
[
  {  
    "addonID": 229316,
    "fileID": 2233250
  },
  {  
    "addonID": 223794,
    "fileID": 2245762
  }
]
```

[GET `/api/ids`](https://curse.nikky.moe/api/ids)


[POST `/api/manifest`](https://curse.nikky.moe/api/manifest) TBD / planned

```sh
curl -X POST \
  'https://curse.nikky.moe/api/manifest?p=id&p=downloadurl&p=addon.id&p=addon.name&p=addon.categorysection.name&p=addon.categorysection.packagetype&p=addon.categorysection.path' \
  -H 'content-type: application/json' \
  -d @manifest.json
```

[GET `/api/update/sync`](https://curse.nikky.moe/api/update/sync) TBD / planned

parameters:

- `bool` addons
- `bool` descriptions
- `bool` files
- `bool` changelogs (enabling WILL cripple performance)

[GET `/api/update/scan`](https://curse.nikky.moe/api/update/scan) TBD / planned

scans for hidden / deleted file ids
parameters:

- `bool` addons
- `bool` descriptions
- `bool` files
- `bool` changelogs (enabling WILL cripple performance)

## Config

Coming Soon

## Badge demo

no parameters for newest file on the newest gameversion

[![Download](https://curse.nikky.moe/api/img/287323)](https://curse.nikky.moe/api/url/287323)

supporting all the shields io styles: `plastic`, `flat`, `flat-square`, `for-the-badge`, `social`
[![Download](https://curse.nikky.moe/api/img/287323?style=flat)](https://curse.nikky.moe/api/url/287323)

for older versions

[![Download](https://curse.nikky.moe/api/img/287323?version=1.7.10)](https://curse.nikky.moe/api/url/287323?version=1.7.10)

pinning the fileid too

[![Download](https://curse.nikky.moe/api/img/246105/2535073)](https://curse.nikky.moe/api/url/246105/2535073)

