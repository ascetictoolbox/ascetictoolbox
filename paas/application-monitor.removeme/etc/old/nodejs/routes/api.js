var express = require('express');
var router = express.Router();
var config = require("../config");
var TreeOperator = require("../utils/TreeOperator");

// TODO: USE NEXT FUNCTION TO PROCESS ERRORS: http://stackoverflow.com/questions/13133071/express-next-function-what-is-it-really-for

/*
 * Gets all the registered apps and their nodes
 */
router.get('/', function(req, res) {
	var db = global.db;
	db.Metrics.findRegisteredApps(req.query.start,req.query.end,function(err, apps) {
		res.json(200,apps);
	});
});


//curl -X POST -H "Content-Type: application/json" -d '{"somethingHere":3,"somethingThere":"hi"}' http://localhost:3000/api/cosa/algoaqui

router.post("/:ApplicationId/:NodeId", function(req, res, next) {
    req.accepts("application/json");

	console.log(req.body)

    var db = global.db;
    var metrics = new db.Metrics({
        applicationId : req.params.ApplicationId,
        nodeId : req.params.NodeId,
        timestamp : Date.now(),
        values : req.body
    });

    metrics.save(function(error, user) {

        if (error) {
			console.log("error!! "+error);
			console.log(new Error().stack);
			res.send(500, error);
		}

        res.send(200);

    });

});

router.get("/:ApplicationId/:NodeId", function(req, res, next) {
	var callbackRange = function(err, array) {
		if(array == null || array.length == 0) {
			res.send(200);
		}
		var summ = TreeOperator.sum(JSON.parse(JSON.stringify(array)));
		res.send({
			applicationId : req.params.ApplicationId,
			nodeId : req.params.NodeId,
			startTime : req.query.start ? req.query.start : 0,
			endTime : req.query.end ? req.query.end : Date.now(),
			values : TreeOperator.divs(summ[0].values,summ[1])
		});
	}
	var callbackLatest = function(err, array) {
		console.log("f - " + array);
		if(array[0] == null) res.send(404);
		else res.send({
			applicationId : req.params.ApplicationId,
			nodeId : req.params.NodeId,
			startTime : array[0].timestamp,
			endTime : array[0].timestamp,
			values : array[0].values
		});
	}

	if(!req.query.start && !req.query.end) {
		global.db.Metrics.findLastMetric(req.params.ApplicationId, req.params.NodeId, callbackLatest)
	} else {
		global.db.Metrics.findAllMetrics(req.query.start,req.query.end,req.params.ApplicationId,req.params.NodeId,callbackRange);
	}
});



module.exports = router;
