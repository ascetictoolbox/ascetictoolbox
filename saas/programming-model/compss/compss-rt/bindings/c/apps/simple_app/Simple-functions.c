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


#include <stdio.h>
#include <errno.h>
#include "Simple.h"


void increment(char *filename)
{
  int counterValue=0;

   FILE *fp;

   fp = fopen(filename, "r");
   fscanf (fp,"%d",&counterValue);
   fclose(fp);

   counterValue++;

   fp = fopen(filename, "w");
   fprintf(fp, "%d", counterValue);
   fclose(fp);

}

