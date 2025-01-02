echo "starting ..."
cd ..
# shellcheck disable=SC2164
cd TestCube
mvn clean -Plocal-execution -Dsuite=test verify
