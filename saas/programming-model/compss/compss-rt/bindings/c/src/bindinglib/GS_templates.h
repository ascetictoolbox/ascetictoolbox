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


#ifndef GS_TEMPLATES_H
#define GS_TEMPLATES_H

#include <stdio.h>
#include <stdlib.h>
#include <fstream>
#include <iostream>
#include <map>
#include <string>
#include <string.h>
#include <boost/archive/text_iarchive.hpp>
#include <boost/archive/text_oarchive.hpp>

#include <GS_compss.h>
#include <param_metadata.h>

// Uncomment the following define to get debug information.
// #define DEBUG_CBINDING

#ifdef DEBUG_CBINDING
	#define debug_printf(args...) printf(args)
#else
	#define debug_printf(args...) {}
#endif

using namespace std;
using namespace boost;

struct Entry {
	 datatype type;
	 char *classname;
	 char *filename;
};

extern map<void *, Entry> objectMap;

void GS_register(void *ref, datatype type, char *classname, char *filename);

template <class T> char *GS_waitOn(T &obj);
template <> inline char *GS_waitOn<char *>(char * &obj);

template <class T>
char *GS_waitOn(T &obj) {
	 Entry entry = objectMap[&obj];
	 char *runtime_filename;

	 debug_printf("\n");
	 debug_printf("[   BINDING]  -  @GS_waitOn  - Entry.type: %d\n", entry.type);
	 debug_printf("[   BINDING]  -  @GS_waitOn  - Entry.classname: %s\n", entry.classname);
	 debug_printf("[   BINDING]  -  @GS_waitOn  - Entry.filename: %s\n", entry.filename);

	 GS_Get_File(entry.filename, 0, &runtime_filename);
	 debug_printf("[   BINDING]  -  Runtime filename: %s\n", runtime_filename);

     ifstream ifs(runtime_filename);
     archive::text_iarchive ia(ifs);

   	 ia >> obj;

   	 return runtime_filename;
}

template <>
char *GS_waitOn<char *>(char * &obj) {
     string in_string;

	 Entry entry = objectMap[&obj];
	 char *runtime_filename;

	 debug_printf("\n");
	 debug_printf("[   BINDING]  -  @GS_waitOn  -  Entry.type: %d\n", entry.type);
	 debug_printf("[   BINDING]  -  @GS_waitOn  -  Entry.classname: %s\n", entry.classname);
	 debug_printf("[   BINDING]  -  @GS_waitOn  -  Entry.filename: %s\n", entry.filename);

	 GS_Get_File(entry.filename, 0, &runtime_filename);
	 debug_printf("[   BINDING]  -  @GS_waitOn  -  Runtime filename: %s\n", runtime_filename);

	 if ((datatype)entry.type != file_dt) {

		 ifstream ifs(runtime_filename);
		 archive::text_iarchive ia(ifs);

		 ia >> in_string;
		 obj = strdup(in_string.c_str());
	 }

   	 return runtime_filename;
}

#endif /* GS_TEMPLATES */
