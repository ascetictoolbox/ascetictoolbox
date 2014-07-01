//var express = require('express');

module.exports = {
    port : 3000,

    database : {
        name : "ApplicationMonitor",
        host : "localhost",
	    port : 27017
    },


	appManager : {
		url : "http://www.example.com"
	},

	paasEnergyModeler : {
		url : "http://222.example.com",
		notifFreqSec : 5   // Notification frequency (in seconds)
	}

};
