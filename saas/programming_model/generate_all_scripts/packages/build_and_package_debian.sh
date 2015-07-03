#!/bin/bash -e

  #Define script variables
  vm_user=

  #---------------------------------------------------------------------------------------------------------------------
  #Install needed software
  echo "--- Installing needed software..."
  sudo apt-get update
  sudo apt-get -y --force-Yes install maven
  #Runtime dependencies
  sudo apt-get -y --force-Yes install openjdk-7-jdk graphviz xdg-utils curl
  #Bindings-common-dependencies
  sudo apt-get -y --force-Yes install libtool automake build-essential
  #Python-binding dependencies
  sudo apt-get -y --force-Yes install python-dev
  #C-binding dependencies
  sudo apt-get -y --force-Yes install libxml2-dev libboost-serialization-dev libboost-iostreams-dev csh
  #Extrae dependencies
  sudo apt-get -y --force-Yes install libxml2 gfortran

  export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/
  echo "      Success"
 

  #---------------------------------------------------------------------------------------------------------------------
  #Download COMPSs repository
  echo "--- Unpackaging COMPSs SVN Revision..."
  cd /home/${vm_user}/
  tar -xzf compss.tar.gz


  #---------------------------------------------------------------------------------------------------------------------
  #Compile, build and package COMPSs
  echo "--- Compile, build and package COMPSs..."
  cd /home/${vm_user}/tmpTrunk/builders/specs/deb
  ./builddeb "debian"

