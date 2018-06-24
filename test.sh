#!/bin/sh
function t() {
    echo "Running $1"
    F=$1
    P=./test/$1
    M=$2
    E=$3
    shift 3
    if [ $(java -jar scs.jar $@ -nw $P -exec "run-sync; echo \$M[$M]; exit") == $E ]; then
        echo "$F: ok"
    else
        echo "$F: fail"
    fi
}

t test 6 60
t test-dec 6 60 -t dec
t test-hex 6 60 -t hex
t test-bin 6 60 -t bin
t test-mul 20 17088
t ben-test 7 7 -a ben
