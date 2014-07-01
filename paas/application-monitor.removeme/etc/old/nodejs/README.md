Configuring Application Monitor
===

The service is neither secure nor authenticated. In development/test scenarios, please make sure the http port is
accessible only from the development cluster.

In production scenarios, close completely the http access from outside and configure a Proxy Server like nginx to
provide authenticated, secure HTTPS access.

