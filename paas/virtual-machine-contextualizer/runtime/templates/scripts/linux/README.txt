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

NOTES:

1) Be sure to add the init scripts before networking is started. If you're 
not sure how, read up on the use of "chkconfig" for redhat distros and 
"update-rc.d" or "insserv" for debian distros.

2) The context scripts require bash to be used as the default shell during 
init.d execution (not dash). For example, in debian based linux 
distributions to use bash instead of dash execute:

dpkg-reconfigure dash

and select "No"

3) Since Ubuntu like to reinvent the wheel and has decided to create and 
use upstart on their server distros, the context_network script will fail 
to start before networking is brought online when using LSB headers and 
update-rc.d. To resolve this issue you will need to alter the following file 
instead of using update-rc.d:

/etc/init/networking.conf

	replace:
    "pre-start exec mkdir -p /var/run/network"
	
	with:
    "pre-start script
        /etc/init.d/context_network
        mkdir -p /var/run/network
    end script"

Similiar changes maybe needed for /etc/init/network-interface.conf depending 
on your usage.