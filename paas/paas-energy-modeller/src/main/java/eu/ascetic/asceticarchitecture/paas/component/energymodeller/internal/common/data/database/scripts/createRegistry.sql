/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
USE ascetic_paas_em;
CREATE TABLE IF NOT EXISTS APPLICATION_REGISTRY (
   app_id INT NOT NULL,
   deploy_id INT NOT NULL,
   vm_id INT NOT NULL,
   iaas_id varchar(15),
   start BIGINT,
   stop BIGINT,
   profile_id INT,
   model_id INT,
   PRIMARY KEY (app_id,deploy_id,vm_id)
);