#!/bin/bash 

MACHINES=(tr-open-12
 tr-open-13
 tr-open-14
 tr-open-15
)

tmux new-session \; \
  split-window -v \; \
  split-window -h \; \
  split-window -h \; \
  select-layout tiled \; \
  select-pane -t 1 \; \
  send-keys "ssh -t ${MACHINES[0]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./rmi_run_server.sh Flights\"" C-m \; \
  select-pane -t 2 \; \
  send-keys "ssh -t ${MACHINES[1]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./rmi_run_server.sh Cars\"" C-m \; \
  select-pane -t 3 \; \
  send-keys "ssh -t ${MACHINES[2]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; ./rmi_run_server.sh Rooms\"" C-m \; \
  select-pane -t 0 \; \
  send-keys "ssh -t ${MACHINES[4]} \"cd $(pwd) > /dev/null; ./run_rmi.sh; echo -n 'Connected to '; hostname; sleep .5s; ./rmi_run_middleware.sh ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}\"" C-m \;