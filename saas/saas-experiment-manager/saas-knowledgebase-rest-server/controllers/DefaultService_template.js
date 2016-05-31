'use strict';

exports.experimentGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expid (String)
   **/

var examples = {};
  
  examples['application/json'] = {
  "appId" : "aeiou",
  "name" : "aeiou",
  "_id" : "aeiou",
  "kpis" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "metrics" : [ {
      "name" : "aeiou",
      "description" : "aeiou",
      "_id" : "aeiou"
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
exports.experimentsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   **/

var examples = {};
  
  examples['application/json'] = [ {
  "appId" : "aeiou",
  "name" : "aeiou",
  "_id" : "aeiou",
  "kpis" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "metrics" : [ {
      "name" : "aeiou",
      "description" : "aeiou",
      "_id" : "aeiou"
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
  "_id" : "aeiou",
  "kpis" : [ {
    "name" : "aeiou",
    "description" : "aeiou",
    "_id" : "aeiou",
    "metrics" : [ {
      "name" : "aeiou",
      "description" : "aeiou",
      "_id" : "aeiou"
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
exports.snapshotGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * snapid (String)
   **/

var examples = {};
  
  examples['application/json'] = {
  "deplId" : "aeiou",
  "date" : "2016-05-25T13:22:25.628+0000",
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
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
exports.snapshotsGet = function(args, res, next) {
  /**
   * parameters expected in the args:
   * expId (String)
   **/

var examples = {};
  
  examples['application/json'] = [ {
  "deplId" : "aeiou",
  "date" : "2016-05-25T13:22:25.632+0000",
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
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
  "date" : "2016-05-25T13:22:25.634+0000",
  "measures" : [ {
    "refersTo" : [ {
      "reference" : "aeiou",
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
