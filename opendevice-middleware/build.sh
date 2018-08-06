#!/bin/bash
cd ../opendevice-web-view
gulp build:production
mvn install
cd ../opendevice-middleware/
mvn -o package -P standalone -DskipTests

