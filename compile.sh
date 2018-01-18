#!/bin/sh
./clean.sh
mkdir bin
javac -d ./bin $(find -name "*.java")
cp ./ic.png ./bin/ic.png
cd bin
jar cvfm ../scs.jar ../manifest $(find -name "*.*")
