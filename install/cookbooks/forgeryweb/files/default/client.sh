#!/bin/sh
### BEGIN INIT INFO
# Provides:          forgeryclient
# Required-Start:    $local_fs $remote_fs $network $syslog
# Required-Stop:     $local_fs $remote_fs $network $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop forgery detection worker
### END INIT INFO

cd /opt/forgery/client

case $1 in
    start)
        echo "Starting forgery-client ..."
        if [ ! -f /opt/forgery/client/pid ]; then
            nohup java -Djava.library.path=linux-x86-64 -jar ForgeryDetector.jar 2>> /opt/forgery/client/logErr >> /opt/forgery/client/log &
            echo $! > /opt/forgery/client/pid
            echo "forgery-client started ..."
        else
            echo "forgery-client is already running ..."
        fi
    ;;
    stop)
        if [ -f /opt/forgery/client/pid ]; then
            PID=$(cat /opt/forgery/client/pid);
            echo "Stopping forgery-client ..."
            kill $PID;
            echo "forgery-client stopped ..."
            rm /opt/forgery/client/pid
        else
            echo "forgery-client is not running ..."
        fi
    ;;
    restart)
        if [ -f /opt/forgery/client/pid ]; then
            PID=$(cat /opt/forgery/client/pid);
            echo "Stopping forgery-client ..."
            kill $PID;
            echo "forgery-client stopped ..."
            rm /opt/forgery/client/pid
 
            echo "Starting forgery-client ..."
            nohup java -Djava.library.path=linux-x86-64 -jar ForgeryDetector.jar 2>> /opt/forgery/client/logErr >> /opt/forgery/client/log &
            echo $! > /opt/forgery/client/pid
            echo "forgery-client started ..."
        else
            echo "forgery-client is already running ..."
        fi
    ;;
esac
