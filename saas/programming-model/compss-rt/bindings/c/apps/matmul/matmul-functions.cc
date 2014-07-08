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
#include <stdio.h>
#include <math.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include "matmul.h"

#ifndef NULL
#define NULL 0
#endif
#define FALSE 0
#define TRUE 1


#include "parameters.h"


typedef struct {
	int rows;
	int cols;
	double **data;
} block;


static block *new_initialized_block(int dim1, int dim2, double val) {
	block *result;
	int i, j;
	
	result = (block *)malloc(sizeof(block));
	result->rows = dim1;
	result->cols = dim2;
	result->data = (double **)malloc(sizeof(double *) * dim1);
	
	for (i = 0; i < dim1; i++) {
		result->data[i] = (double *)malloc(sizeof(double) * dim2);
		for (j = 0; j < dim2; j++) {
			result->data[i][j] = val;
		}
	}
	
	return result;
}


static block *new_zero_block(int dim1, int dim2) {
	return new_initialized_block(dim1, dim2, 0.0);
}


static void delete_block(block *b) {
	int i;
	
	for (i = 0; i < b->rows; i++) {
		free(b->data[i]);
	}
	free(b->data);
	free(b);
}


static block *block_mul(block *A, block *B, block *C) {
/* Pre: The three parameters must exist */
	int i, j, k;
	
	for (i = 0; i < A->rows; i++) {
		for (j = 0; j < B->cols; j++) {
			for (k = 0; k < A->cols; k++) {
				C->data[i][j] += A->data[i][k] * B->data[k][j];
			}
		}
	}
	
	return C;
}


static block *get_block(char *file, int rows, int cols) {
	block *result;
	FILE *fp;
	int i, j;

	result = new_zero_block(rows, cols);
	if ((fp = fopen(file, "r")) == NULL) {
		perror("ERROR opening to read");
	}
	
	for (i = 0; i < result->rows; i++) {
		for (j = 0; j < result->cols; j++) {
			if (fscanf(fp, "%lf ", &(result->data[i][j])) != 1) {
				perror("ERROR reading");
			}
		}
		fscanf(fp, " \n");
	}
	fclose(fp);
	
	return result;
}


static void put_block(block *b, char *file) {
/* Pre: b cannot be NULL */
	FILE *fp;
	int i, j;

	if ((fp = fopen(file , "w")) == NULL) {
		perror("ERROR opening to write");
	}
	
	for (i = 0; i < b->rows; i++) {
		for (j = 0; j < b->cols; j++) {
			if (fprintf(fp, "%lf ", b->data[i][j]) <= 0) {
				perror("ERROR writing");
			}
		}
		fprintf(fp, " \n");
	}
	fclose(fp);
}


void matmul(char *f1, char *f2, char **f3)
{
	block *A;
	block *B;
	block *C;

	A = get_block(f1, BSIZE, BSIZE);
	B = get_block(f2, BSIZE, BSIZE);
	C = get_block(*f3, BSIZE, BSIZE);

	block_mul(A, B, C);
	put_block(C, *f3); //A and B are sources
	
	delete_block(A);
	delete_block(B);
	delete_block(C);
}
