## Ausführen der Anwendung
Öffnen des Projekts 
```bash
#Starten der Anwendung
docker-compose up --build
```
Anschließend Browser öffnen (localhost:8080)

Default-User:
Name: admin
Passwort: admin

## Testing

``` bash

# run docker container 
docker-compose up --build

# on another terminal run unit tests
docker-compose run backend npm test

``` 

## Project Structure
* `backend` 
* `backend/JSONSchema` - folder with custom JSON-Validation files 
* `backend/modules` - folder with custom modules 
* `backend/test` - folder with custom test modules
* `backend-test`
* `data`
* `frontend`
* `frontend/assets` - folder with static assets (images)
* `frontend/components` - folder with custom `.vue` components
* `frontend/css` - custom app CSS styles
* `frontend/pages` - app `.vue` pages
* `frontend/main.js` - main app file where to include/import all required libs and init app
* `frontend/routes.js` - app routes
* `frontend/app.vue` - main app structure/component

## Projekt-Mitglieder
Benjamin Kowatsch (bk095)
