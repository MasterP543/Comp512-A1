#Usage: ./rmi_run_server.sh [<rmi_name>]

java -cp Server:. Server.TCP.TCPResourceManager $1
