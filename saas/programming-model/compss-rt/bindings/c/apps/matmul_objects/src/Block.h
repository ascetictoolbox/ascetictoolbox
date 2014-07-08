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


#ifndef BLOCK_H
#define BLOCK_H

#include <vector>
#include <boost/archive/text_iarchive.hpp>
#include <boost/archive/text_oarchive.hpp>
#include <boost/serialization/serialization.hpp>
#include <boost/serialization/access.hpp>
#include <boost/serialization/vector.hpp>


using namespace std;
using namespace boost;
using namespace serialization;

class Block {

public:
	Block(){};

	Block(int bSize) {
		M = bSize;
		data.resize(M);
		for (int i=0; i<M; i++) {
			data[i].resize(M);
		}
	}

	static Block init(int bSize, double initVal) {
		Block *block = new Block(bSize);
		for (int i=0; i<bSize; i++) {
			for (int j=0; j<bSize; j++) {
				block->data[i][j] = initVal;
			}
		}
		return *block;
	}

	void multiply(Block block1, Block block2) {
		for (int i=0; i<M; i++) {
			for (int j=0; i<M; j++) {
				for (int k=0; k<M; k++) {
					data[i][j] += block1.data[i][k] * block2.data[k][j];
				}
			}
		}
	}

	void print() {
		for (int i=0; i<M; i++) {
			for (int j=0; j<M; j++) {
				cout << data[i][j] << " ";
			}
		}
	}

private:
	int M;
	std::vector< std::vector<double> > data;

	friend class::serialization::access;
	template<class Archive>
	void serialize(Archive & ar, const unsigned int version) {
		ar & M;
		ar & data;
	}
};

#endif
