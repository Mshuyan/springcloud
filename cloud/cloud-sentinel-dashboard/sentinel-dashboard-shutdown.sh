#!/bin/sh

pid=`ps ax | grep -i 'sentinel-dashboard' | grep java | grep -v grep | awk '{print $1}'`
if [ -z "$pid" ] ; then
        echo "No sentinel-dashboard running."
        exit -1;
fi

echo "The sentinel-dashboard(${pid}) is running..."

kill ${pid}

echo "Send shutdown request to sentinel-dashboard(${pid}) OK"
