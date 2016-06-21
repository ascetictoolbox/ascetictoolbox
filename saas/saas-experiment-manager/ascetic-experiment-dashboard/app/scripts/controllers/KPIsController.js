'use strict'



/*
KPIs Controller

Controls data behind the KPI's (Tradeoffs) page
 */
angular.module('asceticApp')
  .controller('KPIsCtrl',
		function ($q,$scope, $http, $routeParams,AppManagerHost,AppMonitorHost,PaasAPI,SkbAPI) {

			// Load existing dashboard or create new ?
      SkbAPI.snapshots("Experiment 1", function(data){
        console.log(data);
      });

			// Identify KPIS
			// Energy, Cost, Time, ...
			var kpiDef,
			kpiDomains = [],
			vmsCache = {},
			eventsCache = {};
			$scope.scatterX = 0; // Id of the KPI on the X axis of the scatterplot
			$scope.scatterY = 1; // Id of the KPI on the Y axis of the scatterplot

      $scope.currentAppId = $scope.currentDeplIds = null;
			PaasAPI.applications.query(function(data) {
          console.log(data);
		      $scope.appIds = data.collection.items.application.map(o => o.name);
          $scope.currentAppId = 'newsAsset';
          $scope.refreshDeploymentList();
		    });

			$scope.refreshDeploymentList = function () {
        console.log('get deployment appId: ' + $scope.currentAppId);
				PaasAPI.deployments.get({appId:$scope.currentAppId}, function(data){
		      $scope.deplIds = data.collection.items.deployment.map(o => o.id);
		    });
			}

			$scope.kpis = [];
			d3.json("data/kpis.json", function (error, data) {
				if (!error) {
					console.log(data);
					d3.json("data/metrics.json", function (error, metrics) {
						if (!error) {
							data.forEach(function (kpi, i) {
								$scope.kpis[i] = {
									name : kpi.name,
									value : i
								};
							});

						} else {
							console.log(error);
						}
					});
				} else {
					console.log(error);
				}

			});

			$scope.selectDepl = function () {
				for (let d of $scope.currentDeplIds) {
					if (!vmsCache.hasOwnProperty(d)) {
						/*$http.get(AppManagerHost+ "/applications/" + $scope.currentAppId + "/deployments/" + d + "/vms/").success(function (data) {
							// At the time of writing this code, this call does only return XML, unlike the others.
							// Let's just handle this on our side and parse it ourselves.
							var vmIds = $($.parseXML(data)).find("vm > id");
							vmsCache[d] = $.grep(vmIds, e => (e.localName == "id")).map(e => parseInt(e.innerHTML));
						});*/

						 vmsCache[490] = [1764,1765,1766,1768];
					}
				}
				$("#selectbar").attr("style", "display: none;");
				$("#bottom").attr("style", "display: block; position:absolute; top:570px; left: 800px;");
				loadAllEventsData().then(function () {
					loadKpis();
				});
			};

			function getMinMaxData() {

				var minEnergy = 10000,
				maxEnergy = 0,
				minCost = 10000,
				maxCost = 0,
				minDuration = 100000,
				maxDuration = 0;
				for (let e of allEventsData) {
					if (e.cost < minCost)
						minCost = e.cost;
					if (e.cost > maxCost)
						maxCost = e.cost;
					if (e.energy < minEnergy)
						minEnergy = e.energy;
					if (e.energy > maxEnergy)
						maxEnergy = e.energy;
					if (e.duration < minDuration)
						minDuration = e.duration;
					if (e.duration > maxDuration)
						maxDuration = e.duration;
				}

				return {
					cost : {
						min : minCost,
						max : maxCost
					},
					energy : {
						min : minEnergy,
						max : maxEnergy
					},
					duration : {
						min : minDuration,
						max : maxDuration
					}
				};
			}

			function getEnergyConsumption(url) {
				console.log(url);
				var deferred = Q.defer();
				var allEnergies = allTheXmlData.find("measurements > energy-measurement > value");
				var theEnergy = $.grep(allEnergies, e => e.parentNode['attributes'][1].textContent == url);
				console.log(theEnergy);
				var energy = parseFloat(theEnergy[0].innerHTML);
				deferred.resolve(energy);
				/*
				$http.get(url)
				.success(function (energy) {
					//document.write(energy);
                    console.log("energy = "+energy);
					var whs = $($.parseXML(energy)).find("energy-measurement > value");
					var wh = parseFloat(whs[0].innerHTML);
					console.log("resolved energy = "+wh);
                    deferred.resolve(wh);
				})
				.error(function () {
					deferred.reject("Get Energy failed for " + url);
				});
        */
				return deferred.promise;

			}

			function getCostEstimation(url) {
				console.log(url);
				var deferred = Q.defer();
				var allcosts = allTheXmlData.find("measurements > cost > charges");
				var theCosts = $.grep(allcosts, e => e.parentNode['attributes'][1].textContent == url);
				console.log(theCosts);
				var charges = parseFloat(theCosts[0].innerHTML);
				deferred.resolve(charges);

        /*
				$http.get(url)
				.success(function (cost) {
					//document.write(cost);
                    console.log("cost = "+cost);
					var costs = $($.parseXML(cost)).find("cost > charges");
					var charges = parseFloat(costs[0].innerHTML);
                    console.log("resolved cost = "+charges);
					deferred.resolve(charges);
				})
				.error(function () {
					deferred.reject("Get cost failed for " + url);
				});
        */
				return deferred.promise;
			}

			var allTheXmlData;
			function loadAllDataXml() {
				var deferred = Q.defer();
				$http.get("http://localhost:9000/data/allthedata.xml").success(function (data) {
					allTheXmlData = $($.parseXML(data));
					deferred.resolve(allTheXmlData);
				});
				return deferred.promise;
			}
			$scope.eventsLoaded = 0;
			$scope.totalEvents = 1;
			var allEventsData = [];

			function loadAllEventsData() {
				return loadAllDataXml().then(function (xmldata) {
					$scope.queryCount = $scope.currentDeplIds.length;
					$scope.queriesDone = 0;
					var allDeploymentsPromises = [];
					var totalEventEnergy = {},
					totalEventCost = {};
					for (let deplId of $scope.currentDeplIds) {
                        console.log('load deployement : '+deplId);
						var deferred = Q.defer();
						var query =
							"FROM events MATCH appId = \"" + $scope.currentAppId + "\" AND deploymentId = \"" + deplId + "\", GROUP BY data.eventType , avg(data.duration) as dur";
						console.log(query);
						$http.post(AppMonitorHost+"/query", query, {
							"headers" : {
								"Content-Type" : "text/plain",
								"Accept" : "application/json"
							}
						})
						.success(function (events) {

							/*    $scope.queryCount += (events.length * 4 * 2) ;
							$scope.queriesDone++; */
							var allEventsPromises = [];
							for (let event of events) {
								console.log('load event '+event['_id'])

								totalEventEnergy[event['_id']] = 0;
								totalEventCost[event['_id']] = 0;
								var allVmsPromises = [];

								for (let vm of vmsCache[490]) {
                                    console.log('load vm : '+vm);
									//var uribase = AppManagerHost +"/applications/" + $scope.currentAppId + "/deployments/" + deplId + "/vms/" + vm + "/events/" + event['_id'];
									//var uribase = "http://localhost:8000/dashboard"
									var uribase = "/applications/"+$scope.currentAppId+"/deployments/"+deplId+"/vms/"+vm+"/events/"+event['_id'] ;
									allVmsPromises.push(getEnergyConsumption(uribase + "/energy-consumption").then(function (e) {
											totalEventEnergy[event['_id']] += e;

										}));
									allVmsPromises.push(getCostEstimation(uribase + "/cost-estimation").then(function (c) {
											totalEventCost[event['_id']] += c;

										}));
								}
								allEventsPromises.push(Q.all(allVmsPromises).then(function (result) {
										//console.log("all vms promises ok" + totalEventEnergy[event['_id']]);
										var evt = {
											workload : event['_id'],
											date : "01012001",
											deployment : deplId,
											duration : event['dur'],
											energy : totalEventEnergy[event['_id']],
											cost : totalEventCost[event['_id']]
										}
										allEventsData.push(evt);
									}));

							}
							Q.all(allEventsPromises).then(function (result) {
								deferred.resolve(result);
							});
						}).error(function (err) {
							console.log("error " + JSON.stringify(err));
							deferred.reject(err);
						});

						allDeploymentsPromises.push(deferred.promise);
					}

					return Q.all(allDeploymentsPromises);
				});

			}

			/* Load the KPIs definition from the kpis.json file */
			function loadKpis() {
				var minMax = getMinMaxData();
				d3.json("data/kpis.json", function (error, data) {
					if (!error) {
						kpiDef = data;
						d3.json("data/metrics.json", function (error, metrics) {
							if (!error) {
								kpiDef.forEach(function (kpi, i) {
									var metric = $.grep(metrics, e => (e.id == kpi.metric))[0];
									// console.log("kpiDef["+i+"] = "+metric.attribute);
									kpiDef[i].attribute = metric.attribute;
									kpiDomains[i] = minMax[metric.attribute].max;
									$scope.kpis[i] = {
										name : kpi.name,
										value : i
									};
								});
								goAhead(allEventsData);
							} else {
								console.log(error);

							}
						});
					} else {
						console.log(error);

					}
				});

			}

			// Identify metrics
			// (Total kWh, Max kWh), (total cost €, workload cost estimation), (average response time, max response time)


			// Identify data source
			// Application monitor (parameters ?)
			// Application manager (parameters ?)
			// Our own mongodb ?
			// Time series passed as parameter ?

			// Connect
			// to the data sources

			// Fetch data

			// Compute kpi based on metrics
			// sum, average ...

			// Format kpis

			// Display
			// One barchart / kpi
			// How many scatter plots ?
			// Which kpi goes on which axis of the scatter plot ?


			// Best function name EVER !
			// This is run after the kpis have been loaded from the json file.
			function goAhead(data) {
				//console.log(data);

				// Various formatters.
				var formatNumber = d3.format(",d"),
				formatChange = d3.format("+,d"),
				formatDate = d3.time.format("%B %d, %Y"),
				formatTime = d3.time.format("%I:%M %p");

				// A nest operator, for grouping the flight list.
				var nestByDate = d3.nest()
					.key(function (d) {
						return d3.time.day(d.date);
					});

				var nestByWLDepl = d3.nest()
					.key(function (d) {
						return d.workload + "-" + d.deployment
					})
					.rollup(function (d) {
						var result = {};
						kpiDef.forEach(function (kpi, j) {
							if (kpi.aggfunc == "avg") {
								result[kpi.attribute] = d3.mean(d, function (g) {
										 return + g[kpi.attribute];
									});
							}
						});
						return result;

					})
					.entries(data);

				// A little coercion, since the data is untyped.
				nestByWLDepl.forEach(function (d, i) {
					d.kpis = [];
					kpiDef.forEach(function (kpi, j) {
						d.kpis[j] = +d.values[kpi.attribute];
					});
				});

				/*
				<div class="box">
				<div class="box-icon">
				<span class="fa fa-4x fa-html5"></span>
				</div>
				<div class="info">
				<h4 class="text-center">Title</h4>
				<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Corrupti atque, tenetur quam aspernatur corporis at explicabo nulla dolore necessitatibus doloremque exercitationem sequi dolorem architecto perferendis quas aperiam debitis dolor soluta!</p>
				<a href="" class="btn">Link</a>
				</div>
				</div>
				 */
				// Create a div for each barchart in the chart area
				var charts = d3.select("#charts"),
				chartdiv;
				var row = charts.append("div").attr("class", "row");
				var icons = ["clock-o", "plug", "euro"]
				kpiDef.forEach(function (kpi, j) {
					chartdiv = row.append("div").attr("class", "box col-md-4").attr("style", "width:380px; margin-left:40px; padding-right:30px;");
					chartdiv.append("div").attr("class", "box-icon").append("span").attr("class", "fa fa-4x fa-" + icons[j]).attr("onclick", "javascript:reset(" + j + ")");
					chartdiv.append("div").attr("id", "chart-" + j).attr("class", "chart");

					chartdiv.append("div").attr("class", "title").text(kpi.name + " (" + kpi.unit + ")");
					chartdiv.append("div").attr("class", "descr").text(kpi.descr);
				});

				// Add a special div for the scatterplot svg below the barcharts

				var rowDiv = charts.append("div").attr("class", "row");
				var boxDiv = rowDiv.append("div").attr("class", "box").attr("style", "height:600px; width:650px; margin-left:40px;padding-right:60px;");
				var energyChart2 = boxDiv.append("div").attr("id", "energychart2").attr("class", "chart");
				//energyChart2.append("div").attr("class","title").text(kpiDef[$scope.scatterX]+ " (x) / " + kpiDef[$scope.scatterY] + " (y)");;
				energyChart2.append("svg").attr("id", "svgchart").attr("style", "margin-top:20px");

				// Deployments coloring (in the scatterplot and in the list below).
				var deployments = d3.map(data, d => d.deployment).keys();
				var depColors = ["#03558B", "#3F9DDB", "#075A36", "#E7AB00", "#EE5B03", "#AC1014", "#FDD526", "#73C509", "#0980BA", "#65286B", ];
				var depColorMap = {};

				for (var i = 0; i < deployments.length; i++) {
					depColorMap[deployments[i]] = depColors[i];
				}

				// Create the crossfilter for the relevant dimensions and groups.
				var globalBrush = [];

				var measures = crossfilter(nestByWLDepl),
				all = measures.groupAll(),
				//date = measures.dimension(function(d) { return d.date; }),
				//dates = date.group(d3.time.day),
				kpis = [],
				kpiGroups = [],
				charts = [],
				scatterScales = [];

				for (var j = 0; j < kpiDef.length; j++) {
					kpis[j] = measures.dimension(function (d) {
							return Math.max(0, d.kpis[j]);
						});
					kpiGroups[j] = kpis[j].group(function (d) {
							return Math.floor(d / 10) * 10;
						});

					charts[j] = barChart()
						.dimension(kpis[j])
						.group(kpiGroups[j])
						.x(d3.scale.linear()
							.domain([0, kpiDomains[j] * 1.1])
							.range([0, 300]));
					/* .domain([0, kpiDomains[j]])
					.rangeRound([0, kpiDomains[j]])) ; */
					scatterScales[j] = d3.scale.linear()
						.domain([0, kpiDomains[j] * 1.1])
						.range([0, 450])
						console.log("scatterScales[" + j + "]=");
					console.log(scatterScales[j].domain());
				}

				charts[kpiDef.length] = scatterPlot();

				// Given our array of charts, which we assume are in the same order as the
				// .chart elements in the DOM, bind the charts to the DOM and render them.
				// We also listen to the chart's brush events to update the display.
				var chart = d3.selectAll(".chart")
					.data(charts)
					.each(function (chart) {
						chart.on("brush", renderAll).on("brushend", renderAll);
					});

				// Render the initial lists.
				// var list = factsList();
				//   .data([factsList]);


				updateList(nestByWLDepl)
				// Render the total.
				d3.selectAll("#total")
				.text(formatNumber(measures.size()));

				renderAll();

				// Renders the specified chart or list.
				function render(method) {
					d3.select(this).call(method);
				}

				// Whenever the brush moves, re-rendering everything.
				function renderAll() {
					chart.each(render);

					d3.select("#active").text(formatNumber(all.value()));
				}

				// Define the div for the tooltip
				var tooltipDiv = d3.select("body").append("div")
					.attr("class", "tooltip")
					.style("opacity", 0);

				window.filter = function (filters) {
					filters.forEach(function (d, i) {
						charts[i].filter(d);
					});
					renderAll();
				};

				window.reset = function (i) {
					charts[i].filter(null);

					renderAll();

					var svg = d3.select("g[id='circlesArea']");
					svg.selectAll("circle").remove();
					var circles = svg.selectAll("circle").data(nestByWLDepl);

					circles.enter().append("circle")
					.attr("fill", d => depColorMap[getDeploymentFromKey(d.key)])
					.attr("cx", d => scatterScales[$scope.scatterX](d.kpis[$scope.scatterX]))
					.attr("cy", d => (450 - scatterScales[$scope.scatterY](d.kpis[$scope.scatterY])))
					.attr("r", 3)
					.on("mouseover", function (d) {
						tooltipDiv.transition()
						.duration(200)
						.style("opacity", .9);
						tooltipDiv.html("<b>" + getWorkloadFromKey(d.key) + "</b><table><tr><td>Cost</td><td>" + (Math.round(100 * d.values.cost) / 100) + " €</td></tr><tr><td>Energy</td><td>" + (Math.round(100 * d.values.energy) / 100) + " Wh</td></tr><tr><td>Duration&nbsp;&nbsp;</td><td>" + (Math.round(100 * d.values.duration) / 100) + " ms</td></tr></table")
						.style("left", (d3.event.pageX) + "px")
						.style("top", (d3.event.pageY - 28) + "px");
					})
					.on("mouseout", function (d) {
						tooltipDiv.transition()
						.duration(500)
						.style("opacity", 0);
					});

					var factsData = [];

					circles.classed("hidden", function (d) {

						var result = false;
						for (var j = 0; j < kpiDef.length; j++) {
							if (i != j) {
								result = result || (globalBrush[j] && (globalBrush[j][0] > d.kpis[j] || d.kpis[j] > globalBrush[j][1]));
							} else {
								globalBrush[j] = null;
							}

						}
						if (!result) {
							factsData.push(d);
						}
						return result;
					});

					updateList(factsData);

				};

				// Draw a barChart
				function barChart() {
					if (!barChart.id)
						barChart.id = 0;

					var margin = {
						top : 10,
						right : 10,
						bottom : 20,
						left : 10
					},
					x,
					y = d3.scale.linear().range([200, 0]),
					id = barChart.id++,
					axis = d3.svg.axis().orient("bottom").ticks(8),
					brush = d3.svg.brush(),
					brushDirty,
					dimension,
					group,
					round;

					function chart(div) {
						var width = x.range()[1],
						height = y.range()[0];

						y.domain([0, allEventsData.length]);

						div.each(function () {
							var div = d3.select(this),
							g = div.select("g");

							// Create the skeletal chart.
							if (g.empty()) {
								div.select(".title").insert("a", ":first-child")
								.attr("href", "javascript:reset(" + id + ")")
								.attr("class", "reset")
								.text("reset")
								.style("display", "none");

								g = div.append("svg")
									.attr("width", width + margin.left + margin.right)
									.attr("height", height + margin.top + margin.bottom)
									.append("g")
									.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

								g.append("clipPath")
								.attr("id", "clip-" + id)
								.append("rect")
								.attr("width", width)
								.attr("height", height);

								g.selectAll(".bar")
								.data(["background", "foreground"])
								.enter().append("path")
								.attr("class", function (d) {
									return d + " bar";
								})
								.datum(group.all());

								g.selectAll(".foreground.bar")
								.attr("clip-path", "url(#clip-" + id + ")");

								g.append("g")
								.attr("class", "axis")
								.attr("transform", "translate(0," + height + ")")
								.call(axis);

								// Initialize the brush component with pretty resize handles.
								var gBrush = g.append("g").attr("class", "brush").call(brush);
								gBrush.selectAll("rect").attr("height", height);
								gBrush.selectAll(".resize").append("path").attr("d", resizePath);

							}

							// Only redraw the brush if set externally.
							if (brushDirty) {
								brushDirty = false;
								g.selectAll(".brush").call(brush);
								div.select(".title a").style("display", brush.empty() ? "none" : null);
								if (brush.empty()) {
									g.selectAll("#clip-" + id + " rect")
									.attr("x", 0)
									.attr("width", width);
								} else {
									var extent = brush.extent();

									g.selectAll("#clip-" + id + " rect")
									.attr("x", x(extent[0]))
									.attr("width", x(extent[1]) - x(extent[0]));
								}
							}

							g.selectAll(".bar").attr("d", barPath);
						});

						function barPath(groups) {
							var path = [],
							i = -1,
							n = groups.length,
							d;
							while (++i < n) {
								d = groups[i];
								path.push("M", x(d.key), ",", height, "V", y(d.value), "h9V", height);
							}
							return path.join("");
						}

						function resizePath(d) {
							var e =  + (d == "e"),
							x = e ? 1 : -1,
							y = height / 3;
							return "M" + (.5 * x) + "," + y
							 + "A6,6 0 0 " + e + " " + (6.5 * x) + "," + (y + 6)
							 + "V" + (2 * y - 6)
							 + "A6,6 0 0 " + e + " " + (.5 * x) + "," + (2 * y)
							 + "Z"
							 + "M" + (2.5 * x) + "," + (y + 8)
							 + "V" + (2 * y - 8)
							 + "M" + (4.5 * x) + "," + (y + 8)
							 + "V" + (2 * y - 8);
						}
					}

					brush.on("brushstart.chart", function () {
						var div = d3.select(this.parentNode.parentNode.parentNode);

						div.select(".title a").style("display", null);
					});

					var factsData;
					brush.on("brush.chart", function () {
						// console.log(this);
						var g = d3.select(this.parentNode),
						extent = brush.extent(),

						scatterplot = d3.select(this.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode).select("svg[id='svgchart']"),

						brushedChartId = d3.select(this.parentNode.parentNode.parentNode)[0][0].id;
						factsData = [];
						scatterplot.selectAll("circle").classed("hidden", function (d) {
							var e = extent;

							var result = false;
							for (var j = 0; j < kpiDef.length; j++) {
								if (brushedChartId === "chart-" + j) {
									globalBrush[j] = e;
								}
								result = result || (globalBrush[j] && (globalBrush[j][0] > d.kpis[j] || d.kpis[j] > globalBrush[j][1]));
							}

							if (!result) {
								factsData.push(d);
							}
							return result;
						});
						updateList(factsData);

						if (round)
							g.select(".brush")
							.call(brush.extent(extent = extent.map(round)))
							.selectAll(".resize")
							.style("display", null);
						g.select("#clip-" + id + " rect")
						.attr("x", x(extent[0]))
						.attr("width", x(extent[1]) - x(extent[0]));
						dimension.filterRange(extent);
					});

					brush.on("brushend.chart", function () {

						if (brush.empty()) {
							var div = d3.select(this.parentNode.parentNode.parentNode);
							div.select(".title a").style("display", "none");
							div.select("#clip-" + id + " rect").attr("x", null).attr("width", "100%");
							dimension.filterAll();
						}
					});

					chart.margin = function (_) {
						if (!arguments.length)
							return margin;
						margin = _;
						return chart;
					};

					chart.x = function (_) {
						if (!arguments.length)
							return x;
						x = _;
						axis.scale(x);
						brush.x(x);
						return chart;
					};

					chart.y = function (_) {
						if (!arguments.length)
							return y;
						y = _;
						return chart;
					};

					chart.dimension = function (_) {
						if (!arguments.length)
							return dimension;
						dimension = _;
						return chart;
					};

					chart.filter = function (_) {
						if (_) {
							brush.extent(_);
							dimension.filterRange(_);
						} else {
							brush.clear();

							dimension.filterAll();
						}
						brushDirty = true;
						return chart;
					};

					chart.group = function (_) {
						if (!arguments.length)
							return group;
						group = _;
						return chart;
					};

					chart.round = function (_) {
						if (!arguments.length)
							return round;
						round = _;
						return chart;
					};

					return d3.rebind(chart, brush, "on");
				}

				function scatterPlot() {

					var padding = 50,
					size = 500;
					var brush = d3.svg.brush();
					var x = d3.scale.linear()
						.range([0, size]);

					var y = d3.scale.linear()
						.range([0, size]);

					var first = true;
					var svg;
					var brush = d3.svg.brush()
						.x(x)
						.y(y);

					function chart(div) {

						if (first) {

							svg = div.select("svg[id='svgchart']")
								.attr("width", size + padding)
								.attr("height", size + padding)
								.attr("fill", "#fff")
								.append("g")
								.attr("id", "circlesArea")
								.attr("transform", "translate(" + padding + "," + padding / 2 + ")");

							svg.append("rect")
							.attr("class", "frame")
							.attr("x", padding / 2)
							.attr("y", padding / 2)
							.attr("width", 500)
							.attr("height", size - padding);

							svg.append("line")
							.attr("x1", 0)
							.attr("y1", size - padding)
							.attr("x2", size)
							.attr("y2", size - padding)
							.attr("stroke-width", 2)
							.attr("stroke", "black");

							svg.append("line")
							.attr("x1", 0)
							.attr("y1", 0)
							.attr("x2", 0)
							.attr("y2", size - padding)
							.attr("stroke-width", 2)
							.attr("stroke", "black");

							svg.append("text")
							.style("fill", "black")
							.style("font-size", "13px")
							.attr("text-anchor", "middle")
							.attr("transform", "translate(-40,30) rotate(-90)")
							.text(kpiDef[$scope.scatterY].name + " (" + kpiDef[$scope.scatterY].unit + ")");

							svg.append("text")
							.style("fill", "black")
							.style("font-size", "13px")
							.attr("text-anchor", "middle")
							.attr("transform", "translate(450,490)")
							.text(kpiDef[$scope.scatterX].name + " (" + kpiDef[$scope.scatterX].unit + ")");

							var xMax = scatterScales[$scope.scatterX].domain()[1],
							yMax = scatterScales[$scope.scatterY].domain()[1];

							for (var tick = 6; tick > 0; tick--) {
								console.log(xMax / tick);
								svg.append("text")
								.style("fill", "black")
								.style("font-size", "10px")
								.attr("text-anchor", "middle")
								.attr("transform", "translate(" + Math.round(scatterScales[$scope.scatterX](tick * (xMax / 6))) + ",470)")
								.text(Math.round(tick * (xMax / 6)));
							}

							console.log(yMax);
							for (var tick = 6; tick > 0; tick--) {
								// console.log(Math.round(yMax/tick));
								svg.append("text")
								.style("fill", "black")
								.style("font-size", "10px")
								.attr("text-anchor", "middle")
								.attr("transform", "translate(-15," + (500 - Math.round(scatterScales[$scope.scatterY](tick * (yMax / 6))) - 50) + ")")
								.text(Math.round(tick * yMax / 6));
							}

							first = false;
						}

						var circles = svg.selectAll("circle").data(nestByWLDepl);

						circles.enter().append("circle")
						.attr("fill", d => depColorMap[getDeploymentFromKey(d.key)])
						.attr("cx", d => scatterScales[$scope.scatterX](d.kpis[$scope.scatterX]))
						.attr("cy", d => (size - scatterScales[$scope.scatterY](d.kpis[$scope.scatterY]) - padding))
						.attr("r", 3)
						.on("mouseover", function (d) {
							tooltipDiv.transition()
							.duration(200)
							.style("opacity", .9);
							tooltipDiv.html("<b>" + getWorkloadFromKey(d.key) + "</b><table><tr><td>Cost</td><td>" + (Math.round(100 * d.values.cost) / 100) + " €</td></tr><tr><td>Energy</td><td>" + (Math.round(100 * d.values.energy) / 100) + " Wh</td></tr><tr><td>Duration&nbsp;&nbsp;</td><td>" + (Math.round(100 * d.values.duration) / 100) + " ms</td></tr></table")
							.style("left", (d3.event.pageX) + "px")
							.style("top", (d3.event.pageY - 28) + "px");
						})
						.on("mouseout", function (d) {
							tooltipDiv.transition()
							.duration(500)
							.style("opacity", 0);
						});

						circles.exit().remove();

					}

					return d3.rebind(chart, brush, "on");
				}

				function getDeploymentFromKey(key) {
					var dashPos = key.lastIndexOf('-');
					if (dashPos > 0) {
						return key.substr(dashPos + 1);
					}
				}

				function getWorkloadFromKey(key) {
					var dashPos = key.lastIndexOf('-');
					if (dashPos > 0) {
						return key.substr(0, dashPos);
					}
				}

				function updateList(factsData) {

					var facts = d3.select("#fact-table").selectAll("tr").select("td").data(factsData);
					var factsEnter = facts.enter().append("tr").append("td").attr("style", function (d) {
							return "width:80%; text-align: right; padding-right: 25px; font-size: 13px; padding-bottom:5px; color:" + depColorMap[getDeploymentFromKey(d.key)]
						}).html(function (d) {
							return getWorkloadFromKey(d.key) + "<span style=' position:relative; float:right; margin-left:2em;'>" + getDeploymentFromKey(d.key) + "</span>";
						});
					//factsEnter.append("td").text(function(d) { return getDeploymentFromKey(d.key) ;});
					facts.exit().remove();

				}

			}

		}
	);
