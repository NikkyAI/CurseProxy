## Alpacka.Meta

### Setup

```
git clone git@github.com:NikkyAI/alpacka.meta.git
cd alpacka.meta
dotnet restore
```

### Run

```
dotnet run download complete
dotnet run download hourly
```

#### file structure


```
$output$
├── addon
│   ├── $addon_id$
│   │   ├── description.html
│   │   ├── files
│   │   │   ├── $file_id$.changelog.html
│   │   │   ├── $file_id$.json
│   │   │   │   ...
│   │   │   ├── $file_id$.changelog.html
│   │   │   ├── $file_id$.json
│   │   │   └── index.json
│   │   └── index.json
│   └── $addon_id$
│       ├── description.html
│       ├── files
│       │   ├── $file_id$.changelog.html
│       │   ├── $file_id$.json
│       │   │   ...
│       │   └── index.json
│       └── index.json
├────── complete.json
├────── complete.json.bz2
├────── mods.json
├────── mods.json.bz2
├────── modpacks.json
└────── modpacks.json.bz2
```