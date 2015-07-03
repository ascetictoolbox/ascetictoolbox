#!/bin/bash

export EXTRAE_HOME=@sub_PREFIXDIR@
source ${EXTRAE_HOME}/etc/extrae.sh

# Only show output for task 0, others task send output to /dev/null
if test "${SLURM_PROCID}" == "0" ; then
	${EXTRAE_HOME}/bin/extrae -config ../extrae.xml $@ > job${SLURM_JOB_ID}.${SLURM_PROCID}.out 2> job${SLURM_JOB_ID}.${SLURM_PROCID}.err
else
	${EXTRAE_HOME}/bin/extrae -config ../extrae.xml $@ > /dev/null 2> /dev/null
fi

