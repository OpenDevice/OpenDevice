#!/bin/bash
WEB_PATH="webapp"

# set context(current-dir) to directory of script
cd "$( cd "$( dirname "${0}" )" && pwd )"

case $1 in
start)
java -cp "opendevice-middleware.jar:lib/*" -Dconfig="config.js" -Dapp.port=8181 -Dapp.dir=$WEB_PATH -Dapp.mode=local  br.com.criativasoft.opendevice.middleware.Main
;;
debug)
java -Dapp.port=8181 -Dapp.dir=web -Dapp.mode=remote -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y -cp "lib/*" -jar opendevice-middleware.jar
;;
compile)
mvn -o package -P standalone -DskipTests
;;

*)
echo "Usage: ./server.sh (start|debug)"
exit 1
 ;;
esac