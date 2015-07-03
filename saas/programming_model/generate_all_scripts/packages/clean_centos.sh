#!/bin/bash -e
  #Define script variables
  vm_user=

  #---------------------------------------------------------------------------------------------------------------------
  #Clean created folders
  echo "--- Cleaning created folders"
  sudo rm -rf /home/${vm_user}/tmpTrunk
  echo "      Success"


  #---------------------------------------------------------------------------------------------------------------------
  #Unninstall needed software
  echo "--- Unninstalling needed software..."
  sudo yum remove -y apache-maven
  sudo yum remove -y rpm-build
  #Runtime dependencies
  sudo yum remove -y java-1.7.0-openjdk java-1.7.0-openjdk-devel graphviz xdg-utils
  #Bindings-common-dependencies
    #None
  #Python-binding dependencies
  sudo yum remove -y python-devel
  #C-binding dependencies
  sudo yum remove -y libxml2-devel boost-devel tcsh
  #Extrae dependencies
  sudo yum remove -y gcc-gfortran
  #Clean
  sudo rm /etc/yum.repos.d/epel-apache-maven.repo
  sudo yum clean all

  echo "      Success"

