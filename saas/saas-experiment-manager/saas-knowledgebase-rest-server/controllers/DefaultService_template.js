'use strict';

exports.experimentsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   **/

var examples = {};

  examples['application/json'] = [ {
  "appId" : "aeiou",
  "name" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou",
  "kpis" : [ {

    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "metrics" : [ {
      "name" : "aeiou",
      "description" : "aeiou",
      "_id" : "aeiou",
      "type" : "aeiou"
    } ]
  } ],
  "events" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou"
  } ]
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.experimentsPost = function(args, res, next) {
  /**
   * parameters expected in the args:
   * body (Experiment)
   **/

var examples = {};

  examples['application/json'] = {
  "appId" : "aeiou",
  "name" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou",
  "kpis" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "metrics" : [ {
      "name" : "aeiou",
      "description" : "aeiou",
      "_id" : "aeiou",
      "type" : "aeiou"
    } ]
  } ],
  "events" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou"
  } ]
};



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.experimentGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/

var examples = {};

  examples['application/json'] = {
  "appId" : "aeiou",
  "name" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou",
  "kpis" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "metrics" : [ {
      "name" : "aeiou",
      "description" : "aeiou",
      "_id" : "aeiou",
      "type" : "aeiou"
    } ]
  } ],
  "events" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou"
  } ]
};



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.eventGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "name" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou"
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.experimentKPISGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "name" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou",
  "metrics" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "type" : "aeiou"
  } ]
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.experimentSnapshotGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "deplId" : "aeiou",
  "date" : "2016-06-29T11:08:58.965+0000",
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
      "name" : "aeiou",
      "category" : "aeiou"
    } ],
    "metric" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "value" : "aeiou"
  } ],
  "name" : "aeiou",
  "deplName" : "aeiou",
  "description" : "aeiou",
  "experimentId" : "aeiou",
  "_id" : "aeiou",
  "vms" : [ {
    "vmId" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "events" : [ "aeiou" ]
  } ]
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.snapshotsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expId (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "deplId" : "aeiou",
  "date" : "2016-06-29T11:08:58.970+0000",
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
      "name" : "aeiou",
      "category" : "aeiou"
    } ],
    "metric" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "value" : "aeiou"
  } ],
  "name" : "aeiou",
  "deplName" : "aeiou",
  "description" : "aeiou",
  "experimentId" : "aeiou",
  "_id" : "aeiou",
  "vms" : [ {
    "vmId" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "events" : [ "aeiou" ]
  } ]
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.snapshotsPost = function(args, res, next) {
  /**
   * parameters expected in the args:
   * body (Snapshot)
   **/

var examples = {};

  examples['application/json'] = {
  "deplId" : "aeiou",
  "date" : "2016-06-29T11:08:58.974+0000",
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
      "name" : "aeiou",
      "category" : "aeiou"
    } ],
    "metric" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "value" : "aeiou"
  } ],
  "name" : "aeiou",
  "deplName" : "aeiou",
  "description" : "aeiou",
  "experimentId" : "aeiou",
  "_id" : "aeiou",
  "vms" : [ {
    "vmId" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "events" : [ "aeiou" ]
  } ]
};



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.snapshotGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

var examples = {};

  examples['application/json'] = {
  "deplId" : "aeiou",
  "date" : "2016-06-29T11:08:58.975+0000",
  "name" : "aeiou",
  "deplName" : "aeiou",
  "description" : "aeiou",
  "experimentId" : "aeiou",
  "_id" : "aeiou"
};



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.snapshotMeasureByEventGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
      "name" : "aeiou",
      "category" : "aeiou"
    } ],
    "metric" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "value" : "aeiou"
  } ],
  "name" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou"
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.snapshotMeasuresGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "refersTo" : [ {
    "reference" : "aeiou",
    "name" : "aeiou",
    "category" : "aeiou"
  } ],
  "metric" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou",
  "value" : "aeiou"
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
exports.snapshotVMsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

var examples = {};

  examples['application/json'] = [ {
  "vmId" : "aeiou",
  "description" : "aeiou",
  "_id" : "aeiou",
  "events" : [ "aeiou" ]
} ];



  if(Object.keys(examples).length > 0) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(examples[Object.keys(examples)[0]] || {}, null, 2));
  }
  else {
    res.end();
  }


}
