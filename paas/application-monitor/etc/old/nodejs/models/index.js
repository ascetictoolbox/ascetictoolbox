// models/index.js
if (!global.hasOwnProperty('db')) {
    console.log("initializing database...");

    var mongoose = require('mongoose');

    var config = require("../config");

    mongoose.connect('mongodb://' + config.database.host + ':' + config.database.port + '/' + config.database.name.toLowerCase());
    global.db = {
        mongoose : mongoose,
        Metrics : require('./Metrics')(mongoose)
    };
}

module.exports = global.db;