# ToDo List

- figure out headless oauth or alternative ways of obtaining oauth tokens

- caching to reduce api requests
  - use database for concurrent access

- scheduled executing (crontab)
  could construct a custom jb manager with
  coroutines + delay and look up times in the database 

- load config from hocon file in `pwd`
  http://ktor.io/servers/configuration.html#custom

- frontend
  - endpoint documentation as website
  - project explorer ?
  
  
sections for 'World of Warcraft' gameId: 1
1 = sectionName: addons name: Addons
sections for 'The Secret World' gameId: 64
14 = sectionName: tsw-mods name: Mods
sections for 'Runes of Magic' gameId: 335
4571 = sectionName: addons name: Addons
sections for 'World of Tanks' gameId: 423
8 = sectionName: wot-mods name: Mods
9 = sectionName: wot-skins name: Skins
sections for 'Rift' gameId: 424
4564 = sectionName: addons name: Addons
sections for 'Minecraft' gameId: 432
4471 = sectionName: modpacks name: Modpacks
6 = sectionName: mc-mods name: Mods
12 = sectionName: texture-packs name: Texture Packs
17 = sectionName: worlds name: Worlds
sections for 'WildStar' gameId: 454
18 = sectionName: ws-addons name: Addons
sections for 'The Elder Scrolls Online' gameId: 455
19 = sectionName: teso-addons name: Addons
sections for 'Darkest Dungeon' gameId: 608
4613 = sectionName: dd-mods name: Mods
sections for 'Stardew Valley' gameId: 669
4643 = sectionName: mods name: Mods