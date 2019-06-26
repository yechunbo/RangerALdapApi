#!/bin/bash

PROJECTNAME=RangerALdapApi
pid=`ps -ef |grep $PROJECTNAME |grep -v "grep" |awk '{print $2}' `   
if [ $pid ]; then  
    echo "$PROJECTNAME is  running  and pid=$pid"  
    kill -9 $pid  
    if [[ $? -eq 0 ]];then   
       echo "sucess to stop $PROJECTNAME "   
    else   
       echo "fail to stop $PROJECTNAME "  
     fi  
fi  
