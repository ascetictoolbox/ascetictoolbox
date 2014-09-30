var TreeOperator = require("../utils/TreeOperator")

exports.testAddition = function(test) {

	var tree1 = { "objectProperty" :
					{ "someValue" : 2, "someotherValue" : 6 },
                          "arrayProperty" : [1,2,3,4],
                          "numberProperty" : 3,
                          "stringProperty" : "56.77" };

	var tree2 = { "objectProperty" :
                    { "someValue" : 1, "someotherValue" : 1 },
                        "arrayProperty" : [0,1,1,1],
                        "numberProperty" : 10,
                        "stringProperty" : "1" };

    var r = TreeOperator.add(tree1,tree2);

    test.equal(r.objectProperty.someValue,3);
    test.equal(r.objectProperty.someotherValue,7);
	test.equal(r.arrayProperty[0],1);
	test.equal(r.arrayProperty[3],5);
    test.equal(r.numberProperty, 13);
    test.equal(r.stringProperty, 57.77);

	test.done();
}

exports.testDifferentTrees = function(test) {
	var tree1 = { "objectProperty" :
					{ "someValue" : 2, "someotherValue" : { "leave1":0, "leave2": 0} },
						"arrayProperty" : [1,2,3,4],
						"numberProperty" : { "leave1":0, "leave2": 0},
						"stringProperty" : "56.77" };

	var tree2 = { "objectProperty" :
					{ "someValue" : 2 },
						"arrayProperty" : [1,2,3,4],
						"numberProperty" : { "leave1":0, "leave2": 0},
						"stringProperty" : "56.77" };

	var r = TreeOperator.add(tree1,tree2);

	test.equal(r.objectProperty.someValue,4);
	test.equal(r.objectProperty.hasOwnProperty("someotherValue"), false);
	test.equal(r.arrayProperty[0],2);
	test.equal(r.arrayProperty[3],8);
	test.equal(r.numberProperty.leave1, 0);
	test.equal(r.numberProperty.leave2, 0);
	test.equal(r.stringProperty, 56.77*2);

	test.done();

}

exports.testMissingLeave = function(test) {
	var tree1 = { "objectProperty" :
	{ "someValue" : 2, "someotherValue" : { "leave1":0, "leave2": 0} },
		"arrayProperty" : [1,2,3,4],
		"numberProperty" : { "leave1":0, "leave2": 0},
		"stringProperty" : "56.77" };

	var tree2 = { "objectProperty" :
	{ "someValue" : 2 },
		"arrayProperty" : [1,2,3,4],
		"numberProperty" : { "leave1":0 },
		"stringProperty" : "56.77" };

	var r = TreeOperator.add(tree1,tree2);

	test.equal(r.objectProperty.someValue,4);
	test.equal(r.objectProperty.hasOwnProperty("someotherValue"), false);
	test.equal(r.arrayProperty[0],2);
	test.equal(r.arrayProperty[3],8);
	test.equal(r.numberProperty.leave1, 0);
	test.equal(r.numberProperty.hasOwnProperty("leave2"), false);
	test.equal(r.stringProperty, 56.77*2);

	test.done();
}

exports.testMetricOperations = function(test) {
	var metrics = require("./sampleMetrics.js");

	var r = TreeOperator.add(metrics, metrics);

	test.equal(r.nodes.MasterNode.iostat.load["5m"], 1.29*2);
	test.equal(r.nodes.SlaveNode.ps.pid, 4096*2);
	test.equal(r.nodes.MasterNode.ps.hasOwnProperty("command"), false);
	test.equal(r.nodes.SlaveNode.ps.hasOwnProperty("user"), false);

	test.done();
}

exports.testScalarMultiply = function(test) {
	var metrics = require("./sampleMetrics");

	var r = TreeOperator.muls(metrics, 5);

	test.equal(r.nodes.MasterNode.iostat.load["5m"], 1.29*5);
	test.equal(r.nodes.SlaveNode.ps.pid, 4096*5);
	test.equal(r.nodes.MasterNode.ps.command, "./executable --param");
	test.equal(r.applicationId, "Some Batch App");
	test.equal(r.startTime, 1399976840925*5);

	test.done();
}

exports.testAverage = function(test) {
	var t = { "a1" : { "b1" : 1, "b2" : { "c1" : 1 , "c2" : 2 }},
			  "a2" : { "b3" : 3, "b4" : { "c3" : 3 , "c4" : 4 }}};
	var a = [];
	for(var i = 0 ; i < 15 ; i++) {
		a.push(JSON.parse(JSON.stringify(t)));
	}

	var res = TreeOperator.sum(a);
	test.equal(res[1], 15);
	test.equal(res[0].a1.b1,15);
	test.equal(res[0].a2.b4.c4,60);

	res = TreeOperator.divs(res[0],15);

	test.equal(res.a1.b1,1);
	test.equal(res.a2.b4.c4,4);

	test.done();
}




