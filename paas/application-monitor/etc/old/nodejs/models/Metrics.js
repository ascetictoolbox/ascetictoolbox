// TODO: Mirar si se puede usar mapreduce para esto

module.exports = function(mongoose) {
    console.log("initializing Metrics schema...");

    var Schema = mongoose.Schema;
    var MetricsSchema = new Schema({
        applicationId : {type: String, index: true},    // Identifier of the application
        nodeId : {type: String, index: true},           // Identifier of the node
        timestamp : {type: Number, index: true},        // timestamp in milliseconds
        values : Object
    },{autoIndex: false});

    /* Add here any method
    MetricSetSchema.methods.methodName = function() {
        (some method code here)
        return (something)
    }*/
	/**
	 * returns an object with the registered apps in the next format:
	 * {"IntellijIdea":["MyMac"],"TestApp":["TestNode","TestNode2","TestNode1"]}
	 */

	var addTimeConstraints = function(query,startTimeMs,endTimeMs) {
		if(startTimeMs) query = query.where("timestamp").gte(startTimeMs);
		if(endTimeMs) query = query.where("timestamp").lt(endTimeMs);
		return query;
	}

	MetricsSchema.statics.findRegisteredApps = function(startTimeMs, endTimeMS, callback) {
		console.log("mierda");
		var appQuery = addTimeConstraints(mongoose.model("Metrics").distinct("applicationId"),startTimeMs,endTimeMS);
		appQuery.exec(function(err, apps) {
			if(err) callback(err);
			var attacher = function(appslist, returnDocument) {
				if(appslist.length == 0) {
					callback(null,returnDocument);
					return;
				}
				var app = appslist.pop();
				var nodeQuery = addTimeConstraints(mongoose.model("Metrics").distinct("nodeId"));
				nodeQuery.where("applicationId").equals(app);

				nodeQuery.exec(function(err, nodes) {
					if(err) callback(err);
					returnDocument[app] = nodes;
					attacher(appslist, returnDocument);
				});
			}
			attacher(apps,{});
		});
	};

	// TODO: consider using threads_a_gogo (or any other solution) too much metrics makes the system go slowing
	// another option is to move the calculation of average metrics to mongodb
	MetricsSchema.statics.findAllMetrics = function(startTimeMS, endTimeMS, appId, nodeId, callback) {
		var query = addTimeConstraints(mongoose.model("Metrics").find(),startTimeMS, endTimeMS);
		query = query.where("applicationId").equals(appId)
			.where("nodeId").equals(nodeId);

		query.exec(callback);
	}

	MetricsSchema.statics.findLastMetric = function(appId, nodeId, callback) {
		var query = mongoose.model("Metrics").find({applicationId:appId,"nodeId":nodeId}).sort({timestamp:-1}).limit(1);
		query.exec(callback);
	};

	// indexing data values for accelerating search
	return mongoose.model("Metrics", MetricsSchema);

}