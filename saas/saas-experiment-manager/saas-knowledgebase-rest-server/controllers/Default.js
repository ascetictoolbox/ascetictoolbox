'use strict';

var url = require('url');


var Default = require('./DefaultService');


module.exports.experimentsGet = function experimentsGet (req, res, next) {
  Default.experimentsGet(req.swagger.params, res, next);
};

module.exports.experimentsPost = function experimentsPost (req, res, next) {
  Default.experimentsPost(req.swagger.params, res, next);
};

module.exports.snapshotsGet = function snapshotsGet (req, res, next) {
  Default.snapshotsGet(req.swagger.params, res, next);
};

module.exports.snapshotsPost = function snapshotsPost (req, res, next) {
  Default.snapshotsPost(req.swagger.params, res, next);
};
