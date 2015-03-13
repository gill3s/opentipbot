#!/bin/sh
SERVICE_NAME=opentipbot
PATH_TO_JAR=/opt/opentipbot/opentipbot.jar
PATH_TO_LOG=/var/log/opentipbot.log
PID_PATH_NAME=/tmp/opentipbot-pid
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            echo "Starting SERVICE_NAME ..." >> $PATH_TO_LOG
            nohup java -jar $PATH_TO_JAR /tmp 2>> $PATH_TO_LOG >>&1 &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            echo "$SERVICE_NAME stoping ..." >>  $PATH_TO_LOG
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            echo "$SERVICE_NAME stopped ..." >> $PATH_TO_LOG
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            echo "$SERVICE_NAME stopping ..." >> $PATH_TO_LOG;
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            echo "$SERVICE_NAME stopped ..." >> $PATH_TO_LOG;
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            echo "$SERVICE_NAME starting ..." >> $PATH_TO_LOG;
            nohup java -jar $PATH_TO_JAR /tmp 2>> $PATH_TO_LOG >>&1 &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac