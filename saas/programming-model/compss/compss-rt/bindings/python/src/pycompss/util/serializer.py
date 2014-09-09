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

import os
from cPickle import load, dump, HIGHEST_PROTOCOL

def serialize_to_file(obj, file_name, force = False):
    if not os.path.exists(file_name) or force:
        f = open(file_name, 'wb')
        dump(obj, f, HIGHEST_PROTOCOL)
        f.close()
    return file_name
    
def deserialize_from_file(file_name):
    f = open(file_name, 'r')
    return load(f)

def serialize_objects(to_serialize):
    for target in to_serialize:
        obj = target[0]
        file_name = target[1]
        f = open(file_name, 'wb')
        dump(obj, f, HIGHEST_PROTOCOL)
