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


#include <time.h>
#include <stdio.h>
#include <errno.h>

#include "GS_compss.h"
#include "Simple.h"

int main(int argc, char **argv)
{

	long int t = time(NULL);
	FILE *fp;
	char filename[15]="counter.txt";
	int initialValue = 1;
	int finalValue=0;

	GS_On(PRJ_FILE, RES_FILE, MASTER_DIR, APPNAME);

	fp = fopen(filename, "w");
	
	fprintf(fp, "%d", initialValue);
	printf("Initial Counter Value is: %d \n", initialValue);
	fclose(fp);

	increment(filename);

	GS_Off(0);

	fp = fopen(filename, "r");
        fscanf (fp,"%d",&finalValue);
        printf("Final Counter Value is: %d \n", finalValue);
        fclose(fp);
	
	printf("Total time:\n");
	t = time(NULL) - t;
	printf("%li Hours, %li Minutes, %li Seconds\n", t / 3600, (t % 3600) / 60,
			 (t % 3600) % 60);

	return 0;
}

