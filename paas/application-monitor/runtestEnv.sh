#!/usr/bin/env bash

echo "Running mongodb..."

docker run --name appmon-mongo -p 27017:27017 -d mongo:2.6

echo "Running activeMQ..."

docker run --name appmon-activemq \
	-p 5672:5672 \
	-p 61616:61616 \
	-p 8161:8161 \
	-e 'ACTIVEMQ_ENABLED_AUTH=false' \
	-d webcenter/activemq



#	-e 'ACTIVEMQ_WRITE_LOGIN=guest' \
#	-e 'ACTIVEMQ_WRITE_PASSWORD=guest' \
#	-e 'ACTIVEMQ_READ_LOGIN=guest' \
#	-e 'ACTIVEMQ_READ_PASSWORD=guest' \
#	-e 'ACTIVEMQ_JMX_LOGIN=guest' \
#	-e 'ACTIVEMQ_JMX_PASSWORD=guest' \
