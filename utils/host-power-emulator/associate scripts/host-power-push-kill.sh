#!/bin/bash
kill $(cat watt-meter-emulator-push-restarter.pid)
kill $(cat watt-meter-emulator-push.pid)
