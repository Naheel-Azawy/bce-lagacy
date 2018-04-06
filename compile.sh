#!/bin/sh
./clean.sh
sed -i -E "s/VERSION =(.+)/VERSION = \"$(head -1 changelog)\";/g" ./src/app/Info.java
mkdir bin
javac -d ./bin $(find -name "*.java")
cp ./ic.png ./bin/ic.png
cd bin
jar cvfm ../scs.jar ../manifest $(find -name "*.*")
cd ..
./gen-extras.sh 1> /dev/null