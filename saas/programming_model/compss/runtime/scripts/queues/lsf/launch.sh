#!/bin/bash

  #Get script parameters
  IT_HOME=$1
  LSB_DJOB_HOSTFILE=$2
  tasks_per_node=$3
  tasks_in_master=$4
  worker_WD_type=$5
  network=$6
  library_path=$7
  cp=$8
  debug=$9
  comm=${10}

  #Leave COMPSs parameters in $*
  shift 10

  #Set script variables
  export IT_HOME=${IT_HOME}
  export GAT_LOCATION=${IT_HOME}/../Dependencies/JAVA_GAT
  worker_install_dir=${IT_HOME}/scripts/system/
  if [ "${worker_WD_type}" == "gpfs" ]; then
     worker_working_dir=$(mktemp -d -p /gpfs/${HOME})
  elif [ "${worker_WD_type}" == "scratch" ]; then
     worker_working_dir=$TMPDIR
  fi
  if [ "${network}" == "default" ]; then
     network=""
  elif [ "${network}" == "infiniband" ]; then
     network="-ib0"
  fi
  sec=`/bin/date +%s`
  RESOURCES_FILE=${worker_working_dir}/resources_$sec.xml
  PROJECT_FILE=${worker_working_dir}/project_mn_$sec.xml

  #---------------------------------------------------------------------------------------
  # Begin creating the resources file and the project file
  /bin/cat > ${RESOURCES_FILE} << EOT
<?xml version="1.0" encoding="UTF-8"?>
<ResourceList>
  <Disk Name="gpfs">
    <MountPoint>/gpfs</MountPoint>
  </Disk>

EOT

  /bin/cat > ${PROJECT_FILE} << EOT
<?xml version="1.0" encoding="UTF-8"?>
<Project>

EOT

  # Get node list
  ASSIGNED_LIST=$(cat ${LSB_DJOB_HOSTFILE} | /usr/bin/sed -e 's/\.[^\ ]*//g')
  echo "Node list assigned is:"
  echo "${ASSIGNED_LIST}"
  # Remove the processors of the master node from the list
  MASTER_NODE=$(hostname)
  echo "Master will run in ${MASTER_NODE}"
  WORKER_LIST=$(echo ${ASSIGNED_LIST} | /usr/bin/sed -e "s/$MASTER_NODE//g")
  # To remove only once: WORKER_LIST=\`echo \$ASSIGNED_LIST | /usr/bin/sed -e "s/\$MASTER_NODE//"\`;
  echo "List of workers:"
  echo "${WORKER_LIST}"
 
  # Add worker slots on master if needed
  if [ ${tasks_in_master} -ne 0 ]; then
	/bin/cat >> ${RESOURCES_FILE} << EOT
<Resource Name="${MASTER_NODE}${network}">
    <Capabilities>
      <Host>
        <TaskCount>0</TaskCount>
      </Host>
      <Processor>
        <Architecture>Intel</Architecture>
        <Speed>2.6</Speed>
        <CoreCount>${tasks_in_master}</CoreCount>
      </Processor>
      <OS>
        <OSType>Linux</OSType>
      </OS>
      <StorageElement>
        <Size>36</Size>
      </StorageElement>
      <Memory>
        <PhysicalSize>28</PhysicalSize>
      </Memory>
      <ApplicationSoftware>
        <Software>COMPSs</Software>
      </ApplicationSoftware>
      <FileSystem/>
      <NetworkAdaptor/>
    </Capabilities>
    <Requirements/>
    <Disks>
      <Disk Name="gpfs">
        <MountPoint>/gpfs</MountPoint>
      </Disk>
    </Disks>
  </Resource>

EOT
        /bin/cat >> ${PROJECT_FILE} << EOT
  <Worker Name="${MASTER_NODE}${network}">
    <InstallDir>${worker_install_dir}</InstallDir>
    <WorkingDir>${worker_working_dir}</WorkingDir>
    <LibraryPath>${library_path}</LibraryPath>
  </Worker>

EOT
  fi

  # Find the number of tasks to be executed on each node
  for node in ${WORKER_LIST}; do
	ssh $node "/bin/mkdir -p ${worker_working_dir}"
	/bin/cat >> ${RESOURCES_FILE} << EOT
  <Resource Name="${node}${network}">
    <Capabilities>
      <Host>
        <TaskCount>0</TaskCount>
      </Host>
      <Processor>
        <Architecture>Intel</Architecture>
        <Speed>2.6</Speed>
        <CoreCount>${tasks_per_node}</CoreCount>
      </Processor>
      <OS>
        <OSType>Linux</OSType>
      </OS>
      <StorageElement>
        <Size>36</Size>
      </StorageElement>
      <Memory>
        <PhysicalSize>28</PhysicalSize>
      </Memory>
      <ApplicationSoftware>
        <Software>COMPSs</Software>
      </ApplicationSoftware>
      <FileSystem/>
      <NetworkAdaptor/>
    </Capabilities>
    <Requirements/>
    <Disks>
      <Disk Name="gpfs">
	<MountPoint>/gpfs</MountPoint>
      </Disk>
    </Disks>
  </Resource>

EOT
	/bin/cat >> ${PROJECT_FILE} << EOT
  <Worker Name="${node}${network}">
    <InstallDir>${worker_install_dir}</InstallDir>
    <WorkingDir>${worker_working_dir}</WorkingDir>
    <LibraryPath>${library_path}</LibraryPath>
  </Worker>

EOT
  done

  # Finish the resources file and the project file 
  /bin/cat >> ${RESOURCES_FILE} << EOT
</ResourceList>
EOT

  /bin/cat >> ${PROJECT_FILE} << EOT
</Project>
EOT

  echo "Generation of resources and project file finished"
  echo "Project.xml:   ${PROJECT_FILE}"
  echo "Resources.xml: ${RESOURCES_FILE}"


  #---------------------------------------------------------------------------------------
  # Launch the application with COMPSs
  MPI_CMD="mpirun -timestamp-output -n 1 -H $MASTER_NODE ${IT_HOME}/scripts/user/runcompss --project=${PROJECT_FILE} --resources=${RESOURCES_FILE} $*"
  if [ "${comm/NIO}" != "${comm}" ]; then
    # Adapt debug flag to worker script
    if [ "${debug}" == "debug" ]; then
      debug="true"
    else
      debug="false"
    fi
    # Runtime will use NIO Adaptor. Starting MPI Workers
    if [ ${tasks_in_master} -ne 0 ]; then
      USED_WORKERS=${ASSIGNED_LIST}
    else
      USED_WORKERS=${WORKER_LIST}
    fi
    for node in ${USED_WORKERS}; do
      MPI_CMD=$MPI_CMD" : -n 1 -H $node ${IT_HOME}/scripts/system/adaptors/nio/persistent_worker_starter.sh ${library_path} null ${cp} ${debug} ${worker_working_dir} ${tasks_per_node} 5 5 $node 43001 43000"
    done
  fi
  echo MPI_CMD=$MPI_CMD
  $MPI_CMD

  #---------------------------------------------------------------------------------------
  # Cleanup
  for node in ${WORKER_LIST}; do
	ssh $node "/bin/rm -rf ${worker_working_dir}"
  done
  /bin/rm -rf ${PROJECT_FILE}
  /bin/rm -rf ${RESOURCES_FILE}

