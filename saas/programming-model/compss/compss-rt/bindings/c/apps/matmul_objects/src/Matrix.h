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


#ifndef MATRIX_H
#define MATRIX_H

#include <vector>
#include <boost/archive/text_iarchive.hpp>
#include <boost/archive/text_oarchive.hpp>
#include <boost/serialization/serialization.hpp>
#include <boost/serialization/access.hpp>
#include <boost/serialization/vector.hpp>

#include "Block.h"

using namespace std;
using namespace boost;
using namespace serialization;

class Matrix {

public:
	Matrix(){};

	Matrix(int mSize) {
		N = mSize;
		data.resize(N);
		for (int i=0; i<N; i++) {
			data[i].resize(N);
		}
	}

	static Matrix init(int mSize, int bSize, double val) {
		Matrix *matrix = new Matrix(mSize);
		for (int i=0; i<mSize; i++) {
			for (int j=0; j<mSize; j++) {
				matrix->data[i][j] = Block::init(bSize, val);
			}
		}
		return *matrix;
	}

	void multiply(Matrix matrix1, Matrix matrix2) {
		for (int i=0; i<N; i++) {
			for (int j=0; j<N; j++) {
				for (int k=0; k<N; k++) {
					data[i][j].multiply(matrix1.data[i][k], matrix2.data[k][j]);
				}
			}
		}
	}

	void print() {
		for (int i=0; i<N; i++) {
			for (int j=0; j<N; j++) {
				data[i][j].print();
			}
			cout << "\r\n";
		}
	}

private:
	int N;
	std::vector< std::vector<Block> > data;

	friend class::serialization::access;
	template<class Archive>
	void serialize(Archive & ar, const unsigned int version) {
		ar & N;
		ar & data;
	}
};

#endif
