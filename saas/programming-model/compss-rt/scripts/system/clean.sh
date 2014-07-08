
#!/bin/bash

workingDir=$1
cd $workingDir

rm -rf *.IT

tracing=$2
if [ $tracing == "true" ]
then
    node=$3
    tar cvzf ${node}_compss_trace.tar.gz TRACE.mpits set-*
    rm -rf TRACE.mpits set-*
fi
