// TODO use expression ids or hashes for more compact storage

var parser = require("./metric-expression"),
    tiers = require("./tiers"),
    types = require("./types"),
    reduces = require("./reduces"),
    event = require("./event"),
    setImmediate = require("./set-immediate");

var metric_fields = {v: 1},
    metric_options = {sort: {"_id.t": 1}, batchSize: 1000},
    event_options = {sort: {t: 1}, batchSize: 1000};

// Query for metrics.
exports.getter = function(db) {
  var collection = types(db),
      Double = db.bson_serializer.Double,
      queueByName = {},
      meta = event.putter(db);

  function getter(request, callback) {
    var start = new Date(request.start),
        stop = new Date(request.stop),
        id = request.id;

    // Validate the dates.
    if (isNaN(start)) return callback({error: "invalid start"}), -1;
    if (isNaN(stop)) return callback({error: "invalid stop"}), -1;

    // Parse the expression.
    var expression;
    try {
      expression = parser.parse(request.expression);
    } catch (e) {
      return callback({error: "invalid expression"}), -1;
    }

    // Round start and stop to the appropriate time step.
    var tier = tiers[+request.step];
    if (!tier) return callback({error: "invalid step"}), -1;
    start = tier.floor(start);
    stop = tier.ceil(stop);

    // Compute the request metric!
    measure(expression, start, stop, tier, "id" in request
        ? function(time, value) { callback({time: time, value: value, id: id}); }
        : function(time, value) { callback({time: time, value: value}); });
  }

  // Computes the metric for the given expression for the time interval from
  // start (inclusive) to stop (exclusive). The time granularity is determined
  // by the specified tier, such as daily or hourly. The callback is invoked
  // repeatedly for each metric value, being passed two arguments: the time and
  // the value. The values may be out of order due to partial cache hits.
  function measure(expression, start, stop, tier, callback) {
    (expression.op ? binary : expression.type ? unary : constant)(expression, start, stop, tier, callback);
  }

  // Computes a constant expression;
  function constant(expression, start, stop, tier, callback) {
    var value = expression.value();
    while (start < stop) {
      callback(start, value);
      start = tier.step(start);
    }
    callback(stop);
  }

  // Serializes a unary expression for computation.
  function unary(expression, start, stop, tier, callback) {
    var remaining = 0,
        time0 = Date.now(),
        time = start,
        name = expression.source,
        queue = queueByName[name],
        step = tier.key;

    // Compute the expected number of values.
    while (time < stop) ++remaining, time = tier.step(time);

    // If no results were requested, return immediately.
    if (!remaining) return callback(stop);

    // Add this task to the appropriate queue.
    if (queue) queue.next = task;
    else setImmediate(task);
    queueByName[name] = task;

    function task() {
      findOrComputeUnary(expression, start, stop, tier, function(time, value) {
        callback(time, value);
        if (!--remaining) {
          callback(stop);
          if (task.next) setImmediate(task.next);
          else delete queueByName[name];

          // Record how long it took us to compute as an event!
          var time1 = Date.now();
          meta({
            type: "cube_compute",
            time: time1,
            data: {
              expression: expression.source,
              ms: time1 - time0
            }
          });
        }
      });
    }
  }

  // Finds or computes a unary (primary) expression.
  function findOrComputeUnary(expression, start, stop, tier, callback) {
    var name = expression.type,
        type = collection(name),
        map = expression.value,
        reduce = reduces[expression.reduce],
        filter = {t: {}},
        fields = {t: 1};

    // Copy any expression filters into the query object.
    expression.filter(filter);

    // Request any needed fields.
    expression.fields(fields);

    find(start, stop, tier, callback);

    // The metric is computed recursively, reusing the above variables.
    function find(start, stop, tier, callback) {
      var compute = tier.next && reduce.pyramidal ? computePyramidal : computeFlat,
          step = tier.key;

      // Query for the desired metric in the cache.
      type.metrics.find({
        i: false,
        "_id.e": expression.source,
        "_id.l": tier.key,
        "_id.t": {
          $gte: start,
          $lt: stop
        }
      }, metric_fields, metric_options, foundMetrics);

      // Immediately report back whatever we have. If any values are missing,
      // merge them into contiguous intervals and asynchronously compute them.
      function foundMetrics(error, cursor) {
        handle(error);
        var time = start;
        cursor.each(function(error, row) {
          handle(error);
          if (row) {
            callback(row._id.t, row.v);
            if (time < row._id.t) compute(time, row._id.t);
            time = tier.step(row._id.t);
          } else {
            if (time < stop) compute(time, stop);
          }
        });
      }

      // Group metrics from the next tier.
      function computePyramidal(start, stop) {
        var bins = {};
        find(start, stop, tier.next, function(time, value) {
          var bin = bins[time = tier.floor(time)] || (bins[time] = {size: tier.size(time), values: []});
          if (bin.values.push(value) === bin.size) {
            save(time, reduce(bin.values));
            delete bins[time];
          }
        });
      }

      // Group raw events. Unlike the pyramidal computation, here we can control
      // the order in which rows are returned from the database. Thus, we know
      // when we've seen all of the events for a given time interval.
      function computeFlat(start, stop) {
        filter.t.$gte = start;
        filter.t.$lt = stop;
        type.events.find(filter, fields, event_options, function(error, cursor) {
          handle(error);
          var time = start, values = [];
          cursor.each(function(error, row) {
            handle(error);
            if (row) {
              var then = tier.floor(row.t);
              if (time < then) {
                save(time, values.length ? reduce(values) : reduce.empty);
                while ((time = tier.step(time)) < then) save(time, reduce.empty);
                values = [map(row)];
              } else {
                values.push(map(row));
              }
            } else {
              save(time, values.length ? reduce(values) : reduce.empty);
              while ((time = tier.step(time)) < stop) save(time, reduce.empty);
            }
          });
        });
      }

      function save(time, value) {
        callback(time, value);
        if (value) {
          type.metrics.save({
            _id: {
              e: expression.source,
              l: tier.key,
              t: time
            },
            i: false,
            v: new Double(value)
          }, handle);
        }
      }
    }
  }

  // Computes a binary expression by merging two subexpressions.
  function binary(expression, start, stop, tier, callback) {
    var left = {}, right = {};

    measure(expression.left, start, stop, tier, function(t, l) {
      if (t in right) {
        callback(t, t < stop ? expression.op(l, right[t]) : l);
        delete right[t];
      } else {
        left[t] = l;
      }
    });

    measure(expression.right, start, stop, tier, function(t, r) {
      if (t in left) {
        callback(t, t < stop ? expression.op(left[t], r) : r);
        delete left[t];
      } else {
        right[t] = r;
      }
    });
  }

  return getter;
};

function handle(error) {
  if (error) throw error;
}
