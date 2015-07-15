/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
CREATE TABLE IF NOT EXISTS EMONITORING (monitoringid int NOT NULL AUTO_INCREMENT, applicationid varchar(20),deploymentid varchar(20),type varchar(20), started timestamp DEFAULT '2014-01-01 00:00:00', ended timestamp DEFAULT  '2014-01-01 00:00:00', status boolean, events varchar(50), PRIMARY KEY (monitoringid));
CREATE TABLE IF NOT EXISTS DATAEVENT (applicationid varchar(50),deploymentid varchar(50),vmid varchar(50), eventid varchar(50), data varchar(100), starttime bigint,endtime bigint, energy double);
CREATE TABLE IF NOT EXISTS DATACONSUMPTION (applicationid varchar(50),deploymentid varchar(50),vmid varchar(50), eventid varchar(50), time bigint, vmenergy double, vmpower double,vmcpu double);
