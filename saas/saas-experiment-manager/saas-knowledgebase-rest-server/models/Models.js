var swaggerMongoose = require('swagger-mongoose');
var fs = require('fs');
var jsyaml = require('js-yaml');
var util = require('util');
require('mongoose').set('debug', true);

var swagger = fs.readFileSync('./api/swagger.yaml', 'utf8');
var swaggerDoc = jsyaml.safeLoad(swagger);
var persistence = swaggerMongoose.compile(swaggerDoc).models;

var Experiment = persistence.Experiment;
var Snapshot = persistence.Snapshot;

module.exports.Experiment = persistence.Experiment;
module.exports.Snapshot = persistence.Snapshot;



/*Experiment.find(function(err,data){
  console.log("Session: %j", data);
});*/
