#!/bin/bash

# Copyright 2013 University of Leeds
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#  
#       http://www.apache.org/licenses/LICENSE-2.0
#  
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Author: Django Armstrong (een4dja@leeds.ac.uk)
# Version: 1.0.0

# LIMITATIONS:
# 1) The context CD ROM device must be connected at D:/
# 2) Only a single network device is supported.

#######################
###### Functions ######
#######################

# Gets IP address from a given MAC
mac2ip() {
    mac=$1
 
    let ip_a=0x`echo $mac | cut -d'-' -f 3`
    let ip_b=0x`echo $mac | cut -d'-' -f 4`
    let ip_c=0x`echo $mac | cut -d'-' -f 5`
    let ip_d=0x`echo $mac | cut -d'-' -f 6`
 
    ip="$ip_a.$ip_b.$ip_c.$ip_d"
 
    echo $ip
}

# Programmatic hostname generation useful for large clusters
generate_hostname() {
  IP=$1
  NUM=`echo $IP | cut -d'.' -f 4`
  NUM2=`echo 000000$NUM | sed 's/.*\(...\)/\1/'`
  echo "$BASE_HOSTNAME-$NUM2"
}

# Echo static network variables for debuging
echo_static_network_vars() {
  echo "Using static network variables from profile \"$1:\""
  if [ "$STATIC_HOSTNAME" != "" ]
  then
    echo "STATIC HOSTNAME: $STATIC_HOSTNAME"
  fi
  echo "NETMASK: $STATIC_NETMASK"
  echo "GATEWAY: $STATIC_GATEWAY"
  echo "SUBNET: $STATIC_SUBNET"
  for i in `seq -w 1 ${#STATIC_DNS[*]}`; do
    echo "DNS$i: ${STATIC_DNS[$i]}"
  done
  echo "DNS SEARCH: $STATIC_DNS_SEARCH"
  if [ "$EXTRA_HOSTS" != "" ]
  then
    for i in `seq -w 1 ${#EXTRA_HOSTS[*]}`; do
      echo "EXTRA HOST$i: ${EXTRA_HOSTS[$i]}"
    done
  fi
}

#######################################
###### Start of script execution ######
#######################################

##### Ethernet Device Variables #####
MAC=`ipconfig /all | grep "Physical Address" | cut -d':' -f 2 | cut -d' ' -f 2`
MAC_FIRST_BYTE=`echo $MAC | cut -d'-' -f 1`
INTERFACE_NAME=`ipconfig | grep "Ethernet" | cut -d' ' -f3-20 | cut -d':' -f1`
echo ""
echo "Ethernet device \"$INTERFACE_NAME\" has MAC address: $MAC"

##### Static networking variables #####
# PROVIDER A) LEEDS: (10.*)
if [ "`mac2ip $MAC | cut -d'.' -f1`" == "10" ]
then
  STATIC_HOSTNAME="" #Leave blank for IP generated name
  STATIC_NETMASK="255.0.0.0"
  STATIC_GATEWAY="10.0.0.1"
  STATIC_SUBNET="10.0.0.0"
  STATIC_DNS[1]="129.11.144.1"
  STATIC_DNS[2]="8.8.8.8"
  STATIC_DNS_SEARCH="OptimisBase"
  EXTRA_HOSTS=""
  echo_static_network_vars "PROVIDER A) LEEDS: (10.*)"
# PROVIDER B) ATOS: (192.168.252.*)
elif [ "`mac2ip $MAC | cut -d'.' -f1,2,3`" == "192.168.252" ]
then
  STATIC_HOSTNAME="" #Leave blank for IP generated name
  STATIC_NETMASK="255.255.255.0"
  STATIC_GATEWAY="192.168.252.62"
  STATIC_SUBNET="192.168.252.0"
  STATIC_DNS[1]="212.170.156.7"
  STATIC_DNS[2]="212.170.156.77"
  STATIC_DNS_SEARCH="OptimisBase"
  EXTRA_HOSTS[1]="192.168.252.36 optimis1"
  EXTRA_HOSTS[2]="192.168.252.2 optimis2"
  EXTRA_HOSTS[3]="192.168.252.27 optimis3"
  EXTRA_HOSTS[4]="192.168.252.26 optimis4"
  echo_static_network_vars "PROVIDER B) ATOS: (192.168.252.*)"
# PROVIDER C) UMU: (130.239.48.*)
elif [ "`mac2ip $MAC | cut -d'.' -f1,2,3`" == "130.239.48" ]
then
  STATIC_HOSTNAME="" #Leave blank for IP generated name
  STATIC_NETMASK="255.255.255.128"
  STATIC_GATEWAY="130.239.48.1"
  STATIC_SUBNET="130.239.48.0"
  STATIC_DNS[1]="130.239.40.2"
  STATIC_DNS_SEARCH="OptimisBase"
  EXTRA_HOSTS=""
  echo_static_network_vars "PROVIDER C) UMU: (130.239.48.*)"
# PROVIDER D) FLEX FULL: (109.231.120.*)
elif [ "`mac2ip $MAC | cut -d'.' -f1,2,3`" == "109.231.120" ]
then
  STATIC_HOSTNAME="" #Leave blank for IP generated name
  STATIC_NETMASK="255.255.255.240"
  STATIC_GATEWAY="109.231.120.17"
  STATIC_SUBNET="109.231.120.16"
  STATIC_DNS[1]="8.8.4.4"
  STATIC_DNS[2]="8.8.8.8"
  STATIC_DNS_SEARCH="OptimisBase"
  EXTRA_HOSTS=""
  echo_static_network_vars "PROVIDER D) FLEX FULL: (109.231.120.*)"
# PROVIDER E) FLEX ENHANCED: (109.231.123.*)
elif [ "`mac2ip $MAC | cut -d'.' -f1,2,3`" == "109.231.123" ]
then
  STATIC_HOSTNAME="" #Leave blank for IP generated name
  STATIC_NETMASK="255.255.255.128"
  STATIC_GATEWAY="109.231.123.1"
  STATIC_SUBNET="109.231.123.0"
  STATIC_DNS[1]="8.8.4.4"
  STATIC_DNS[2]="8.8.8.8"
  STATIC_DNS_SEARCH="OptimisBase"
  EXTRA_HOSTS=""
  echo_static_network_vars "PROVIDER E) FLEX ENHANCED: (109.231.123.*)"
else
  echo "WARNING: Unknown Networking Environment (Ignore if using ISO or DHCP)!"
fi

##### Other variables #####
BASE_HOSTNAME="VM" # VERY DANGEROUS: Must not be more than 10 chars long or infinite reboots ftw!
HOSTS_FILE="/cygdrive/c/WINDOWS/system32/drivers/etc/hosts"
echo "127.0.0.1 localhost" > $HOSTS_FILE

##### Setup ethernet device #####  
# WARNING: We only support a single network device...
if [ $MAC_FIRST_BYTE == "0A" ] # Use DHCP if first byte of mac address is '0A'
then
# IP by DHCP
  echo "Setting IP via DHCP..."
  netsh interface ip set address "$INTERFACE_NAME" dhcp
  echo "Setting DNS via DHCP..."
  netsh interface ip set dns "$INTERFACE_NAME" dhcp

  
  # Set hostname after we got an IP address 
  LAST_IP_BYTE=`ipconfig | grep "IP Address" | cut -d' ' -f18 | cut -d'.' -f4`
  # Sleep until we get an IP
  while [ $LAST_IP_BYTE == "0" ]
  do
     echo "Waiting for IP address from DHCP"
     sleep 1
     LAST_IP_BYTE=`ipconfig | grep "IP Address" | cut -d' ' -f18 | cut -d'.' -f4`
  done
  echo ""

  IP=`ipconfig | grep "IP Address" | cut -d' ' -f18`
  echo "Setting up hostname..."
  HOSTNAME=`generate_hostname $IP`
  cscript //Nologo "C:\cygwin\home\Administrator\hostname.js" $HOSTNAME
  echo "New hostname is:" `hostname`
  echo ""
  echo "Config is:"
  netsh interface ip show config
  ipconfig -all

#### Static IP ####
else
  ### Variables ###
  # Support for vmware via mounted ovf, must always be the first CD ROM device (i.e. D:/)
  if [ -f /cygdrive/d/ovf-env.xml ]
  then
    echo "Found ovf at D:/ using new static variables..."
    STATIC_NETMASK=`cat /cygdrive/d/ovf-env.xml | grep mask | cut -d'"' -f4`
    STATIC_GATEWAY=`cat /cygdrive/d/ovf-env.xml | grep gw | cut -d'"' -f4`
    IP=`cat /cygdrive/d/ovf-env.xml | grep ip | cut -d'"' -f4`
    unset STATIC_DNS
    STATIC_DNS[1]=`cat /cygdrive/d/ovf-env.xml | grep dns1 | cut -d'"' -f4`
    STATIC_DNS[2]=`cat /cygdrive/d/ovf-env.xml | grep dns2 | cut -d'"' -f4`
    STATIC_HOSTNAME=""
    STATIC_DNS_SEARCH="vm.local"
    EXTRA_HOSTS=""
    echo_static_network_vars "OVF Network Description"
  else
    # Get IP from mac address instead
    IP=`mac2ip $MAC`
  fi
  echo "IP Address is: $IP"
  CLASS_C_NETWORK=`echo $IP | cut -d'.' -f 1,2,3`
  
  ### Set Hostname ###
  echo "Setting up hostname..."
  HOSTNAME=`generate_hostname $IP`
  cscript //Nologo "C:\cygwin\home\Administrator\hostname.js" $HOSTNAME
  echo "New hostname is:" `hostname`
  echo ""
  
  echo "Setting up Static IP address, netmask and gateway..."
  netsh interface ip set address "$INTERFACE_NAME" static $IP $STATIC_NETMASK $STATIC_GATEWAY 1
  echo "Setting up static DNS server..."
  netsh interface ip set dns "$INTERFACE_NAME" static ${STATIC_DNS[1]} primary
  if [ "${STATIC_DNS[2]}" != "" ]
  then
    netsh interface ip add dns "$INTERFACE_NAME" ${STATIC_DNS[2]} index=2
  fi
  
  ### Generate hosts file (Useful for creating large clusters of machines)
  for n in `seq -w 01 254` # Generate 254 hosts on first class C subnet
  do
    n2=`echo $n | sed 's/^0*//'`
    echo ${CLASS_C_NETWORK}.$n2 $BASE_HOSTNAME-${n} >> $HOSTS_FILE
  done
  
  echo "Config is:"
  netsh interface ip show config
  ipconfig -all
fi

##### Add additional static hosts
if [ ${#EXTRA_HOSTS[*]} -ne 0 ]
then
  for i in `seq -w 1 ${#EXTRA_HOSTS[*]}`
  do
    echo ${EXTRA_HOSTS[$i]} >> $HOSTS_FILE
  done
fi