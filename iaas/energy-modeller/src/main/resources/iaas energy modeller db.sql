CREATE SCHEMA `iaas_energy_modeller` ;

CREATE TABLE IF NOT EXISTS host
  (
     host_id   INT NOT NULL,
     host_name VARCHAR(50)
  );

ALTER TABLE host
ADD CONSTRAINT pk_hostID PRIMARY KEY (host_id);

CREATE TABLE IF NOT EXISTS host_calibration_data
  (
     calibration_id INT NOT NULL,
     host_id        INT,
     cpu            DOUBLE,
     memory         DOUBLE,
     energy         DOUBLE
  );

ALTER TABLE host_calibration_data
ADD CONSTRAINT pk_host_calibration_data PRIMARY KEY (calibration_id);

ALTER TABLE `host_calibration_data` 
CHANGE COLUMN `calibration_id` `calibration_id` INT(11) NOT NULL AUTO_INCREMENT ;

ALTER TABLE host_calibration_data
ADD CONSTRAINT fk_host_id
FOREIGN KEY (host_id)
REFERENCES host(host_id);

CREATE TABLE IF NOT EXISTS host_measurement
  (
     measurement_id INT NOT NULL,
     host_id   INT NOT NULL,
     clock     BIGINT UNSIGNED,
     energy    DOUBLE,
     power     DOUBLE
  );

ALTER TABLE host_measurement
ADD CONSTRAINT pk_measurementID PRIMARY KEY (measurement_id);

ALTER TABLE `host_measurement` 
CHANGE COLUMN `measurement_id` `measurement_id` INT(11) NOT NULL AUTO_INCREMENT ;

ALTER TABLE host_measurement
ADD CONSTRAINT fk_measurement_host_id
FOREIGN KEY (host_id)
REFERENCES host(host_id);