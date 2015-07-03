// Load the Visualization API
google.load('visualization', '1', {packages: ['corechart']});
google.load('visualization', '1', {packages: ['gauge']});

/* -----------------------------------------------------------------------------------------------
 * Total Load Chart
 */
function drawTotalLoadChart (divUuid, infoSTR) {
	// Create data table
  	var data = new google.visualization.DataTable();
  	data.addColumn('number', 'Time');
  	data.addColumn('number', 'Workload');
  	data.addColumn('number', 'Resources');
  	
  	// Insert available Information
  	var infoMatrix = infoSTR.split(" ");
	for (var i=0; i<infoMatrix.length; i++) {
		//Each element has: time, totalLoad
  		var elem = infoMatrix[i].split(":");
  		data.addRow([parseInt(elem[0]), parseFloat(elem[1]), parseInt(elem[2])]);
  	}
  	
  	// Create graph options
  	var options = {
  		chartArea: {
  			width: '80%',
  			height: '80%'
  		},
        hAxis: {
          	title: 'Time (s)',
          	minValue: 0
        },
        vAxes: {
        	0: {logScale: false,
        		title: 'Workload (s)',
        		minValue: 0
        	},
        	1: {logScale: false,
        		title: 'Number of Resources',
        		minValue: 0
        	},
		},
	    series: {
	       0:{targetAxisIndex:0},
	       1:{targetAxisIndex:1}
	  	}, 
	  	legend: {
	  		position: 'top'
	  	}
    };

    // Instantiate and draw chart
    var chart = new google.visualization.AreaChart(document.getElementById(divUuid));
    chart.draw(data, options);
}

/* -----------------------------------------------------------------------------------------------
 * Load Per Core Chart
 */
function drawLoadPerCoreChart(divUuid, numCoresSTR, infoSTR) {
	// Create data table
  	var data = new google.visualization.DataTable();
  	data.addColumn('number', 'Time');
  	var numCores = parseInt(numCoresSTR);
  	for (var i = 0; i < numCores; i++) {
  		data.addColumn('number', 'Core ' + i.toString());
  	}
  	data.addColumn('number', 'Resources');
  	
  	// Insert available Information
  	var infoMatrix = infoSTR.split(" ");
	for (var i=0; i<infoMatrix.length; i++) {
		//Each element has: time:core1Load:...:coreNLoad:totalResources
  		var infoArray = infoMatrix[i].split(":");
  		var newRow = [];
  		newRow.push(parseInt(infoArray[0]))
  		for (var j=1; j<infoArray.length-1; j++) {//totalResources skipped
  			newRow.push(parseFloat(infoArray[j]));
  		}
  		for (var j=infoArray.length-1; j<numCores+1; j++) {
  			newRow.push(0);
  		}
  		newRow.push(parseInt(infoArray[infoArray.length-1]))
  		data.addRow(newRow);
  	}
  	
  	// Create graph options
  	var options = {
  		chartArea: {
  			width: '80%',
  			height: '80%'
  		},
        hAxis: {
          	title: 'Time (s)',
          	minValue: 0
        },
        vAxes: {
        	0: {logScale: false,
        		title: 'Workload (s)',
        		minValue: 0
        	},
        	1: {logScale: false,
        		title: 'Number of Resources',
        		minValue: 0
        	},
		},
	    series: "",
        isStacked: true, 
	  	legend: {
	  		position: 'top'
	  	}
    };
    parametrizedSeries = {};
	parametrizedSeries[numCores] = {targetAxisIndex:1};
	options.series = parametrizedSeries;
    
    // Instantiate and draw chart
    var chart = new google.visualization.AreaChart(document.getElementById(divUuid));
    chart.draw(data, options);
}

/* -----------------------------------------------------------------------------------------------
 * Total Cores Chart - for running and pending charts
 */
function drawTotalCores(divUuid, infoSTR) {	
  	// Create data table
  	var data = new google.visualization.DataTable();
  	data.addColumn('number', 'Time');
  	data.addColumn('number', 'CoreElements');
  	data.addColumn('number', 'Resources');
  	
  	// Insert available Information
  	var infoMatrix = infoSTR.split(" ");
	for (var i=0; i<infoMatrix.length; i++) {
		//Each element has: time, numCores, numResources
  		var elem = infoMatrix[i].split(":");
  		data.addRow([parseInt(elem[0]), parseInt(elem[1]), parseInt(elem[2])]);
  	}
  	
  	// Create graph options
  	var options = {
  		chartArea: {
  			width: '80%',
  			height: '80%'
  		},
        hAxis: {
          	title: 'Time (s)',
          	minValue: 0
        },
        vAxes: {
        	0: {logScale: false,
        		title: 'Number of CoreElements',
        		minValue: 0
        	},
        	1: {logScale: false,
        		title: 'Number of Resources',
        		minValue: 0
        	},
		},
	    series:{
	       0:{targetAxisIndex:0},
	       1:{targetAxisIndex:1}
	  	}, 
	  	legend: {
	  		position: 'top'
	  	}
    };

    // Instantiate and draw chart
    var chart = new google.visualization.AreaChart(document.getElementById(divUuid));
    chart.draw(data, options);
}

/* -----------------------------------------------------------------------------------------------
 * Cores per core Chart - for running and pending charts
 */
function drawCoresPerCoreChart(divUuid, numCoresSTR, infoSTR) {
	// Create data table
  	var data = new google.visualization.DataTable();
  	data.addColumn('number', 'Time');
  	var numCores = parseInt(numCoresSTR);
  	for (var i = 0; i < numCores; i++) {
  		data.addColumn('number', 'CoreElement ' + i.toString());
  	}
  	data.addColumn('number', 'Resources');
  	
  	// Insert available Information
  	var infoMatrix = infoSTR.split(" ");
	for (var i=0; i<infoMatrix.length; i++) {
		//Each element has: time:Core0:...:CoreN:totalResources
  		var infoArray = infoMatrix[i].split(":");
  		var newRow = [];
  		newRow.push(parseInt(infoArray[0]))
  		for (var j=1; j<infoArray.length-1; j++) {
  			newRow.push(parseInt(infoArray[j]));
  		}
  		for (var j=infoArray.length-1; j<numCores+1; j++) {
  			newRow.push(0);
  		}
  		newRow.push(parseInt(infoArray[infoArray.length-1]))
  		data.addRow(newRow);
  	}
  	
  	// Create graph options
  	var options = {
  		chartArea: {
  			width: '80%',
  			height: '80%'
  		},
        hAxis: {
          	title: 'Time (s)',
          	minValue: 0
        },
        vAxes: {
        	0: {logScale: false,
        		title: 'Number of CoreElements',
        		minValue: 0
        	},
        	1: {logScale: false,
        		title: 'Number of Resources',
        		minValue: 0
        	},
		},
	    series: "",
        isStacked: true, 
	  	legend: {
	  		position: 'top'
	  	}
    };
    parametrizedSeries = {};
	parametrizedSeries[numCores] = {targetAxisIndex:1};
	options.series = parametrizedSeries;
    
    // Instantiate and draw chart
    var chart = new google.visualization.AreaChart(document.getElementById(divUuid));
    chart.draw(data, options);
}

/* -----------------------------------------------------------------------------------------------
 * Total Resources Status Chart - Memory and CPU
 */
function drawTotalResourcesStatusChart(divUuid, infoSTR) {
  	// Create data table
  	var data = new google.visualization.DataTable();
  	data.addColumn('string', 'Label');
  	data.addColumn('number', 'Value');
  	
  	// Insert available Information
  	var infoValues = infoSTR.split(":");
  	data.addRow(['Total CPU', parseInt(infoValues[1])])
  	data.addRow(['Total Memory', parseInt(infoValues[2])])
  	
  	
  	// Create graph options
  	var options = {
  		width: 300,
  		height: 300,
        redFrom: 90, redTo: 100,
        yellowFrom: 75, yellowTo: 90,
        minorTicks: 5
    };

    // Instantiate and draw chart
    var chart = new google.visualization.Gauge(document.getElementById(divUuid));
    chart.draw(data, options);
}

/* -----------------------------------------------------------------------------------------------
 * Empty Chart
 */
function drawEmpty(divUuid) {
  	// Create data table
  	var data = new google.visualization.DataTable();
  	
  	// Create graph options
  	var options = {
  		chartArea: {
  			width: '80%',
  			height: '80%'
  		},
	  	legend: {
	  		position: 'top'
	  	}
    };

    // Instantiate and draw chart
    var chart = new google.visualization.AreaChart(document.getElementById(divUuid));
    chart.draw(data, options);
}