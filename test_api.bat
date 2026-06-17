@echo off
curl.exe -s -X POST http://localhost:8080/api/selection/recommend -H "Content-Type: application/json" -d @req3.json -o resp5.json 2>curl_err.txt
type resp5.json
type curl_err.txt
