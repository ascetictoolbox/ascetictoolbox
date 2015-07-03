#!/bin/bash

scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $scriptDir/../compss

find compss-annotations -name "*.java" -exec $scriptDir/replace_header.sh {} java_c \; 
find compss-connectors -name "*.java" -exec $scriptDir/replace_header.sh {} java_c \;
find compss-monitor -name "*.java" -exec $scriptDir/replace_header.sh {} java_c \;
find compss-rt/rt -name "*.java" -exec $scriptDir/replace_header.sh {} java_c \;

find compss-rt/bindings/bindings-common -name "*.c" -exec $scriptDir/replace_header.sh {} java_c \;
find compss-rt/bindings/bindings-common -name "*.cc" -exec $scriptDir/replace_header.sh {} java_c \;
find compss-rt/bindings/bindings-common -name "*.h" -exec $scriptDir/replace_header.sh {} java_c \;

find compss-rt/bindings/c -name "*.c" -exec $scriptDir/replace_header.sh {} java_c \;
find compss-rt/bindings/c -name "*.cc" -exec $scriptDir/replace_header.sh {} java_c \;
find compss-rt/bindings/c -name "*.h" -exec $scriptDir/replace_header.sh {} java_c \;

find compss-rt/bindings/python -name "*.py" -exec $scriptDir/replace_header.sh {} python \;
