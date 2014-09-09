#
#  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


'''
@author: etejedor
'''

from pycompss.runtime.binding import start_runtime, stop_runtime, get_file, synchronize, get_compss_mode


def compss_start():
    start_runtime()
    
def compss_stop():
    stop_runtime()

def compss_open(file_name, mode = 'r'):
    compss_mode = get_compss_mode(mode)
    compss_name = get_file(file_name, compss_mode)
    return open(compss_name, mode)

def compss_wait_on(obj, to_write = True):
    #print "Waiting on", obj
    if to_write:
        mode = 'r+'
    else:
        mode = 'r'
    compss_mode = get_compss_mode(mode)
    return synchronize(obj, compss_mode)
    
