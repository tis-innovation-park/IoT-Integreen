#!/bin/sh

HOST=localhost:8080
API_TOKEN=M2JhMmRkMDEtZTAwZi00ODM5LThmYTktOGU4NjNjYmJmMjc5N2UzNzYwNWItNTc2ZS00MGVlLTgyNTMtNTgzMmJhZjA0ZmIy
SO_ID=14111309265504474785e6a3f42b9bdec6af535d023d2
 
curl -i -X POST -H "Content-Type: text/plain" \
-H "Authorization: $API_TOKEN" \
http://$HOST/$SO_ID/actuations/$1
