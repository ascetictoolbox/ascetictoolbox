#!/bin/sh

scriptDir=`dirname $0`
export LD_LIBRARY_PATH=$scriptDir/../../bindings/c/lib:$scriptDir/../../bindings/bindings-common/lib:$LD_LIBRARY_PATH
app_dir=$1
exec $app_dir/worker_c $@
