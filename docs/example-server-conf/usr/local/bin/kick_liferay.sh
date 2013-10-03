#!/bin/bash

if [[ "x${USER}" != "xroot" ]]
then
    echo "$0: This script must be run as root!" >&2
    exit 1
fi

function getProcs() {
    procs=$( \
        ps aux \
        | egrep '^liferay.*\/opt\/jdk\/bin\/java.*\/opt\/liferay\/tomcat' \
        )
}

getProcs
if [[ "x$procs" != "x" ]]
then
    echo "* Try killing Liferay with init script"
    /etc/init.d/liferay stop -force
fi

count=0

getProcs
while [[ "x$procs" != "x" ]]
do
    if [[ ${count} -lt 3 ]]
    then
        echo "* Doesn't seem to want to die, try killing with SIGTERM"
        sig="-15"
    else
        echo "* Really doesn't seem to want to die, try killing with SIGKILL"
        sig="-9"
    fi
    echo ${procs} | awk '{ print $2 }' | xargs kill ${sig}
    count=$[ ${count} + 1 ]
    sleep 3
    getProcs
done

echo "* Clean up temp and work"
rm -Rf /opt/liferay/tomcat/temp/*
rm -Rf /opt/liferay/tomcat/work/*

echo "* Remove Lucene lock file"
sudo rm -f /opt/liferay/data/lucene/10108/write.lock

echo "* Restart Liferay"
/etc/init.d/liferay start
