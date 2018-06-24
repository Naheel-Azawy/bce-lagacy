#!/bin/bash

# Example usage:
# ./scs-tmux.sh
# ./scs-tmux.sh 7777
# ./scs-tmux.sh 7777 ./test/test-io
# ./scs-tmux.sh 7777 ./test/test-io "set-freq 70; set-mem-start 55"
# ./scs-tmux.sh 7777 ./test/ben-test "set-arch ben; set-freq 5"

P=$1 # Port
F=$2 # File
C=$3 # Commands
if [ "$P" = "" ]; then
    P="5555"
fi
if [ "$F" = "" ]; then
    F="~/.scs_tmp"
    rm -f $F
    touch $F
fi
if [ "$C" != "" ]; then
    C="$C;"
fi
function cmd-exists { command -v $1 >/dev/null 2>&1; }
if [ "$EDITOR" = "" ]; then
    if cmd-exists emacs; then
        if emacsclient -e 0 >&/dev/null; then
            EDITOR="emacsclient -nw"
        else
            EDITOR="emacs -nw"
        fi
    elif cmd-exists vim; then
        EDITOR="vim"
    elif cmd-exists nano; then
        EDITOR="nano"
    else
        EDITOR="more"
    fi
fi
S="SCS"
RUN="sleep 0.5; java -jar scs.jar --client $P "
tmux new-session -d -s $S -n "${S}_server"
tmux send-keys "java -jar scs.jar --server $P; exit" Enter
tmux new-window -n "${S}_clients"
tmux send-keys "$EDITOR $F; exit" Enter
tmux splitw -v -p 25
tmux send-keys "$RUN \"echo Terminal; clear-con 1; trm\"; exit" Enter
tmux splitw -v -p 10
tmux send-keys "$RUN \"echo Console; clear-con 1; $C load $F\"; exit" Enter
tmux selectp -t 1
tmux splitw -h -p 50
tmux send-keys "$RUN \"echo Logs; clear-con 2; log connect\"; exit" Enter
tmux selectp -t 0
tmux splitw -h -p 60
tmux send-keys "$RUN \"clear-con; reg connect\"; exit" Enter
tmux splitw -h -p 50
tmux send-keys "$RUN \"clear-con; mem connect\"; exit" Enter
tmux selectp -t 5
tmux attach -t $S
