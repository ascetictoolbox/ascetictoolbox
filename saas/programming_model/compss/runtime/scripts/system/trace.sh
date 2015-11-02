#!/bin/bash
  
  #-------------------------------------
  # Define script variables and exports
  #-------------------------------------
  scriptDir=$(dirname $0)
  extraeDir=$scriptDir/../../../extrae

  export LD_LIBRARY_PATH=$extraeDir/lib:$LD_LIBRARY_PATH

  #-------------------------------------
  # Get common parameters
  #-------------------------------------
  action=$1
  workingDir=$2
  shift 2
  cd $workingDir
 
  #-------------------------------------
  # MAIN actions
  #-------------------------------------
  if [ $action == "start" ]; then
	eventType=$1
	taskId=$2
	slot=$3
	$extraeDir/bin/extrae-cmd emit $slot $eventType $taskId
	endCode=$?
  elif [ $action == "end" ]; then
	eventType=$1
	slot=$2
	$extraeDir/bin/extrae-cmd emit $slot $eventType 0
	endCode=$?
  elif [ $action == "init" ]; then
    rm -rf TRACE.mpits set-* *_compss_trace.tar.gz
    node=$1
    nslots=$2
    $extraeDir/bin/extrae-cmd init $node $nslots
    endCode=$?
  elif [ $action == "package" ]; then
    node=$1
    tar cvzf ${node}_compss_trace.tar.gz TRACE.mpits set-*
    endCode=$?
    rm -rf TRACE.mpits set-*
  elif [ $action == "gentrace" ]; then
    appName=$1
    traceFiles=(`find *_compss_trace.tar.gz`)
    for file in ${traceFiles[*]}; do
        tmpDir=`mktemp -d`
        tar -C $tmpDir -xvzf $file
        cat $tmpDir/TRACE.mpits >> TRACE.mpits
        cp -r $tmpDir/set-* .
        rm -rf $tmpDir $file
    done
    sec=`/bin/date +%s`
    $extraeDir/bin/mpi2prv -f TRACE.mpits -o ./trace/${appName}_compss_trace_${sec}.prv
    endCode=$?
    rm -rf TRACE.mpits set-*
  fi

  #-------------------------------------
  # Exit
  #-------------------------------------
  if [ $endCode -eq 0 ]; then
        exit 0
  else
        echo 1>&2 "Tracing action $action failed"
        exit 1 
  fi

