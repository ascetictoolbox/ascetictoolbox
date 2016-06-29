'use strict';
var util = require('util');
var mongoose = require('mongoose'),
	colors = require('colors'),
	config = require('../config'),
	db = mongoose.connection;

db.on('error', function() {
	console.log('Database connection error'.red);
});
db.on('connecting', function () {
	console.log('Database connecting'.cyan);
});
db.once('open', function() {
	console.log('Database connection established'.green);
});
db.on('reconnected', function () {
	console.log('Database reconnected'.green);
});

mongoose.connect(config.db_url, {server: {auto_reconnect: true}});

var Models = require('../models/Models.js');

exports.experimentsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
  **/

  Models.Experiment.find(function(err,data){
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(data));
  });
}
exports.experimentsPost = function(args, res, next) {
  /**
   * parameters expected in the args:
  * body (Experiment)
  **/
	console.log(util.inspect(args.body.value,false,null));
  var exp = new Models.Experiment(args.body.value);

  exp.save(function (err) {
      if (err) throw err;
      Models.Experiment.findOne({_id: exp._id}, function (err, data) {
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify(data));
      });
    });
}
exports.experimentGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/

	 Models.Experiment.find({_id: args.expid.value},function(err,data){
	 	res.setHeader('Content-Type', 'application/json');
	 	res.end(JSON.stringify(data));
	 });
}
exports.eventGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/
   Models.Experiment.find({_id: args.expid.value},function(err,data){
	 	res.setHeader('Content-Type', 'application/json');
	 	res.end(JSON.stringify(data[0].events));
	 });
}
exports.experimentSnapshotGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/
   console.log(args.expId)

   Models.Snapshot.find({experimentId: args.expid.value},function(err,data){
     res.setHeader('Content-Type', 'application/json');
     res.end(JSON.stringify(data));
   });
}
exports.snapshotsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expId (String)
  **/
  console.log(args.expId)
  if(args.expId.value==null){
    Models.Snapshot.find(function(err,data){
      res.setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify(data));
    });
  }
  else{
    Models.Snapshot.find({experimentId: args.expId.value},function(err,data){
      res.setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify(data));
    });
  }
}
exports.snapshotsPost = function(args, res, next) {
  console.log(util.inspect(args.body.value,false,null));
  var exp = new Models.Snapshot(args.body.value);
  exp.save(function (err) {
      if (err) throw err;
      Models.Snapshot.findOne({_id: exp._id}, function (err, data) {
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify(data));
      });
    });
}
exports.snapshotGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

	 Models.Snapshot.find({_id: args.snapid.value},function(err,data){
		 res.setHeader('Content-Type', 'application/json');
		 res.end(JSON.stringify(data));
	 });
}
exports.snapshotMeasuresGet = function(args, res, next) {
   /**
    * parameters expected in the args:
    * snapid (String)
    **/

 	 Models.Snapshot.find({_id: args.snapid.value},function(err,data){
 		 res.setHeader('Content-Type', 'application/json');
 		 res.end(JSON.stringify(data[0].measures));
 	 });
}
exports.snapshotVMsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/
   Models.Snapshot.find({_id: args.snapid.value},function(err,data){
     res.setHeader('Content-Type', 'application/json');
     res.end(JSON.stringify(data[0].vms));
   });
}

exports.experimentKPISGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/
 	Models.Experiment.find({_id: args.expid.value},function(err,data){
 	 res.setHeader('Content-Type', 'application/json');
 	 res.end(JSON.stringify(data[0].kpis));
 	});
}


exports.snapshotMeasureByEventGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

	Models.Snapshot.find({_id: args.snapid.value},function(err,snapshots){
		var snapshot = snapshots[0];
		console.log("getting experiment with id : " + snapshot.expId)
		Models.Experiment.find({_id: snapshot.experimentId},function(err,experiments){
			var results = experiments[0].events.map(function(event){
				var newEvent = Object()
				newEvent._id = event._id
				newEvent.name= event.name
				newEvent.description = event.description
				newEvent.measures = snapshot.measures.filter(function(m){
					return m.refersTo.length == 1 && m.refersTo[0].name == event.name
				});
				return newEvent
			});

	 		res.setHeader('Content-Type', 'application/json');
	 		res.end(JSON.stringify(results));
	 	});
	});
	/*
	what to return a cross join between event of the experiment and measure
	*/
}
