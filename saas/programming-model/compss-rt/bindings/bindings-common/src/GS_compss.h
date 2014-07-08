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



#ifndef GS_COMPSS_H
#define GS_COMPSS_H

/*** ==============> API FUNCTIONS <================= ***/
extern "C" void GS_On(void);
extern "C" void GS_Off(void);
extern "C" void GS_ExecuteTask(long appId, char *class_name, char *method_name, int priority, int has_target, int num_params, void **params);
extern "C" void GS_Get_File(char *file_name, int mode, char **buf);

#endif /* GS_COMPSS_H */
