/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
 
CREATE DATABASE IF NOT EXISTS ascetic_paas_em;

USE ascetic_paas_em;

CREATE TABLE IF NOT EXISTS EMONITORING (monitoringid int NOT NULL AUTO_INCREMENT, providerid varchar(50), applicationid varchar(50),deploymentid varchar(50),start BIGINT DEFAULT 0, stop BIGINT DEFAULT 0, status boolean, events varchar(50), PRIMARY KEY (monitoringid));
CREATE TABLE IF NOT EXISTS DATACONSUMPTION (providerid varchar(50),applicationid varchar(50),deploymentid varchar(50),vmid varchar(50), eventid varchar(50), metrictype varchar(50), time bigint, vmenergy double, vmpower double,vmcpu double,vmmemory double);
CREATE TABLE IF NOT EXISTS APPLICATION_REGISTRY (providerid varchar(50) NOT NULL, applicationid varchar(50) NOT NULL, deploymentid varchar(50) NOT NULL, vmid varchar(50) NOT NULL, iaasid varchar(50), start BIGINT, stop BIGINT, energy double, power double, profileid INT,   modelid INT,   PRIMARY KEY (providerid,applicationid,deploymentid,vmid));
CREATE TABLE IF NOT EXISTS CPUFEATURES (model varchar(50), core int,tdp double,minpower double,maxpower double, PRIMARY KEY (model));