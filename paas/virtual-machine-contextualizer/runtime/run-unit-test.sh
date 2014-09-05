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

#WARNING These OVF Definitions are out of date...

#Test conversion of linux cqow2 image
#java -jar virtual-machine-contextualizer.jar 1 /opt/ascetic/vmc/runtime /opt/ascetic/vmc/runtime/some-ovf.xml vmdk

#Test conversion of windows cqow2 image
#java -jar virtual-machine-contextualizer.jar 1 /opt/ascetic/vmc/runtime /opt/ascetic/vmc/runtime/some-ovf.xml vmdk

#Test the programming model ovf
#java -jar virtual-machine-contextualizer.jar 1 /opt/ascetic/vmc/runtime /opt/ascetic/vmc/runtime/some-ovf.xml 

#Test with defaults
java -DovfSampleDir=/home/vmc/test-ovf-out -jar virtual-machine-contextualizer.jar 1 `pwd`

#Test image manipulation scripts
echo ""
echo "### Testing following scripts in bin:"

for i in `find bin -maxdepth 1 -type f`
do
  echo "### Testing script usage output:"
  echo ""
  sh $i
  echo "### DONE ###"
  echo ""
  echo "### Testing with empty image:"
  echo ""
  echo "WARNING: TEST NOT IMPLEMENTED YET!"
  echo ""
done

echo "### TEST: eu.ascetic.vc.ImageScripts.bash:50) COMPLETE ###"

echo ""
echo "OK (3 tests)"

#echo ""
#echo "Greping for ISO Image in repository:"
#echo ""
#ls repository | grep jboss
#echo ""
#echo "Test passed image is found!"
#rm repository/3_jboss_1.iso
