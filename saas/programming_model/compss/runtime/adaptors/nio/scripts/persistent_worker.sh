#!/bin/bash

  # MN grep starter
  pid=$(ps -elfa | grep integratedtoolkit.nio.worker.NIOWorker | grep 43001 | grep -v grep | awk '{print $4}')
  if [ "$pid" != "" ]; then
    echo $pid
    exit 0
  fi

  # Normal start
  add_to_classpath () {
        DIRLIBS=${1}/*.jar
        for i in ${DIRLIBS}
        do
                if [ "$i" != "${DIRLIBS}" ] ; then
                        CLASSPATH=$CLASSPATH:"$i"
                fi
        done
  }


  #Script variables
  scriptDir=$(dirname $0)

  #Parameters
  libPath=$1
  appDir=$2
  cp=$3
  shift 3

  debug=$1
  workingDir=$2

  # Create sandbox
  if [ ! -d $workingDir ]; then
	/bin/mkdir -p $workingDir
  fi
  export IT_WORKING_DIR=$workingDir
  mkdir -p $workingDir/log
  mkdir -p $workingDir/jobs 

  # Set lib path
  if [ "$libPath" != "null" ]; then
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$libPath
  fi

  # Set appDir
  export IT_APP_DIR=$appDir
  if [ "$appDir" != "null" ]; then
  	add_to_classpath "$appDir"
  	add_to_classpath "$appDir/lib"
  fi

  # Set the classpath
  if [ "$cp" == "null" ]; then	
	cp=""
  fi

  # Launch the worker
  worker_jar=${scriptDir}/../../../../adaptors/nio/worker/compss-adaptors-nio-worker.jar  
  cmd="java -Xms128m -Xmx2048m -classpath $CLASSPATH:$cp:${worker_jar} integratedtoolkit.nio.worker.NIOWorker"

  if [ "$debug" == "true" ]; then
    echo "libPath: $libPath"
    echo "AppDir: $appDir"
    echo "Classpath: $cp"
    echo "WorkingDir: $workingDir"
    echo "Cmd: $cmd $*"
  fi

  # Launch the JVM
  id=$6
  nohup $cmd $* 1>$workingDir/log/worker_${id}.out 2> $workingDir/log/worker_${id}.err | echo $! &
  endCode=$?

  #Exit
  if [ $endCode -eq 0 ]; then
	exit 0
  else
	echo 1>&2 "Worker could not be initalized"
	exit 7
  fi
