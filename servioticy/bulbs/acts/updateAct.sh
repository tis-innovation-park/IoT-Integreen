#!/bin/sh

API_TOKEN=M2JhMmRkMDEtZTAwZi00ODM5LThmYTktOGU4NjNjYmJmMjc5N2UzNzYwNWItNTc2ZS00MGVlLTgyNTMtNTgzMmJhZjA0ZmIy
SO_ID=14111309265504474785e6a3f42b9bdec6af535d023d2

curl -i -X PUT -H "Content-Type: application/json" \
-H "Authorization: $API_TOKEN" \
-d 'Received on the device' \
http://localhost:8080/$SO_ID/actuations/$1
