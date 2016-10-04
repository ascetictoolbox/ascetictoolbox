# HAProxy dynamic configuration employing ASCETiC PaaS layer

Created by David García Pérez (david.garciaperez@atos.net)

## License

This project is under Apache 2.0 license. Details can be found in the file: LICENSE.TXT

## Code considerations

This work employs a bit old images of Debian Squeeze, probably part of the scripts that bootstrap the environment need updates to a more actual environment.

## Installation instructions

To install, simple copy all this files to your HAProxy machine.

In the config folder you will find two files:

* config.cfg you need to update those variables to your own configuration.
* haproxy.cfg is the template file for your configuration. Please, never start haproxy in debug mode, or this will not work.

## Run the scripts

Once configured, you simple need to execute the following command as root:

```
nohup ./bootstrap.sh
```
