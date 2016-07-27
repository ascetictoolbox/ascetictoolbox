#!/bin/bash

  ######################
  # MAIN PROGRAM
  ######################

  # Load common setup functions --------------------------------------
  scriptDir=$(dirname $0)
  source ${scriptDir}/setup.sh

  # Load parameters --------------------------------------------------
  load_parameters $@
  # Shift the parameters consumed only by the script
  shift ${paramsToShift}

  # BLAUNCH start ----------------------------------------------------
  # Check that the current machine has not already awaken any WORKER in PORT and for app UUID
  worker_class="integratedtoolkit.nio.worker.NIOWorker"
  pid=$(ps -elfa | grep ${worker_class} | grep ${appUuid} | grep ${worker_port} | grep -v grep | awk '{print $4}')
  if [ "$pid" != "" ]; then
    if [ "$debug" == "true" ]; then
       echo "Worker already awaken. Nothing to do"
    fi
    echo $pid
    exit 0
  fi
 
  # Normal start -----------------------------------------------------
  # Setup
  setup_environment
  setup_jvm

  # Launch the Worker JVM
  pre_launch

  if [ "$debug" == "true" ]; then
      export NX_ARGS="--summary"
      echo "Calling NIOWorker"
      echo "Cmd: $cmd $*"
  fi

  nohup $cmd $* 1>$workingDir/log/worker_${hostName}.out 2> $workingDir/log/worker_${hostName}.err | echo $! &
  endCode=$?

  post_launch

  # Exit
  if [ $endCode -eq 0 ]; then
	exit 0
  else
	echo 1>&2 "Worker could not be initalized"
	exit 7
  fi

