OUTDATED BY THE MOMENT. USE SOME PROXY LIKE NGINX TO PROVIDE SECURE COMMUNICATION

Put here your SSL certificate for the server. For testing purposes you can sign it yourself:

Create server key
$ openssl genrsa -out serverKey.pem 1024

Create server certificate request and signed certificate
$ openssl req -new -key serverKey.pem -out servercertrequest.csr
... bunch of prompts
$ openssl x509 -req -in servercertrequest.csr -signkey serverKey.pem -out serverCert.pem

Client-side:
Create private key
$ openssl genrsa -out clientKey.pem 1024

Create client certificate request
$ openssl req -new -key clientKey.pem -out clientCertRequest.csr

Send .csr file to the server and allow server to sign it

$ openssl x509 -req -in clientCertRequest.csr -signkey serverKey.pem -out clientCert.pem
