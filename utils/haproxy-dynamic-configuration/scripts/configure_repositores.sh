#!/bin/bash

################################################################################
# Under Apache 2.0 Licences. See LICENSE.TXT for details                       @
# Author: David García Pérez, Atos Research and Innovation                     @
################################################################################

# THIS IS ONLY VALID FOR DEBIAN SQUEEZE!!!:

# We define the archive repositories for Squeeze:
mv /etc/apt/sources.list /etc/apt/sources.list.old

echo "deb http://archive.debian.org/debian/ squeeze main contrib non-free" > /etc/apt/sources.list
echo "deb-src http://archive.debian.org/debian/ squeeze main contrib non-free" >> /etc/apt/sources.list
echo "deb http://archive.debian.org/debian/ squeeze-lts main contrib non-free" >> /etc/apt/sources.list
echo "deb-src http://archive.debian.org/debian/ squeeze-lts main contrib non-free" >> /etc/apt/sources.list

# It is necessary to ignore valid-until requirement when installing packages for Squeeze:

echo 'Acquire::Check-Valid-Until "false";' > /etc/apt/apt.conf.d/90ignore-release-date

# We update the sources to install new packages:
apt-get update

# We install necessary packages...
apt-get --yes --force-yes install curl
apt-get --yes --force-yes install xmlstartlet
