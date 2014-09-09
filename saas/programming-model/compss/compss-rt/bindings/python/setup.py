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

from distutils.core import setup, Extension
from distutils.command.install_lib import install_lib
from distutils import log

#import os

compssmodule = Extension('compss',
        include_dirs = [
                '../bindings-common/src',
		'../bindings-common/include'
		],
        library_dirs = [
		'../bindings-common/lib'
		],
        libraries = ['bindings_common'],
        extra_compile_args = ['-fPIC'],
        sources = ['src/ext/compssmodule.c'])

#class compss_install_lib(install_lib):
#	def run(self):
#		install_lib.run(self)
#		for fn in self.get_outputs():
#			if fn.endswith('worker_python.sh'):
				# Make worker_python.sh executable
#		       		mode = ((os.stat(fn).st_mode) | 0555) & 07777
#		        	log.info("changing mode of %s to %o", fn, mode)
#		        	os.chmod(fn, mode)

setup (name = 'PyCOMPSs',
        version = '1.0',
        description = 'Python binding of the COMPSs runtime',
	long_description=open('README.txt').read(),
	author = 'Enric Tejedor',
	author_email = 'enric.tejedor@bsc.es',
	url = 'http://www.bsc.es/compss',
	license = 'Apache 2.0',
	packages = ['', 'pycompss', 'pycompss.api', 'pycompss.runtime', 'pycompss.worker', 'pycompss.util'],
	package_dir = {'pycompss' : 'src/pycompss'},
	#package_data = {'' : ['bin/worker_python.sh', 'log/logging.json']},
	package_data = {'' : ['log/logging.json']},
	#cmdclass = {'install_lib' : compss_install_lib},
        ext_modules = [compssmodule])
