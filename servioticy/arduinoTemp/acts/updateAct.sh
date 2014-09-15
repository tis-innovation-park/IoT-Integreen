#!/bin/sh

API_TOKEN=M2JhMmRkMDEtZTAwZi00ODM5LThmYTktOGU4NjNjYmJmMjc5N2UzNzYwNWItNTc2ZS00MGVlLTgyNTMtNTgzMmJhZjA0ZmIy
SO_ID=14056883311025a9b94340f964f06b73a1b972c875300

curl -i -X PUT -H "Content-Type: application/json" \
-H "Authorization: $API_TOKEN" \
-d 'Received on the device' \
http://localhost:8080/$SO_ID/actuations/$1
