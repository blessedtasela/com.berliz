#!/bin/bash

heroku config:set DB_USERNAME=ucobue657e6j8t -a berliz-server
heroku config:set DB_PASSWORD=p7d7c0c1f9000b4f7cdb20f3db482cbfe615ddbd55dc8f592f91e8dee10cc6c4a -a berliz-server
heroku config:set DB_NAME=d7vgoamro31njg -a berliz-server
heroku config:set DB_URL=jdbc:postgresql://c9uss87s9bdb8n.cluster-czrs8kj4isg7.us-east-1.rds.amazonaws.com:5432/d7vgoamro31njg -a berliz-server
heroku config:set JWT_SECRET=berliz -a berliz-server
heroku config:set MAIL_USERNAME=berlizworld@gmail.com -a berliz-server
heroku config:set MAIL_PASSWORD=lkhkwqvqyqgsrjyd -a berliz-server

# run these lines one after another in CLI after installing heroku cli

## Step 1: Backup local database
#pg_dump -U your_local_username -h localhost -d your_local_db_name -F c -b -v -f db_backup.dump
#
## Step 2: Get Heroku database URL
#heroku config:get DATABASE_URL -a your-heroku-app-name
#
## Step 3: Restore to Heroku Postgres
#pg_restore -U your_heroku_username -h YOUR_HEROKU_DATABASE_URL -d your_heroku_db_name -v db_backup.dump