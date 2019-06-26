#!/bin/bash
PROJECTNAME=RangerALdapApi
pid=`ps -ef |grep $PROJECTNAME |grep -v "grep" |awk '{print $2}'`   
if [ $pid ]; then  
    echo "$PROJECTNAME  is  running  and pid=$pid"  
else  
   echo "Start success to start $PROJECTNAME ...."
   nohup java -jar RangerALdapApi-0.0.1-SNAPSHOT.jar  >> catalina.out  2>&1 &
fi 
