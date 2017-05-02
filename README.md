# Alpacka.Meta

## About

Alpacka Meta is a tool to connect to the `SOAP API` of curse and make the data available in easy to read json files

### Features
- get info and urls of **deleted** files (as long as you have the project and file id)
- get all addons from the curse hourly and complete feed
- filter to get mods or modpacks only

to use the meta files you do **NOT** need:
- a curse login
- Visual Studio plugins to generate a SOAP client based on `.wsdl` files and fix horrible generated code and incorrect endpoint URLs

You just need to understand the [file structure](#file-structure) and json file content

Files are hosted on https://cursemeta.nikky.moe/ and updated approximately every hour (assuming nothing goes horribly wrong)  
Files are also mirrored to https://github.com/NikkyAI/alpacka-meta-file every 12 hours

This tool does not interact with the curse website at all so any changes in html cannot break it

[tips for selfhosting](#selfhosting)

## Setup

for running the tool you need the [dotnet core](https://www.microsoft.com/net/core#linuxredhat) runtime (packagename: `dotnet-cli`)

and if you want to build and compile on the same server you will also require the dotnet sdk (packagename: `dotnet-sdk`)

```
git clone git@github.com:NikkyAI/alpacka.meta.git
cd alpacka.meta
dotnet restore
dotnet build
dotnet publish -c release
```
add `alias alpacka-meta='dotnet /path/to/src/alpacka-meta/bin/release/netcoreapp1.1/publish/alpacka-meta.dll'`
to your `.bashrc` / `.zshrc`

note: cron seems to be unable to read the aliases so for scripts you may need to use the full `dotnet [ddl]` command

### Register

with the command `register` you can create a old style (non twitch) account that can be used with the API

## Run

```
alpacka-meta download complete
alpacka-meta download hourly
```

## file structure

Example URLs using Wearable Backpacks for the project and file ids

https://www.curseforge.com/projects/257572/

* [cursemeta.nikky.moe](https://cursemeta.nikky.moe)
  * [addon/](https://cursemeta.nikky.moe/addon/)
    * [`addon id`/](https://cursemeta.nikky.moe/addon/257572/)
      * [description.html](https://cursemeta.nikky.moe/addon/257572/description.html)
      * [files/](https://cursemeta.nikky.moe/addon/257572/files)
        * [`file_id`.changelog.html](https://cursemeta.nikky.moe/addon/257572/files/2382299.changelog.html)
        * [`file_id`.json](https://cursemeta.nikky.moe/addon/257572/files/2382299.json)
        * [index.json](https://cursemeta.nikky.moe/addon/257572/files/index.json)
      * [index.json](https://cursemeta.nikky.moe/addon/257572/index.json)
  * [complete.json](https://cursemeta.nikky.moe/complete.json)
  * [complete.json.bz2](https://cursemeta.nikky.moe/complete.json.bz2)
  * [mods.json](https://cursemeta.nikky.moe/mods.json)
  * [mods.json.bz2](https://cursemeta.nikky.moe/mods.json.bz2)
  * [modpacks.json](https://cursemeta.nikky.moe/modpacks.json)
  * [modpacks.json.bz2](https://cursemeta.nikky.moe/modpacks.json.bz2)

the html files are opt in options and increase the runtime significantly.. so they may not exist by default
//TODO: add cgi hook and command to get .html files

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

## selfhosting

you will need to run  
`alpacka-meta download hourly`  
every hour or less to be safe

using `~/www/cursemeta` as location for scripts and html  
a crontab entry could look like:  
`*/30 * * * * ~/www/cursemeta/update.sh > /dev/null`

```bash
# ~/www/cursemeta/update.sh
#!/bin/env bash

cd ~/www/cursemeta/html/
/usr/local/bin/dotnet ~/src/alpacka-meta/bin/release/netcoreapp1.1/publish/alpacka-meta.dll download hourly -o ~/www/cursemeta/html
```

### nginx configuration
this is not required but will make your life easier.. hopefully

`autoindex on;`  

`index index.html index.htm index.json;`
> for using `$url/` instead of `url/index.json`

`try_files $uri $uri.json $uri/ $uri.html @getinfo;`
> `@getinfo` points to a rewrite and proxy pass to apache which handles /bin/cgi scripts

```
location @getinfo {
    rewrite "^/addon/(?<project>[0-9]+)/files/(?<file>[0-9]+)\.json$" /cgi-bin/somescript.cgi?$project-$file break;
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header   Host $host;
}
```  

this block will execute a cgi script that is supposed to call
`alpacka-meta get --file $project:$file`
and then return (print) the content of the just generated file  

you could probably handle it with nginx fastcgi too but this is what worked best for me

note: all `.html` entries are in this case unecessary as there will be no html hosted, they could be removed
