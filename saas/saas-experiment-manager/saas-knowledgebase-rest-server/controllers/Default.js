'use strict';

var url = require('url');


var Default = require('./DefaultService');


module.exports.experimentsGet = function experimentsGet (req, res, next) {
  Default.experimentsGet(req.swagger.params, res, next);
};

module.exports.experimentsPost = function experimentsPost (req, res, next) {
  Default.experimentsPost(req.swagger.params, res, next);
};

module.exports.experimentGet = function experimentGet (req, res, next) {
  Default.experimentGet(req.swagger.params, res, next);
};

module.exports.eventGet = function eventGet (req, res, next) {
  Default.eventGet(req.swagger.params, res, next);
};

module.exports.experimentKPISGet = function experimentKPISGet (req, res, next) {
  Default.experimentKPISGet(req.swagger.params, res, next);
};

module.exports.experimentSnapshotGet = function experimentSnapshotGet (req, res, next) {
  Default.experimentSnapshotGet(req.swagger.params, res, next);
};

module.exports.snapshotsGet = function snapshotsGet (req, res, next) {
  Default.snapshotsGet(req.swagger.params, res, next);
};

module.exports.snapshotsPost = function snapshotsPost (req, res, next) {
  Default.snapshotsPost(req.swagger.params, res, next);
};

module.exports.snapshotGet = function snapshotGet (req, res, next) {
  Default.snapshotGet(req.swagger.params, res, next);
};

module.exports.snapshotMeasureByEventGet = function snapshotMeasureByEventGet (req, res, next) {
  Default.snapshotMeasureByEventGet(req.swagger.params, res, next);
};

module.exports.snapshotMeasuresGet = function snapshotMeasuresGet (req, res, next) {
  Default.snapshotMeasuresGet(req.swagger.params, res, next);
};

module.exports.snapshotVMsGet = function snapshotVMsGet (req, res, next) {
  Default.snapshotVMsGet(req.swagger.params, res, next);
};
