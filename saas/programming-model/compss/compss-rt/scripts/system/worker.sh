#!/bin/bash

scriptDir=`dirname $0`
lang=$1
workingDir=$2
libPath=$3
rmfilesNum=$4
shift 4

# Create sandbox
if [ ! -d $workingDir ]
then
        /bin/mkdir -p $workingDir
fi
export IT_WORKING_DIR=$workingDir
sandbox=`/bin/mktemp -d -p $workingDir`
cd $workingDir

# Remove obsolete files
for (( i=0;i<$rmfilesNum;i++)); do
 echo $1
 rm -f $1
 shift 1
done

tracing=$1
shift 1

# Set lib path
if [ "$libPath" != "null" ]
then
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$libPath
fi

# Trace start event
if [ $tracing == "true" ]
then
    eventType=$1
	taskId=$2
	slot=$3
	shift 3
	$scriptDir/trace.sh start $eventType $taskId $slot
fi

appDir=$1
export IT_APP_DIR=$appDir


cd $sandbox

# Run the task with the language-dependent script
$scriptDir/worker_$lang.sh $*
endCode=$?

cd $workingDir


# Trace end event
if [ $tracing == "true" ]
then
	$scriptDir/trace.sh end $eventType $slot
fi

rm -rf $sandbox

if [ $endCode -eq 0 ]
then
	exit 0
else
	echo 1>&2 "Task execution failed"
	exit 7
fi

