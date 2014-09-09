#!/bin/sh

usage ()
{
	/bin/cat <<EOT
Usage: $0 required_options [other_options]
Required options:
	--app=<name>				JAVA - Fully qualified name of the application | C - Path to the master binary | Python - Path to the .py file containing the main program
	--exec_time=<minutes>			Expected execution time of the application (minutes)
	--num_nodes=<n>				Number of nodes to use
Other options:
	--classpath=<path>			Classpath for the application classes - Default current dir
	--cline_args="<args>"			Command line arguments to pass to the application
        --debug=<bool>                          Enable debugging mode - Default false
	--graph=<bool>                          Generation of the task dependency graph (true | false) - Default false
	--lang=<name>                           Language of the application ( java | c | python ) - Default java
	--library_path=<path>                   Non-standard directories to search for libraries (e.g. Java JVM library, Python library, C binding library) - Default is current directory 
	--queue_system=<name>			Queue system to use ( lsf | pbs | slurm ) - Default lsf
        --task_count=<int>                      Number of different functions/methods, invoked from the application, that have been selected as a task - Default 50
	--tasks_per_node=<n>			Maximum number of simultaneous tasks running in a node - Default 16
	--tracing=<bool>			Generation of traces (true | false) - Default false
	--working_dir=<path>			Working directory of the application - Default is current directory
	-h --help				Print this help message
EOT
	exit 0;
}


# Parse options
while getopts vhL:P:-: o
do case "$o" in
	h)
		usage;
		;;
	-) 
		case "$OPTARG" in
			app=*)
				APP_NAME=`echo $OPTARG | sed -e 's/app=//g'`;
			;;
			num_nodes=*)
                                NNODES=`echo $OPTARG | sed -e 's/num_nodes=//g'`;
                        ;;
			cline_args=*)
                                CLARGS=`echo $OPTARG | sed -e 's/cline_args=//g'`;
                        ;;
			exec_time=*)
				WC_LIMIT=`echo $OPTARG | sed -e 's/exec_time=//g'`;
			;;
			classpath=*)
                                CP=`echo $OPTARG | sed -e 's/classpath=//g'`;
                        ;;
			graph=*)
                                GRAPH=`echo $OPTARG | sed -e 's/graph=//g'`;
                        ;;
			tracing=*)
                                TRACING=`echo $OPTARG | sed -e 's/tracing=//g'`;
                        ;;
			tasks_per_node=*)
                                TPN=`echo $OPTARG | sed -e 's/tasks_per_node=//g'`;
                        ;;
			lang=*)
                                LANGU=`echo $OPTARG | sed -e 's/lang=//g'`;
                        ;;
			library_path=*)
                                LIBRARY_PATH=`echo $OPTARG | sed -e 's/library_path=//g'`;
                        ;;
                        task_count=*)
                                TASK_COUNT=`echo $OPTARG | sed -e 's/task_count=//g'`;
                        ;;
                        debug=*)
                                DEBUG=`echo $OPTARG | sed -e 's/debug=//g'`;
                        ;;
			working_dir=*)
                                WORKING_DIR=`echo $OPTARG | sed -e 's/working_dir=//g'`;
                        ;;
			queue_system=*)
                                QUEUE_SYSTEM=`echo $OPTARG | sed -e 's/queue_system=//g'`;
                        ;;
			help)
				usage;
			;;
			*) 
				echo "Bad argument: $OPTARG";
				echo;
				usage;
			;;
			esac
	;;			
	esac
done


check_options()
{
	if [ -z "$APP_NAME" ]
	then
		echo "Option --app=<name> not provided"
                echo
                usage;
	fi

	if [ -z "$NNODES" ]
        then
                echo "Option --num_nodes=<n> not provided"
                echo
                usage;
        fi

	if [ -z "$WC_LIMIT" ]
        then
                echo "Option --exec_time=<minutes> not provided"
                echo
                usage;
        fi

	if [ -z "$CLARGS" ]
        then
                CLARGS=""
        fi

	if [ -z "$CP" ]
        then
		CP=`pwd`
        fi

	if [ -z "$TRACING" ]
        then
                TRACING=false
        fi

	if [ -z "$TPN" ]
        then
                TPN=16
        fi

	if [ -z "$GRAPH" ]
        then
                GRAPH=false
        fi

	if [ -z "$LANGU" ]
        then
                LANGU=java
        elif [ "$LANGU" = "java" ]
        then
                LANGU=java
        elif [ "$LANGU" = "c" ]
        then
                LANGU=c
        elif [ "$LANGU" = "python" ]
        then
                LANGU=python
        else
                echo "Option --lang value must be java, c or python"
                echo
                usage;
        fi

	if [ -z "$TASK_COUNT" ]
        then
                TASK_COUNT=50
        fi

        if [ -z "$LIBRARY_PATH" ]
        then
                LIBRARY_PATH=`pwd`
        fi

        if [ -z "$DEBUG" ]
        then
                DEBUG=false
        fi

	if [ -z "$WORKING_DIR" ]
        then
                WORKING_DIR=`pwd`
        fi

	if [ -z "$QUEUE_SYSTEM" ]
        then
        	QUEUE_SYSTEM=lsf
        elif [ "$QUEUE_SYSTEM" = "lsf" ]
        then
                QUEUE_SYSTEM=lsf
        elif [ "$QUEUE_SYSTEM" = "pbs" ]
        then
                QUEUE_SYSTEM=pbs
        elif [ "$QUEUE_SYSTEM" = "slurm" ]
        then
                QUEUE_SYSTEM=slurm
        else
                echo "Option --queue_system value must be lsf, pbs or slurm"
                echo
                usage;
	fi
}

# Fixed options
MONITORING=60000

# Pre-checks
check_options

# Run the application
script_dir=`dirname $0`
$script_dir/$QUEUE_SYSTEM/submit.sh $NNODES $TPN $WC_LIMIT $TRACING $MONITORING $DEBUG $CP $GRAPH $WORKING_DIR $LANGU $TASK_COUNT $LIBRARY_PATH $APP_NAME $CLARGS

