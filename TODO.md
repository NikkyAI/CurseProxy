move logic to in-memory database https://docs.microsoft.com/en-us/aspnet/core/tutorials/web-api-vsc#create-the-database-context

cache addon and file IDs

filter for mods / modpacks / texturepacks etc in feed

POST register

* cronjobs:
  * 30m-1h pull hourly and process
  * 1d-2d pull complete and process
  * concat file/*.json to file/index.json
