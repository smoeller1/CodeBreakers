#!/bin/bash

echo "================================"
echo
echo "Testing RequestType 2"
curl -i -X POST -H 'Content-Type: application/json' -d '{"RequestType":2,"Username":"smoeller","Password":"smoeller"}' http://mongorestapp.mybluemix.net/user
echo
echo
echo



echo "================================"
echo
echo "Testing RequestType 10"
curl -i -X POST -H 'Content-Type: application/json' -d '{"RequestType":10,"RouteStartAddress":"Kansas City, MO","RouteEndAddress":"Springfield, MO"}' http://mongorestapp.mybluemix.net/user
echo
echo
echo
