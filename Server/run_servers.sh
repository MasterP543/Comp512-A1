#!/bin/bash 

MACHINES=(tr-open-16
 tr-open-17
 tr-open-18
 tr-open-19
)

tmux new-session \; \
    split-window -v \; \
    split-window -h \; \
    split-window -h \; \
    select-layout tiled \; \
    select-pane -t 1 \; \
    send-keys "ssh -t ${MACHINES[0]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Flights\"" C-m \; \
    select-pane -t 2 \; \
    send-keys "ssh -t ${MACHINES[1]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Cars\"" C-m \; \
    select-pane -t 3 \; \
    send-keys "ssh -t ${MACHINES[2]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Rooms\"" C-m \; \
    select-pane -t 0 \; \
    send-keys "ssh -t ${MACHINES[3]} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; sleep .5s; ./run_middleware.sh ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]} \"" C-m \;
