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



#ifndef PARAM_METADATA_H
#define PARAM_METADATA_H

enum datatype {file_dt=0, boolean_dt, char_dt, string_dt, byte_dt, short_dt, int_dt, long_dt,
			   float_dt, double_dt, object_dt, wchar_dt, wstring_dt, longlong_dt, void_dt, any_dt, null_dt};

enum direction {in_dir=0, out_dir, inout_dir, null_dir};

#endif /* PARAM_METADATA_H */
