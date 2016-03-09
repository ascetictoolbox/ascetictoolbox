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

CREATE TABLE IF NOT EXISTS host_profile_data
  (
     host_profile_id INT NOT NULL,
     host_id        INT,
	 type			VARCHAR(50),
     value          DOUBLE
  );

ALTER TABLE host_profile_data
ADD CONSTRAINT pk_host_profile_data PRIMARY KEY (host_profile_id);

ALTER TABLE `host_profile_data` 
CHANGE COLUMN `host_profile_id` `host_profile_id` INT(11) NOT NULL AUTO_INCREMENT ;

ALTER TABLE host_profile_data
ADD CONSTRAINT fk_host_profile_data_host_id
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

CREATE TABLE IF NOT EXISTS vm
  (
     vm_id   INT NOT NULL,
     vm_name VARCHAR(50),
	 deployment_id VARCHAR(50)
  );

ALTER TABLE vm
ADD CONSTRAINT pk_vmID PRIMARY KEY (vm_id);

CREATE TABLE IF NOT EXISTS vm_measurement
  (
     measurement_id INT NOT NULL,
     host_id INT NOT NULL,
     vm_id   INT NOT NULL,
     clock     BIGINT UNSIGNED,
     cpu_load    DOUBLE,
	 power_overhead    DOUBLE
  );

ALTER TABLE vm_measurement
ADD CONSTRAINT pk_vm_measurementID PRIMARY KEY (measurement_id);

ALTER TABLE `vm_measurement` 
CHANGE COLUMN `measurement_id` `measurement_id` INT(11) NOT NULL AUTO_INCREMENT ;

ALTER TABLE vm_measurement
ADD CONSTRAINT fk_vm_measurement_host_id
FOREIGN KEY (host_id)
REFERENCES host(host_id);

ALTER TABLE vm_measurement
ADD CONSTRAINT fk_vm_measurement_vm_id
FOREIGN KEY (vm_id)
REFERENCES vm(vm_id);

CREATE INDEX idx_vm_clock ON vm_measurement (clock);
CREATE INDEX idx_vm_measure_vm_clock ON vm_measurement (vm_id, clock);
CREATE INDEX idx_vm_measure_spd ON vm_measurement (host_id, vm_id, clock);

CREATE INDEX idx_host_clock ON host_measurement (clock);
CREATE INDEX idx_host_measure_spd ON host_measurement (host_id, clock);

CREATE TABLE IF NOT EXISTS vm_app_tag_arr
  (
     vm_id          INT NOT NULL,
	 vm_app_tag_id 	INT NOT NULL
  );
  
CREATE TABLE IF NOT EXISTS vm_disk_arr
  (
     vm_id          INT NOT NULL,
	 vm_disk_id 	INT NOT NULL
  ); 
 
  
CREATE TABLE IF NOT EXISTS vm_app_tag
  (
     vm_app_tag_id  INT NOT NULL,
     tag_name       VARCHAR(255)
  );  

CREATE TABLE IF NOT EXISTS vm_disk
  (
     vm_disk_id  INT NOT NULL,
     disk_name       VARCHAR(255)
  );    
  
ALTER TABLE vm_app_tag
ADD CONSTRAINT pk_vm_app_tag PRIMARY KEY (vm_app_tag_id);

ALTER TABLE vm_disk
ADD CONSTRAINT pk_vm_disk PRIMARY KEY (vm_disk_id);

ALTER TABLE `vm_app_tag` 
CHANGE COLUMN `vm_app_tag_id` `vm_app_tag_id` INT(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `vm_disk` 
CHANGE COLUMN `vm_disk_id` `vm_disk_id` INT(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE vm_app_tag 
ADD UNIQUE INDEX tag_name_UNIQUE (tag_name ASC);

ALTER TABLE vm_disk 
ADD UNIQUE INDEX vm_disk_UNIQUE (disk_name ASC);

ALTER TABLE vm_app_tag_arr
ADD CONSTRAINT pk_vm_app_tag_arr PRIMARY KEY (vm_app_tag_id, vm_id);

ALTER TABLE vm_disk_arr
ADD CONSTRAINT pk_vm_disk_arr PRIMARY KEY (vm_disk_id, vm_id);

ALTER TABLE vm_app_tag_arr
ADD CONSTRAINT fk_vm_app_arr_vm_id
FOREIGN KEY (vm_id)
REFERENCES vm(vm_id);

ALTER TABLE vm_app_tag_arr
ADD CONSTRAINT fk_vm_app_arr_vm_app_tag_id
FOREIGN KEY (vm_app_tag_id)
REFERENCES vm_app_tag(vm_app_tag_id);

ALTER TABLE vm_disk_arr
ADD CONSTRAINT fk_vm_disk_arr_vm_id
FOREIGN KEY (vm_id)
REFERENCES vm(vm_id);

ALTER TABLE vm_disk_arr
ADD CONSTRAINT fk_vm_disk_arr_vm_disk_id
FOREIGN KEY (vm_disk_id)
REFERENCES vm_disk(vm_disk_id);