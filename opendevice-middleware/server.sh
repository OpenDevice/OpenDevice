
WEB_PATH="/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-web-view/src/main/webapp"


case $1 in
start)
java -cp "target/opendevice-middleware.jar:target/lib/*" -Dapp.port=8181 -Dapp.dir=$WEB_PATH -Dapp.mode=local  br.com.criativasoft.opendevice.middleware.Main
;;
debug)
java -Dapp.port=8181 -Dapp.dir=web -Dapp.mode=remote -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y -cp "lib/*" -jar target/opendevice-middleware-standalone.jar
;;
compile)
mvn -o package -P standalone -DskipTests
;;

*)
echo "Usage: ./server.sh (start|debug)"
exit 1
 ;;
esac

