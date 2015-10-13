#!/usr/bin/env bash

echo "Running mongodb..."

cd etc/db

mongod -f mongo.conf &

echo "Running activeMQ..."

cd ../activemq/bin

./activemq start
