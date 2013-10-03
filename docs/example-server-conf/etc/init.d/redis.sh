#!/bin/sh
#
# redis
#
# chkconfig:   - 85 15
# processname: redis-server
# config:      /etc/redis/redis.conf
# pidfile:     /var/run/redis.pid

# Source function library.
. /etc/rc.d/init.d/functions

# Source networking configuration.
. /etc/sysconfig/network

# Check that networking is up.
[ "$NETWORKING" = "no" ] && exit 0

exec="/usr/sbin/redis-server"
prog="$(basename $exec)"
name="redis"

[ -e /etc/sysconfig/$name ] && . /etc/sysconfig/$name

lockfile=/var/lock/subsys/$name

start() {
    echo -n $"Starting $name: "
    # start it up here, usually something like "daemon $exec"
    daemon $exec /etc/$name/$name.conf
    retval=$?
    echo
    [ $retval -eq 0 ] && touch $lockfile
    return $retval
}

stop() {
    echo -n $"Stopping $name: "
    # stop it here, often "killproc $prog"
    killproc $prog
    retval=$?
    echo
    [ $retval -eq 0 ] && rm -f $lockfile
    return $retval
}
 
restart() {
    stop
    start
}
 
reload() {
    echo -n $"Reloading $name: "
    killproc $prog -HUP
    RETVAL=$?
    echo
}
 
force_reload() {
    restart
}

fdr_status() {
    status $prog
}

case "$1" in
    start|stop|restart|reload)
        $1
        ;;
    force-reload)
        force_reload
        ;;
    status)
        fdr_status
        ;;
    *)
        echo $"Usage: $0 {start|stop|status|restart|reload|force-reload}"
        exit 2
esac
