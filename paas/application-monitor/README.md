appmon
======

Application Monitor, created as part of the ASCETiC EU Project (www.ascetic-project.eu)

Requirements
------------

* Java
* MongoDB 2.6.*

How to configure
----------------

* Edit the next properties of the conf/application.conf file
  * mongo.host: hostname or IP of your MongoDB installation
  * mongo.port: port of your MongoDB installation
  * mq.url: url of your AMQP1.0 message queue

How to run:
-----------

From the sources root, run the next command:
`./activator run`

API Documentation
=================

The rest of this document details the REST api for Application Monitor, and provides examples to be executed with [cURL](http://curl.haxx.se/).

## Events

Events are any information that the application will push to the Application Monitor. For
example, metrics about monitoring of processes, CPU status of the machine, or simple 
events/signals (e.g. "Application Start" or "Process Interruption").

An event can be a *continous* event, if it defines a `timestamp` property with the time it was stored
 in the database and an `endtime` property with the timestamp it has ben marked as "finished". A
 *spot* event only defines the initial `timestamp` property.

The information of the event is posted in the body of the HTTP request. It is a JSON
document that includes the next properties:
    
* `_id` (JSON), REQUIRED, AUTO-GENERATED
    * The identifier of the document, as inserted by MongoDB
* `timestamp` (long number), REQUIRED, AUTO-GENERATED
    * A number (of Long java-equivalent type) that holds the time of the event, as number
    of milliseconds since 1/1/1970 00:00, UTC time.
    * This parameter HAS NOT to be specified when posting a new event, since the application
    monitor will automatically add it.
* `endtime` (long number), OPTIONAL
    * A number (of Long java-equivalent type) that holds the finishing time of the event, as number
    of milliseconds since 1/1/1970 00:00, UTC time.
    * An event without
* `appId` (string), REQUIRED
    * The identifier of the application
* `nodeId` (string), REQUIRED
    * The identifier of the node (e.g. hostname, IP, VM ID...) where the process is running
* `deploymentId` (string), OPTIONAL
    * The identifier of the instance/deployment. Useful, for example, when different instances
    of the same application are running at the same time.
* `data` (JSON), OPTIONAL
    * User-defined document with information about the event.

## Post a new event [POST /event]

Submits a new event.

+ Body content: an `application/json` object with the next information:
    * `appId` (required, string)
        * The identifier of the application
    * `nodeId` (required, string)
        * The identifier of the node (e.g. hostname, IP, VM ID...) where the process is running
    * `deploymentId` (optional, string)
        * The identifier of the instance/deployment. Useful, for example, when different instances
        of the same application are running at the same time.
    * `data` (optional, JSON)
        * Free-form, user-defined document with information about the event.
    * Example
    
            { "appId":"Idea",
              "nodeId":"MyMac3",
              "data":{
                 "cpu_pct":0.25,
                 "mem_kb":123043,
                 "mem_pct":0.17
            }}

+ Invocation example:
    
        $ curl -X POST -H "Content-type: application/json" --data \
        '{ "appId":"Idea", "nodeId":"MyMac3", "data":{
        "cpu_pct":0.25, "mem_kb":123043, "mem_pct":0.17}}' \
        http://paas-stable:9000/event

+ Returns
    + Response 200: an `application/json` object with the next properties:
        + `_id`: a string containing the auto-generated unique identifier of the event
        + `timestamp`: a long integer containing the timestamp (in milliseconds) when
         the event was received
        + Example:
        
            {"_id":"53934cba30047a8c9f648508",
            "timestamp":1402162485485}
            
### Update event [PATCH]
Updates the information of an event. For example, to notify that a *continuous* event has ended by
updating its `endtime` property.

The system will merge the submitted document with the document that
is already in the database. If there is a collision of properties in the document tree, the new version
will override the oldest one.

Please note that here, as opposite to the POST operation, the `_id` field is mandatory.

+ Request (application/json)

        {"_id":"53934cba30047a8c9f648508",
        "data": {
            "message" : "This new property will be merged with the old event or, if collision, will override" }}

+ Response 200

## Retrieve [/event/{id}]



### Get event information [GET]

Retrieves the information of an event. Please note that this method returns the identifier
of the event inside the document as an ObjectId BSON notation (as they are actually stored in the
Mongo Database.

The information of the event is posted in the body of the HTTP request. It is a JSON
document that includes the next properties:
    
* `_id` (JSON), REQUIRED, AUTO-GENERATED
    * The identifier of the document, as inserted by MongoDB
* `timestamp` (long number), REQUIRED, AUTO-GENERATED
    * A number (of Long java-equivalent type) that holds the time of the event, as number
    of milliseconds since 1/1/1970 00:00, UTC time.
    * This parameter HAS NOT to be specified when posting a new event, since the application
    monitor will automatically add it.
* `endtime` (long number), OPTIONAL
    * A number (of Long java-equivalent type) that holds the finishing time of the event, as number
    of milliseconds since 1/1/1970 00:00, UTC time.
    * An event without
* `appId` (string), REQUIRED
    * The identifier of the application
* `nodeId` (string), REQUIRED
    * The identifier of the node (e.g. hostname, IP, VM ID...) where the process is running
* `deploymentId` (string), OPTIONAL
    * The identifier of the instance/deployment. Useful, for example, when different instances
    of the same application are running at the same time.
* `data` (JSON), OPTIONAL
    * User-defined document with information about the event.

Events can be *spot* events (if only `timestamp` parameter is specified) or *continuous* events
(if they define both `timestamp` and `endtime`)


+ Parameters
    + id (string)... Identifier of the event, as returned by [POST][/event] or [PATCH][/event]
    
+ Response 200

        { "_id" : { "$oid" : "53b119fee8c6182d22ee7bcc"} , "appId" : "SimpleApp" , "nodeId" : "SimpleNode" , "deploymentId" : 41.12868376428366 , "data" : { "rnd" : 48.553120447770425 , "event" : "UP"} , "timestamp" : 1404115454543}

### Mark as finished [PATCH]

Orders the server to set the `endtime` property of event `id` with its current internal time.
+ Parameters
    + id (string)... Identifier of the event, as returned by [POST][/event] or [PATCH][/event]

+ Response 200

        {"_id":"53934cba30047a8c9f648508",
        "timestamp":1402162485485,
        "endtime": 1402162785485}

## Applications

### List all applications [GET /apps]

Shows the identifiers/names of all the applications that have been registerd during
a given time range, and their nodes.

+ Query Parameters
    + `start` (optional, number)
        * Time stamp (in milliseconds) of the start of the time range.
        * Default value: 0
    + `end (optional, number)
        * time stamp (in milliseconds) of the end of the time range.
        * Default value: now
+ Invocation example:

        $ curl http://paas-stable:9000/apps
        
+ Returns
    + Response 200: an "application/json" object, where each property name belong to the name of a
    registered application, and its corresponding value is an array with all the nodes (VMs) that
    execute such application.
    + Example:

        {"NewApp":["NewNode"],"OtherApp":["Backend","Frontend"],"SimpleApp":["OnlyNode"]}
    


### Post information about a recently finished deployment [POST /apps]

Posts the next information about a finished deployment:

+ Body content: an "application/json" object with the next structure:
    * `appId` (required, string)
        * Application Identifier
    * `deploymentId` (required, string)
        * Instance/Deployment Identifier
    * `data` (required, JSON), containing the next elements:
        * `start` Start time of deployment (free-form string)
        * `end` End time of deployment (free-form string)
        * `power` Total power consumption (free-form string)
    * Example
    
        {"appId" : "MyWebService",
        "deploymentId" : "Deployment3234",
        "data" : {
        	"start" : "2015/10/20 10:33:22 UTC",
        	"end" : "2015/10/25 1:30:29 UTC",
        	"power" : "120 Wh"
        }}
+ Invocation example:

        $ curl -X POST -H "Content-type: application/json" --data \
        '{ "appId" : "MyWebService", "deploymentId" : "Deployment3234", "data" :
        { "start" : "2015/10/20 10:33:22 UTC", "end" : "2015/10/25 1:30:29 UTC",
        "power" : "120 Wh" }}' http://paas-stable:9000/apps
                
+ Returns
    + Response 200: if everything went OK.
    
### Get recently finished deployments [GET /apps/finished]

Shows the information of recently finished deployments, as specified by the [POST /apps] calls, ordered
by the timestamp they were submitted, in descending order.
 
+ Query Parameters
    + `start` (optional, number)
        * Time stamp (in milliseconds) of the start of the time range.
        * Default value: 0
    + `end` (optional, number)
        * Time stamp (in milliseconds) of the end of the time range.
        * Default value: now
    + `limit` (optional, number)
        * Maximum number of results
        * Default value: infinite
    
+ Returns
    + Response 200: an "application/json" Array, where each contained object represents the submitted
     information from a past application deployment, and follows the next structure:
        * `_id`: a JSON object with a property named `$oid`, whose value is a string containing a
          unique identifier for the application information, as stored in the database.
        * `appId`: a string representing the Application Identifier as reported by the client app
        * `deploymentId`: a string representing the Instance/Deployment Identifier as reported by the client
        * `data` a JSON object containing the next properties:
            * `start` Start time of deployment (free-form string) as reported by the client
            * `end` End time of deployment (free-form string) as reported by the client
            * `power` Total power consumption (free-form string) as reported by the client
        * `timestamp` a long integer wit the timestamp (in milliseconds) when the information was reported
    + Example:

        [ { "_id" : { "$oid" : "560d384f929e0764a802a786"},
            "appId" : "MyWebService",
            "deploymentId" : "Deployment3234",
            "data" : {
                "start" : "2015/10/20 10:33:22 UTC" ,
                "end" : "2015/10/25 1:30:29 UTC" ,
                "power" : "120 Wh"
            } ,
            "timestamp" : 1443706959568
          }
          , ( information about other applications)
        ]
    
+ Invocation example:

        $ curl http://paas-stable:9000/apps/finished?limit=4

# Group Queries

A query is an operation to retrieve and aggregate information about metrics, by applying an operation (summatory,
average, maximum, minimum, first, last, count...).

Each query includes in the HTTP request body a JSON Array  with the query information as specified by the
[MongoDB aggregation framework](http://docs.mongodb.org/manual/core/aggregation-introduction/).

For example, in the following sample, there are two operations in the aggregation pipeline:

1. The `$match` operator first filters all the documents whose `event` property in the `data` section
   is equal to `DOWN`. The event property is filtered with the [Dot Notation](http://docs.mongodb.org/manual/core/document/)
   for MongoDB queries.
2. The `$group` operator groups all the documents in a single document (`_id` is set to 0). The property
   `lastTime` of the returned document containes the timestamp of the latest event.

## Basic query creation [/query]

### Submit a query [PUT]
+ Request (application/json)

        [{$match:{"data.event":"DOWN"}},{$group:{_id:0,lastTime:{$max:"$timestamp"}}}]

+ Response 200

        { "_id" : 0, "lastTime" : 1402585905489" }
            




Si te gustan mis aportaciones a github, quizás te gustará mi libro [Del bit a la Nube](http://www.macias.info/del-bit-a-la-nube/)
