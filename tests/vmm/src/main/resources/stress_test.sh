#!/bin/bash

PEM_FILE=/Users/raimon/Downloads/ascetic_pm_demo

echo "Installing stress package"
./install_stress.sh fixedSize2CPUsDeployment ${PEM_FILE}
./install_stress.sh fixedSize3CPUsDeployment ${PEM_FILE}
./install_stress.sh fixedSize4CPUsDeployment ${PEM_FILE}
./install_stress.sh slotAwareDeployment ${PEM_FILE}

echo "Starting stress test"
./stress_all.sh fixedSize2CPUsDeployment ${PEM_FILE}
./stress_all.sh fixedSize3CPUsDeployment ${PEM_FILE}
./stress_all.sh fixedSize4CPUsDeployment ${PEM_FILE}
./stress_all.sh slotAwareDeployment ${PEM_FILE}

echo "Stressing cpus for 5 minutes..."
sleep 300

"Stopping stress test"
./stress_stop.sh fixedSize2CPUsDeployment ${PEM_FILE}
./stress_stop.sh fixedSize3CPUsDeployment ${PEM_FILE}
./stress_stop.sh fixedSize4CPUsDeployment ${PEM_FILE}
./stress_stop.sh slotAwareDeployment ${PEM_FILE}