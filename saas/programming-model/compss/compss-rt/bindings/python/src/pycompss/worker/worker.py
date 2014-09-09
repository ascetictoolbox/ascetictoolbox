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

# Args: debug full_path(method_class) method_name has_target num_params par_type_1 par_1 ... par_type_n par_n

import os, sys, traceback, logging
if sys.version_info >= (2, 7):
    import importlib
from pycompss.util.logs import init_logging
from pycompss.api.parameter import Type, JAVA_MAX_INT, JAVA_MIN_INT


def compss_worker():
    logger = logging.getLogger('pycompss.worker.worker')
    
    args = sys.argv[1:]
    
    verbose = args[0]
    path = args[1]
    method_name = args[2]
    has_target = args[3]
    num_params = int(args[4])
    
    args = args[5:]
    pos = 0
    values = []
    types = []
    for i in range(0, num_params):
        ptype = int(args[pos])
        types.append(ptype)
        
        if (ptype == Type.FILE):
            values.append(args[pos+1])
        elif (ptype == Type.STRING):
            num_substrings = int(args[pos+1])
            aux = ''
            for j in range (2, num_substrings + 2):
                aux += args[pos + j]
                if (j < num_substrings + 1):
                    aux += ' '
            values.append(aux)
            pos += num_substrings 
        elif (ptype == Type.INT):
            values.append(int(args[pos+1]))
        elif (ptype == Type.LONG):
            l = long(args[pos+1])
            if l > JAVA_MAX_INT or l < JAVA_MIN_INT:
                # A Python int was converted to a Java long to prevent overflow
                # We are sure we will not overflow Python int, otherwise this would have been passed as a serialized object
                l = int(l)
            values.append(l)
        elif (ptype == Type.FLOAT):
            values.append(float(args[pos+1]))
        elif (ptype == Type.BOOLEAN):
            if args[pos+1] == 'true':
                values.append(True)
            else:
                values.append(False)
        #elif (ptype == Type.OBJECT):
        #    pass
        else:
            logger.fatal("Invalid type (%d) for parameter %d" % (ptype, i))
            exit(1)
            
        pos = pos + 2
        
    if logger.isEnabledFor(logging.DEBUG):
        values_str = ''
        types_str = ''
        for v in values:
            values_str += "\t\t" + str(v) + "\n"
        for t in types:
            types_str += str(t) + " "
        logger.debug("RUN TASK with arguments\n" +
                "\t- Path: " + path + "\n" +
                "\t- Method/function name: " + method_name + "\n" +
                "\t- Has target: " + has_target + "\n" +
                "\t- # parameters: " + str(num_params) + "\n" +
                "\t- Values:\n" + values_str +
                "\t- COMPSs types: " + types_str)

    try:
        # Try to import the module (for functions)
        if sys.version_info >= (2, 7):
            module = importlib.import_module(path) # Python 2.7
        else:
            module = __import__(path, globals(), locals(), [path], -1)
        getattr(module, method_name)(*values, compss_types = types)
    except ImportError:
        from cPickle import load, dump, HIGHEST_PROTOCOL
        # Not the path of a module, it ends with a class name
        class_name = path.split('.')[-1]
        module_name = path.replace('.' + class_name, '')
        module = __import__(module_name, fromlist=[class_name])
        klass = getattr(module, class_name)
        
        logger.debug("Method in class %s of module %s" % (class_name, module_name))
        
        if (has_target == 'true'):
            # Instance method
            file_name = values.pop()
            f = open(file_name, 'rb')
            obj = load(f)
            logger.debug("Processing callee, a hidden object of %s in file %s" % (file_name, type(obj)))
            values.insert(0, obj)
            types.pop()
            types.insert(0, Type.OBJECT)
            f.close()
            
            getattr(klass, method_name)(*values, compss_types = types)
            
            f = open(file_name, 'wb')
            dump(obj, f, HIGHEST_PROTOCOL)
            f.close()
        else:
            # Class method - class is not included in values (e.g. values = [7])
            types.insert(0, None) # class must be first type
            getattr(klass, method_name)(*values, compss_types = types)
    except Exception:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        lines = traceback.format_exception(exc_type, exc_value, exc_traceback)
        logger.exception("WORKER EXCEPTION")
        logger.exception(''.join(line for line in lines))
        exit(1)
        

if __name__ == "__main__":
    init_logging(os.getenv('PYCOMPSS_HOME') + '/log/logging.json')
    compss_worker()
