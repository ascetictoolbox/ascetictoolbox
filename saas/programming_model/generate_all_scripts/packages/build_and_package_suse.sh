#!/bin/bash -e

  #Define script variables
  vm_user=

  #---------------------------------------------------------------------------------------------------------------------
  #Install needed software
  echo "--- Installing needed software..."
  sudo zypper ar http://download.opensuse.org/repositories/Application:/Geo/openSUSE_13.1/ mvn
  sudo zypper --gpg-auto-import-keys refresh
  sudo zypper install -y maven
  sudo zypper install -y rpm-build
  #Runtime dependencies
  sudo zypper install -y java-1.7.0-openjdk java-1.7.0-openjdk-devel graphviz xdg-utils
  #Bindings-common-dependencies
  sudo zypper install -y libtool automake make gcc-c++
  #Python-binding dependencies
  sudo zypper install -y python-devel
  #C-binding dependencies
  sudo zypper install -y libxml2-devel boost-devel tcsh
  #Extrae dependencies
  sudo zypper install -y libxml2 gcc-fortran

  export JAVA_HOME=/usr/lib64/jvm/java-1.7.0-openjdk/
  echo "      Success"

  
  #---------------------------------------------------------------------------------------------------------------------
  #Download COMPSs repository
  echo "--- Unpackaging COMPSs SVN Revision..."
  cd /home/${vm_user}/
  tar -xzf compss.tar.gz


  #---------------------------------------------------------------------------------------------------------------------
  #Compile, build and package COMPSs
  echo "--- Compile, build and package COMPSs..."
  cd /home/${vm_user}/tmpTrunk/builders/specs/rpm
  ./buildrpm "suse"
 
