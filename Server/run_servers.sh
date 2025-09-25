#!/bin/bash 

MACHINES=(tr-open-01
 tr-open-02
 tr-open-03
 tr-open-04
 tr-open-05
)

tmux new-session \; \
    split-window -h -t 0 \; \
    split-window -v -t 0 \; \
    split-window -v -t 1 \; \
    split-window -v -t 2 \; \
    select-layout tiled \; \
    select-pane -t 1 \; \
    send-keys "ssh -t ${MACHINES[0]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./run_server.sh Flights\"" C-m \; \
    select-pane -t 2 \; \
    send-keys "ssh -t ${MACHINES[1]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./run_server.sh Cars\"" C-m \; \
    select-pane -t 3 \; \
    send-keys "ssh -t ${MACHINES[2]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./run_server.sh Rooms\"" C-m \; \
    select-pane -t 4 \; \
    send-keys "ssh -t ${MACHINES[3]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./run_server.sh Customers\"" C-m \; \
    select-pane -t 0 \; \
    send-keys "ssh -t ${MACHINES[4]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; sleep .5s; ./run_middleware.sh ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]} ${MACHINES[3]}\"" C-m \;
