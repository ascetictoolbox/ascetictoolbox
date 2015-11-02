#!/bin/bash

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
  $cmd $* 1>$workingDir/log/worker_${id}.out 2> $workingDir/log/worker_${id}.err

  #Exit
  exit
