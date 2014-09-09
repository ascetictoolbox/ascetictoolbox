/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */



#include "GS_templates.h"


using namespace std;
using namespace boost;

map<void *, Entry> objectMap;


void GS_register(void *ref, datatype type, char *classname, char *filename) {
	 Entry entry;
	 entry.type = type;
	 entry.classname = strdup(classname);
	 entry.filename = strdup(filename);

	 debug_printf("[   BINDING]  -  @GS_register  -  Entry.type: %d\n", entry.type);
	 debug_printf("[   BINDING]  -  @GS_register  -  Entry.classname: %s\n", entry.classname);
	 debug_printf("[   BINDING]  -  @GS_register  -  Entry.filename: %s\n", entry.filename);

	 objectMap[ref] = entry;
}
