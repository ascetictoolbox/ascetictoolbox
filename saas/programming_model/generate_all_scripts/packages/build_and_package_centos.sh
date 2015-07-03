#!/bin/bash -e
  
  #Define script variables
  vm_user=

  #---------------------------------------------------------------------------------------------------------------------
  #Install needed software
  echo "--- Installing needed software..."
  sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
  sudo yum install -y apache-maven
  sudo yum install -y rpm-build
  #Runtime dependencies
  sudo yum install -y java-1.7.0-openjdk java-1.7.0-openjdk-devel graphviz xdg-utils
  #Bindings-common-dependencies
  sudo yum install -y java-devel libtool automake make gcc-c++
  #Python-binding dependencies
  sudo yum install -y python-devel
  #C-binding dependencies
  sudo yum install -y libxml2-devel boost-devel tcsh
  #Extrae dependencies
  sudo yum install -y libxml2 gcc-gfortran

  export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk.x86_64/
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
  ./buildrpm "centos"
 
