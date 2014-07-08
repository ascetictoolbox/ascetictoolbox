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

import inspect
import os
#import logging


class task(object):
    
    #logger = logging.getLogger(__name__)

    def __init__(self, *args, **kwargs):
        """
        If there are decorator arguments, the function
        to be decorated is not passed to the constructor!
        """
        self.args = args
        self.kwargs = kwargs # these are the only ones actually used (decorator)
        self.is_instance = False
        if 'isModifier' not in self.kwargs:
            self.kwargs['isModifier'] = True
        if 'returns' not in self.kwargs:
            self.kwargs['returns'] = False
        if 'priority' not in self.kwargs:
            self.kwargs['priority'] = False
        
        # Pre-process decorator arguments    
        from pycompss.api.parameter import Parameter, Type, Direction
        import copy
        
        if not inspect.stack()[-2][3] == 'compss_worker':
            for arg_name in self.kwargs.keys():
                if (arg_name not in ['isModifier', 'returns', 'priority']):
                    # Prevent p.value from being overwritten later by ensuring each Parameter is a separate object
                    p = self.kwargs[arg_name]
                    pcopy = copy.copy(p) # shallow copy
                    self.kwargs[arg_name] = pcopy
        
        if self.kwargs['isModifier']:
            d = Direction.INOUT
        else:
            d = Direction.IN
        self.kwargs['self'] = Parameter(p_type = Type.OBJECT, p_direction = d) # add callee object parameter
        if self.kwargs['returns']:
            self.kwargs['compss_retvalue'] = Parameter(p_type = Type.FILE, p_direction = Direction.OUT) 
            
        
    def __call__(self, f):
        """
        If there are decorator arguments, __call__() is only called
        once, as part of the decoration process! You can only give
        it a single argument, which is the function object.
        """
        
        # Assume it is an instance method if the first parameter of the function is called 'self'
        # "I would rely on the convention that functions that will become methods have a first argument named self, and other functions don't. Fragile, but then, there's no really solid way."
        self.spec_args = inspect.getargspec(f)
        if self.spec_args and self.spec_args[0][0] == 'self':
            self.is_instance = True
        if self.kwargs['returns']:
            self.spec_args[0].append('compss_retvalue')
        
        # Get module (for invocation purposes in the worker)
        mod = inspect.getmodule(f)
        self.module = mod.__name__
        if self.module == '__main__': # the module where the function is defined was run as __main__, we need to find out the real module name
            #path = mod.__file__
            #dirs = mod.__file__.split(os.sep)
            #file_name = os.path.splitext(os.path.basename(mod.__file__))[0]
            path = getattr(mod, "app_path") # get the real module name from our launch.py variable
            dirs = path.split(os.sep)
            file_name = os.path.splitext(os.path.basename(path))[0]
            mod_name = file_name
            i = -1
            while True:
		new_l = len(path) - (len(dirs[i]) + 1)
		path = path[0:new_l]
                if "__init__.py" in os.listdir(path):
                    # directory is a package
                    i = i - 1
                    mod_name = dirs[i] + '.' + mod_name
                else:
                    break
            self.module = mod_name
        
        #logger.debug("Decorating function %s in module %s" % (f.__name__, self.module))

        def wrapped_f(*args, **kwargs):
            if inspect.stack()[-2][3] == 'compss_worker': # called from worker code, run the method
                from pycompss.util.serializer import serialize_objects
                
                returns = self.kwargs['returns']
                
                # Discover hidden objects passed as files
                real_values, to_serialize = reveal_objects(args, self.spec_args[0], self.kwargs, kwargs['compss_types'], returns)
                
                ret = f(*real_values)
                #f(*args, **kwargs)
                
                if returns:
                    ret_filename = args[-1]
                    to_serialize.append((ret, ret_filename))
                
                if len(to_serialize) > 0:
                    serialize_objects(to_serialize)
            else:
                from pycompss.runtime.binding import process_task, Function_Type

                # Check the type of the function called
                # inspect.ismethod(f) does not work here, for methods python hasn't wrapped the function as a method yet
                # Everything is still a function here, can't distinguish yet with inspect.ismethod or isfunction
                ftype = Function_Type.FUNCTION
                class_name = ''
                if self.is_instance:
                    ftype = Function_Type.INSTANCE_METHOD
                    class_name = type(args[0]).__name__
                elif args and inspect.isclass(args[0]):
                    for n, _ in inspect.getmembers(args[0], inspect.ismethod):
                        if n == f.__name__:
                            ftype = Function_Type.CLASS_METHOD
                            class_name = args[0].__name__
                
                return process_task(f, ftype, self.spec_args[0], class_name, self.module, args, kwargs, self.kwargs)
        
        return wrapped_f
    

def reveal_objects(values, spec_args, deco_kwargs, compss_types, returns):
    from pycompss.api.parameter import Parameter, Type, Direction
    from cPickle import load

    num_pars = len(spec_args)
    real_values = []
    to_serialize = []
    
    if returns:
        num_pars -= 1 # return value must not be passed to the function call
    
    for i in range(num_pars):
        spec_arg = spec_args[i]
        compss_type = compss_types[i]
        value = values[i]
        if i == 0:
            if spec_arg == 'self': # callee object
                if deco_kwargs['isModifier']:
                    d = Direction.INOUT
                else:
                    d = Direction.IN
                deco_kwargs[spec_arg] = Parameter(p_type = Type.OBJECT, p_direction = d)
            elif inspect.isclass(value): # class (it's a class method)
                real_values.append(value)
                continue
        
        p = deco_kwargs.get(spec_arg)
        if p == None: # decoration not present, using default
            p = Parameter()
            #deco_kwargs[spec_arg] = p
        
        if compss_type == Type.FILE and p.type != Type.FILE:
            # For COMPSs it is a file, but it is actually a Python object
            #logger.debug("Processing a hidden object in parameter %d", i)
            try:
                f = open(value, 'rb')
            except IOError as e:
                raise Exception("Error opening serialized object file" + value + "- I/O error({0}): {1}".format(e.errno, e.strerror))
            try:
                obj = load(f)
            except ImportError, e:
                raise Exception("Error deserializing object from file" + value, e)
                    
            real_values.append(obj)
            if (p.direction != Direction.IN):
                to_serialize.append((obj, value))
        else:
            real_values.append(value)
    
    return real_values, to_serialize

    
