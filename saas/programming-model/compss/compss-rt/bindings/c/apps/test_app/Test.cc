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
#include <string.h>
#include <assert.h>

#include "Test.h"
#include "Test-constants.h"

#define DEBUG_BINDING

int main(int argc, char **argv)
{
	printf("*--------------------------------------------------------------------*\n");
	printf("*                                                                    *\n");
	printf("*     Test for COMP Superscalar C/C++ Binding...                     *\n");
	printf("*                                                                    *\n");
	printf("*     This test will test primitive types and c++ objects.           *\n");
	printf("*                                                                    *\n");
	printf("*                                                                    *\n");
	printf("*     Support: carlos.diaz@bsc.es                                    *\n");
	printf("*                                                                    *\n");
	printf("*     IMPORTANT: input parameters are tested in the worker           *\n");
	printf("*                so look in the job .err files.                      *\n");
	printf("*                                                                    *\n");
	printf("*--------------------------------------------------------------------*\n");
	printf("\n");

	FILE *fp;
	file filename = strdup(TEST_VALUE_FILE);

	GS_On();

	fp = fopen(filename, "w");
	
	fprintf(fp, "\n");
	fprintf(fp, "[   TEST] Test of C types within COMPSs.\n");
	fprintf(fp, "\n");

	fclose(fp);

	// Char (char_dt, wchar_dt)
	char c = TEST_VALUE_CHAR;
	char_size(c, &filename);
	printf("[   TEST] Testing {in} char inside a task.\n");

	// String (string_dt)
	char *s = strdup(TEST_VALUE_STRING);
	string_size(s, &filename);
	printf("[   TEST] Testing {in} string inside a task.\n");

	// Integer (int_dt)
	int i = TEST_VALUE_INT;
	int_size(i, &filename);
	printf("[   TEST] Testing {in} int inside a task.\n");

	// Short (short_dt)
	short si = TEST_VALUE_SHORT;
	short_size(si, &filename);
	printf("[   TEST] Testing {in} short inside a task.\n");

	// Long (long_dt)
	long li = TEST_VALUE_LONG;
	long_size(li, &filename);
	printf("[   TEST] Testing {in} long inside a task.\n");

	// Float (float_dt)
	float f = TEST_VALUE_FLOAT;
	float_size(f, &filename);
	printf("[   TEST] Testing {in} float inside a task.\n");

	// Double (double_dt)
	double d = TEST_VALUE_DOUBLE;
	double_size(d, &filename);
	printf("[   TEST] Testing {in} double inside a task.\n");

	// Boolean (boolean_dt)
	int b = TEST_VALUE_BOOLEAN;
	boolean_size(b, &filename);
	printf("[   TEST] Testing {in} boolean inside a task.\n");

	// File (file_dt)
	string line;
	ifstream output (GS_waitOn(filename));
	if (output.is_open())
	{
	    while ( getline (output, line) )
	    {
	      cout << line << endl;
	    }
	    output.close();
 	}
	else cout << "[  TEST] Unable to open output file.";


	// Out parameters
	out_parameters(&c, &s, &i, &si, &li, &f, &d, &b, &filename);

	printf("[   TEST] Testing {in-out} char parameter...................");
	GS_waitOn(c);
	assert_master(c == TEST_VALUE_CHAR2);
	printf("\tTEST PASSED.\n\n");

    printf("[   TEST] Testing {in-out} string parameter.................");
	GS_waitOn(s);
	assert_master(strcmp(s,TEST_VALUE_STRING2) == 0);
	printf("\tTEST PASSED.\n\n");

	printf("[   TEST] Testing {in-out} int parameter....................");
	GS_waitOn(i);
	assert_master(i == TEST_VALUE_INT2);
	printf("\tTEST PASSED.\n\n");

	//student (object_dt)
	student st;
	st.name = "Carlos";
	st.surname = "Díaz";
	st.age = 34;
	st.domicile.name = "Carrer de Sant Elm";
	st.domicile.number = 75;
	st.domicile.flat = 5;
	st.domicile.letter = 'B';
	objects(&st);

	printf("[   TEST] Testing {in-out} object parameter.................");
	GS_waitOn(st);
	assert_master(st.name == "Ana");
	assert_master(st.surname == "Suárez");
	assert_master(st.age == 31);
    printf("\tTEST PASSED.\n\n");

	GS_Off();

	return 0;
}

