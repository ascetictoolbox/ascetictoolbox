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


#ifndef ADDRESS_h
#define ADDRESS_H

#include <string>
#include <boost/archive/text_iarchive.hpp>
#include <boost/archive/text_oarchive.hpp>
#include <boost/serialization/serialization.hpp>
#include <boost/serialization/access.hpp>

using namespace std;
using namespace boost;
using namespace serialization;

class address {

public:
	string name;
	int number;
	int flat;
	char letter;

	address(){};

private:
	friend class::serialization::access;
	template<class Archive>
	void serialize(Archive & ar, const unsigned int version) {
		ar & name;
		ar & number;
		ar & flat;
		ar & letter;
	}
};

#endif
