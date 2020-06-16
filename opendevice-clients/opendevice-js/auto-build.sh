#!/bin/bash

##
## Use this script to auto-build on file changes, to speed-up development
##

while true; do

inotifywait -e modify,create,delete,move -r src/js && \
echo "Deploy !!!"

mvn generate-resources -DskipTests

done