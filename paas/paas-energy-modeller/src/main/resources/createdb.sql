/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
 
CREATE DATABASE IF NOT EXISTS ascetic_paas_em;

USE ascetic_paas_em;

/* M. Fontanella - 20 Jan 2016 - begin */
/* M. Fontanella - 10 Feb 2016 - begin */
/* M. Fontanella - 12 Feb 2016 - begin */
CREATE TABLE IF NOT EXISTS EMONITORING (monitoringid int NOT NULL AUTO_INCREMENT, providerid varchar(50), applicationid varchar(50),deploymentid varchar(50),start BIGINT DEFAULT 0, stop BIGINT DEFAULT 0, status boolean, events varchar(50), PRIMARY KEY (monitoringid));
/* M. Fontanella - 12 Feb 2016 - end */
/* M. Fontanella - 10 Feb 2016 - end */
/* CREATE TABLE IF NOT EXISTS DATAEVENT (applicationid varchar(50),deploymentid varchar(50),vmid varchar(50), eventid varchar(50), data varchar(100), starttime bigint,endtime bigint, energy double);*/
CREATE TABLE IF NOT EXISTS DATACONSUMPTION (providerid varchar(50),applicationid varchar(50),deploymentid varchar(50),vmid varchar(50), eventid varchar(50), metrictype varchar(50), time bigint, vmenergy double, vmpower double,vmcpu double,vmmemory double);
/* M. Fontanella - 10 Feb 2016 - begin */
CREATE TABLE IF NOT EXISTS APPLICATION_REGISTRY (providerid varchar(50) NOT NULL, appplicationid varchar(50) NOT NULL, deploymentid varchar(50) NOT NULL, vmid varchar(50) NOT NULL, iaasid varchar(50), start BIGINT, stop BIGINT, energy double, power double, profileid INT,   modelid INT,   PRIMARY KEY (providerid,applicationid,deploymentid,vmid));
/* M. Fontanella - 10 Feb 2016 - end */
/* M. Fontanella - 20 Jan 2016 - end */