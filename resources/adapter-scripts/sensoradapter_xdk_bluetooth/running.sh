#!/bin/bash
runningPID=$(ps -ef | grep sensoradapter_xdk_bluetooth_stub.py | grep -v grep | awk '{print $2}');
if [[ $runningPID != "" ]]; then
   echo "true"; #is running
else
   echo "false"; # is not running
fi
