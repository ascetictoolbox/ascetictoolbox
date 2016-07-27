#!/bin/bash

  # Get worker common functions
  scriptDir=$(dirname $0)
  source ${scriptDir}/worker_commons.sh

  # Pre-execution
  get_parameters $@
  set_env

  # Execution
  shift $shiftSizeForApp # appdir, cp, pythonpath
  taskTracing=false # Only available with NIO
  taskId=0 # Not used with GAT

  python $PYCOMPSS_HOME/pycompss/worker/worker.py $taskTracing $taskId "$@"

  # Exit
  if [ $? -eq 0 ]; then
        exit 0
  else
        echo 1>&2 "Task execution failed"
        exit 7
  fi

