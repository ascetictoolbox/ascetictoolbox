#!/bin/bash

scriptDir=`dirname $0`
extraeDir=$scriptDir/../../../extrae

export LD_LIBRARY_PATH=$extraeDir/lib:$LD_LIBRARY_PATH
#export EXTRAE_ON=1

action=$1
if [ $action == "start" ]
then
	eventType=$2
	taskId=$3
	slot=$4
	$extraeDir/bin/extrae-cmd emit $slot $eventType $taskId
	endCode=$?
elif [ $action == "end" ]
then
	eventType=$2
	slot=$3
	$extraeDir/bin/extrae-cmd emit $slot $eventType 0
	endCode=$?
elif [ $action == "init" ]
then
    workingDir=$2
    cd $workingDir
    rm -rf TRACE.mpits set-* *_compss_trace.tar.gz
    node=$3
    nslots=$4
    $extraeDir/bin/extrae-cmd init $node $nslots
    endCode=$?
elif [ $action == "gentrace" ]
then
    appName=$2
    traceFiles=(`find *_compss_trace.tar.gz`)
    for file in ${traceFiles[*]}; do
        tmpDir=`mktemp -d`
        tar -C $tmpDir -xvzf $file
        cat $tmpDir/TRACE.mpits >> TRACE.mpits
        cp -r $tmpDir/set-* .
        rm -rf $tmpDir $file
    done
    sec=`/bin/date +%s`
    $extraeDir/bin/mpi2prv -f TRACE.mpits -o ${appName}_compss_trace_${sec}.prv
    endCode=$?
    rm -rf TRACE.mpits set-*
fi

if [ $endCode -eq 0 ]
then
        exit 0
else
        echo 1>&2 "Tracing action $action failed"
        exit 1 
fi

