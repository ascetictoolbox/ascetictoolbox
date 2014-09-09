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
#include <string.h>
#include <vector>

#include "Matmul.h"
#include "Matrix.h"
#include "Block.h"

#define DEBUG_BINDING

int N;  //MSIZE
int M;	//BSIZE
double val;

int main(int argc, char **argv)
{
	Matrix A;
	Matrix B;
	Matrix C;

	if (argc < 2) {
		//TODO: usage
	} else {

		N = atoi(argv[1]);
		M = atoi(argv[2]);
		val = atof(argv[3]);

		GS_On();

		cout << "Running with the following parameters:\n";
		cout << " - N: " << N << "\n";
		cout << " - M: " << M << "\n";
		cout << " - val: " << val << "\n";

		initMatrix(&A,N,M,val);
		initMatrix(&B,N,M,val);
		initMatrix(&C,N,M,val);

		cout << "Waiting for initialization...\n";

		GS_waitOn(A);
		GS_waitOn(B);
		GS_waitOn(C);

		cout << "Initialization ends...\n";

		multiplyMatrices(&C, &A, &B);

		GS_Off();

		A.print();
		B.print();
		C.print();

	}

	return 0;
}
