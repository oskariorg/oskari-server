#!/bin/sh -e
# LIFERAY startup script
#chkconfig: 2345 80 05
#description: Liferay

export JAVA_HOME=/opt/jdk
export CATALINA_HOME=/opt/liferay/tomcat
export CATALINA_BASE=/opt/liferay/tomcat
export TOMCAT_USER=liferay

if [ $( ulimit -n ) -lt 2048 ] ; then
    ulimit -n 2048
fi

start() {
    echo -n " Starting Liferay-Tomcat ..."
    /bin/su -m $TOMCAT_USER -c $CATALINA_HOME/bin/startup.sh
    sleep 2
}
stop()  {
    echo -n " Stopping Liferay-Tomcat ..."
    /bin/su -m $TOMCAT_USER -c $CATALINA_HOME/bin/shutdown.sh
    sleep 5
}
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo $"{start|stop|restart}"
        exit
esac


