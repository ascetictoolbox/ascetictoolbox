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

from pycompss.api.task import task
from pycompss.api.parameter import *

##### CLASS #####

class MyClass(object):

    static_field = 'value of static field'
    
    def __init__(self, field = None):
    #def __init__(self, *args, **kwargs):
        self.field = field

    @task()
    def instance_method(self):
        print "TEST"
        print "- Instance method"
        print "- Callee object:", self.field
        self.field = self.field * 2

    @task(isModifier = False)
    def instance_method_nonmodifier(self):
        print "TEST"
        print "- Instance method (nonmodifier)"
        print "- Callee object:", self.field

    @classmethod
    @task()
    def class_method(cls):
        print "TEST"
        print "- Class method of class", cls
        print "- Static field:", cls.static_field



##### FUNCTIONS #####

@task()
def function_primitives(i, l, f, b, s):
    print "TEST"
    print "- Static Function"
    print "- Primitive params: %d, %ld, %f, %d, %s" % (i, l, f, b, s)
    
#@task(fin    = Parameter(p_type = Type.FILE),
#      finout = Parameter(p_type = Type.FILE, p_direction = Direction.INOUT),
#      fout   = Parameter(p_type = Type.FILE, p_direction = Direction.OUT))
@task(fin = FILE, finout = FILE_INOUT, fout = FILE_OUT)
def function_files(fin, finout, fout):
    print "TEST"
    print "- Static Function"
    
    fin_d = open(fin, 'r')
    finout_d = open(finout, 'r+')
    fout_d = open(fout, 'w')
    
    print "- In file content:\n", fin_d.read()
    print "- Inout file content:\n", finout_d.read()
    finout_d.write("\n===> INOUT FILE ADDED CONTENT")
    fout_d.write("OUT FILE CONTENT")
    
    fin_d.close()
    finout_d.close()
    fout_d.close()

def par_func():
    print "- Function"

#@task(o = Parameter(p_direction = Direction.INOUT))
@task(o = INOUT)
def function_objects(o, l, dic, tup, cplx, f):
    print "TEST"
    print "- Static Function"
    print "- MyClass object", o.field
    print "- List", l
    print "- Dictionary", dic
    print "- Tuple", tup
    print "- Complex", cplx
    f()
    
    o.field = o.field * 2

@task(returns = int)
def function_return_primitive(i):
    print "TEST"
    print "- Static Function"
    print "\t- Parameter:", i
    return i * 2

@task(returns = MyClass)
def function_return_object(i):
    o = MyClass(i)
    print "TEST"
    print "- Static Function"
    print "\t- Parameter:", o
    return o



