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


Si te gustan mis aportaciones a github, quizás te gustará mi libro [Del bit a la Nube](http://www.xaas.guru/del-bit-a-la-nube/)
