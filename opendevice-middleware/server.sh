
case $1 in
start)
java -Dapp.port=8181 -Dapp.dir=/media/Ricardo/Codigos/Java/Projetos/OpenHouse/openhouse-web-view/src/main/webapp -Dapp.mode=local -jar target/openhouse-server-standalone.jar
;;
debug)
java -Dapp.port=8181 -Dapp.dir=web -Dapp.mode=remote -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y -jar target/openhouse-server-standalone.jar
;;
compile)
mvn package -P build-standalone -DskipTests
;;

*)
echo "Usage: XXXX"
exit 1
 ;;
esac

