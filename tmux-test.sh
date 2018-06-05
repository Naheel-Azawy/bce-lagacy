#!/bin/sh
# start the server here and get port
tmux new-session -d -s s -n w
tmux send-keys "echo src" Enter
tmux splitw -v -p 25
tmux send-keys "echo terminal" Enter
tmux splitw -v -p 10
tmux send-keys "echo commandline" Enter
tmux selectp -t 1
tmux splitw -h -p 50
tmux send-keys "echo logs" Enter
tmux selectp -t 0
tmux splitw -h -p 60
tmux send-keys "echo registers" Enter
tmux splitw -h -p 50
tmux send-keys "echo memory" Enter
tmux attach -t s
# instead of echo connect to the specified stream in that port
