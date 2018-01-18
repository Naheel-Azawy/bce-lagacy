#!/bin/sh
function t() {
    echo "Running $1"
    if [ $(java -jar scs.jar -nogui $4 -m $2 ./test/$1) == $3 ]; then
        echo "$1: ok"
    else
        echo "$1: fail"
    fi
}

t test 6 60
t test-dec 6 60 -d
t test-hex 6 60 -x
t test-bin 6 60 -b
t test-mul 20 17088