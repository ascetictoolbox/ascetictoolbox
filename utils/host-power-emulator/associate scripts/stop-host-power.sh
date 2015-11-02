#!/bin/bash
echo "Stopping emulated watt meter services..."

kill $(cat watt-meter-emulator-restarter.pid)
kill $(cat watt-meter-emulator.pid)

./watt-meter-emulator-kill.sh

echo "Stopped the emulated watt meter"
