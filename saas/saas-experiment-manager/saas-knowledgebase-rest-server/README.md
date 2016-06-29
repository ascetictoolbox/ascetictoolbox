# ASCETIC SaaS Knowledge Base

## Overview
This server is used by the experiment runner to store data computed in the PaaS in order to analyse it the experiment dashboard.

Technically, it consists in a REST full server built on a Mongo DB


## Requirement

- Docker 1.12 or greater

## Run the server

Just run this command to build the docker image

docker build -t saasknowledgebase .

Publish the image in your docker registry (e.g. vm-018066.int.cetic.be:5000)

docker tag saasknowledgebase  vm-018066.int.cetic.be:5000/saasknowledgebase
docker push vm-018066.int.cetic.be:5000/saasknowledgebase

On the host machine,

docker pull vm-018066.int.cetic.be:5000/saasknowledgebase
docker run -p8080:8080 vm-018066.int.cetic.be:5000/saasknowledgebase
