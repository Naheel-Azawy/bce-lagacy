#!/bin/sh
./clean.sh
V="$(head -1 changelog)"
sed -i -E "s/VERSION =(.+)/VERSION = \"$V\";/g" ./src/io/naheel/scs/base/utils/Info.java
sed -i -E "s/\(version (.+)/\(version $V\)/g" ./README.md
mkdir bin
javac -d ./bin $(find ./src -name "*.java")
cp ./ic.png ./bin/ic.png
cd bin
jar cvfm ../scs.jar ../manifest $(find -name "*.*")
cd ..
./gen-extras.sh 1> /dev/null
