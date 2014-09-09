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

from pycompss.api.parameter import *
from pycompss.util.logs import init_logging
from pycompss.util.serializer import *
from tempfile import mkdtemp
from shutil import rmtree
import types, os, re, inspect, logging

import compss


python_to_compss = {types.IntType : Type.INT, # int
                    types.LongType : Type.LONG, # long
                    types.FloatType : Type.FLOAT, # float
                    types.BooleanType : Type.BOOLEAN, # bool
                    types.StringType : Type.STRING, # str
                    #types.InstanceType : Type.OBJECT, # The type of instances of user-defined classes
                    #types.MethodType : Type.OBJECT, # The type of methods of user-defined class instances
                    #types.ClassType : Type.OBJECT, # The type of user-defined old-style classes
                    #types.ModuleType : Type.OBJECT, # The type of modules
                    types.TupleType : Type.OBJECT, # The type of tuples (e.g. (1, 2, 3, 'Spam'))
                    types.ListType : Type.OBJECT, # The type of lists (e.g. [0, 1, 2, 3])
                    types.DictType : Type.OBJECT # The type of dictionaries (e.g. {'Bacon': 1, 'Ham': 0})
                   }

temp_dir = mkdtemp(prefix = 'pycompss', dir = os.getcwd())

temp_obj_prefix = "/compss-serialized-obj_"

objid_to_filename = {}

task_objects = {}

# Objects that have been accessed by the main program
objs_written_by_mp = {} # obj_id -> compss_file_name

init_logging(os.getenv('IT_HOME') + '/bindings/python/log/logging.json')
logger = logging.getLogger(__name__)


class Function_Type:
    FUNCTION  = 1
    INSTANCE_METHOD = 2
    CLASS_METHOD = 3
    
class Future(object):
    pass


def start_runtime():
    compss.start_runtime()
    logger.info("COMPSs started")
    
def stop_runtime():
    clean_objects()
    compss.stop_runtime()
    clean_temps()
    logger.info("COMPSs stopped")

def get_file(file_name, mode):
    logger.debug("Getting file %s with mode %s" % (file_name, mode))
    compss_name = compss.get_file(file_name, mode)
    logger.debug("COMPSs file name is %s" % compss_name)
    return compss_name

def synchronize(obj, mode):
    logger.debug("Synchronizing object %d with mode %s" % (id(obj), mode))
    
    obj_id = id(obj)
    if obj_id not in task_objects:
        return obj
    
    file_name = objid_to_filename[obj_id]
    compss_file = compss.get_file(file_name, mode)
    
    new_obj = deserialize_from_file(compss_file)
    new_obj_id = id(new_obj)
    
    # The main program won't work with the old object anymore, update mapping
    objid_to_filename[new_obj_id] = file_name
    task_objects[new_obj_id] = new_obj
     # Do not let python free old objects until compss_stop, otherwise python could reuse object ids
    #del objid_to_filename[obj_id]
    #del task_objects[obj_id]
    
    logger.debug("Now object with id %d and %s has mapping %s" % (new_obj_id, type(new_obj), file_name))
    
    if mode != Direction.IN:
        objs_written_by_mp[new_obj_id] = compss_file
    
    return new_obj
    
def process_task(f, # Function or method
                 ftype, # Function type
                 spec_args, # Names of the task arguments
                 class_name, # Name of the class (if method)
                 module_name, # Name of the module containing the function/method (including packages, if any)
                 task_args, # Unnamed arguments
                 task_kwargs, # Named arguments
                 deco_kwargs): # Decorator arguments
    
    logger.debug("TASK: %s of type %s, in module %s, in class %s" % (f.__name__, ftype, module_name, class_name))
    
    first_par = 0
    if ftype == Function_Type.INSTANCE_METHOD:
        has_target = True
    else:
        has_target = False
        if ftype == Function_Type.CLASS_METHOD:
            first_par = 1 # skip class parameter
    
    ret_type = deco_kwargs['returns'] 
    if ret_type:
        # Create future for return value
        if ret_type in python_to_compss: # primitives, string, dic, list, tuple
            fu = Future()
        elif inspect.isclass(ret_type):
            # For objects, type of future has to be specified to allow o = func; o.func
            try:
                fu = ret_type()
            except TypeError:
                logger.warning("Type %s does not have an empty constructor, building generic future object" % ret_type)
                fu = Future()
        else:
            fu = Future() # modules, functions, methods
        
        logger.debug("Setting object %d of %s as a future" % (id(fu), type(fu)))
        
        obj_id = id(fu)
        ret_filename = temp_dir + temp_obj_prefix + str(obj_id)
        objid_to_filename[obj_id] = ret_filename
        task_objects[obj_id] = fu
        task_kwargs['compss_retvalue'] = ret_filename
    else:
        fu = None
    
    app_id = 0
 
    if class_name == '':
	path = module_name
    else:
        path = module_name + '.' + class_name
    
    # Infer COMPSs types from real types, except for files
    num_pars = len(spec_args)
    is_future = {}
    for i in range(first_par, num_pars):
        spec_arg = spec_args[i]
        p = deco_kwargs.get(spec_arg)
        if p == None:
            logger.debug("Adding default decoration for parameter %s" % spec_arg)
            p = Parameter()
            deco_kwargs[spec_arg] = p
        if i < len(task_args):
            p.value = task_args[i]
        else:
            p.value = task_kwargs[spec_arg]
        
        val_type = type(p.value)
        is_future[i] = (val_type == Future)
        logger.debug("Parameter " + spec_arg + "\n" + 
                     "\t- Value type: " + str(val_type) + "\n" +
                     "\t- User-defined type: " + str(p.type))

        # Infer type if necessary
        if p.type == None:
            p.type = python_to_compss.get(val_type)
            if p.type == None:
                p.type = Type.OBJECT
            logger.debug("\n\t- Inferred type: %d" % p.type)
        
        # Serialize objects into files
        if p.type == Type.OBJECT or is_future.get(i): # 2nd condition: real type can be primitive, but now it's acting as a future (object)
            turn_into_file(p)
	elif p.type == Type.INT:
	    if p.value > JAVA_MAX_INT or p.value < JAVA_MIN_INT:
		p.type = Type.LONG  # this must go through Java as a long to prevent overflow with Java int
	elif p.type == Type.LONG:
	    if p.value > JAVA_MAX_LONG or p.value < JAVA_MIN_LONG:
		p.type = Type.OBJECT # this must be serialized to prevent overflow with Java long
		turn_into_file(p)
        
        logger.debug("Final type for parameter %s: %d" % (spec_arg, p.type))
    
    # Build values and COMPSs types and directions
    values = []
    compss_types = []
    compss_directions = []
    if ftype == Function_Type.INSTANCE_METHOD:
        ra = range(1, num_pars)
        ra.append(0) # callee is the last
    else:
        ra = range(first_par, num_pars)
    for i in ra:
        spec_arg = spec_args[i]
        p = deco_kwargs[spec_arg]
        values.append(p.value)
        if p.type == Type.OBJECT or is_future.get(i):
            compss_types.append(Type.FILE)
        else:
            compss_types.append(p.type)
        compss_directions.append(p.direction)
    
    # Priority
    has_priority = deco_kwargs['priority']
    
    if logger.isEnabledFor(logging.DEBUG):
        values_str = ''
        types_str = ''
        direct_str = ''
        for v in values:
            values_str += str(v) + " "
        for t in compss_types:
            types_str += str(t) + " "
        for d in compss_directions:
            direct_str += str(d) + " "
        logger.debug("Processing task:\n" +
                     "\t- App id: " + str(app_id) + "\n" +
                     "\t- Path: " + path + "\n" +
                     "\t- Function name: " + f.__name__ + "\n" +
                     "\t- Priority: " + str(has_priority) + "\n" +
                     "\t- Has target: " + str(has_target) + "\n" +
                     "\t- Num params: " + str(num_pars) + "\n" +
                     "\t- Values: " + values_str + "\n" +
                     "\t- COMPSs types: " + types_str + "\n" +
                     "\t- COMPSs directions: " + direct_str)
    
    compss.process_task(app_id,
                        path,
                        f.__name__,
                        has_priority,
                        has_target,
                        values, compss_types, compss_directions)
    
    return fu


def turn_into_file(p):
    obj_id = id(p.value)
    file_name = objid_to_filename.get(obj_id)
    if file_name == None:
        # This is the first time a task accesses this object
        task_objects[obj_id] = p.value
        file_name = temp_dir + temp_obj_prefix + str(obj_id)
        objid_to_filename[obj_id] = file_name
        logger.debug("Mapping object %d to file %s" % (obj_id, file_name))
        serialize_to_file(p.value, file_name)
    elif obj_id in objs_written_by_mp:
        # Main program generated the last version
        compss_file = objs_written_by_mp.pop(obj_id)
        logger.debug("Serializing object %d to file %s" % (obj_id, compss_file))
        serialize_to_file(p.value, compss_file, True)
    p.value = file_name
    
def get_compss_mode(pymode):
    if pymode.startswith('w'):
        return Direction.OUT
    elif pymode.startswith('r+') or pymode.startswith('a'):
        return Direction.INOUT
    else:
        return Direction.IN

def clean_objects():
    task_objects.clear()
    objid_to_filename.clear()
    objs_written_by_mp.clear()
    
def clean_temps():
    rmtree(temp_dir, True)
    cwd = os.getcwd()
    for f in os.listdir(cwd):
        if re.search("d\d+v\d+_\d+\.IT", f):
            os.remove(os.path.join(cwd, f))
