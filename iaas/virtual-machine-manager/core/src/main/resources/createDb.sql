CREATE TABLE IF NOT EXISTS virtual_machines
(id VARCHAR(255),
  appId VARCHAR(255),
  ovfId VARCHAR(255),
  slaId VARCHAR(255),
PRIMARY KEY (id)) ;

CREATE TABLE IF NOT EXISTS current_scheduling_alg
(algorithm VARCHAR(255),
PRIMARY KEY (algorithm));

CREATE TABLE IF NOT EXISTS self_adaptation_options
(options LONGVARCHAR);

CREATE TABLE IF NOT EXISTS users
(
  username VARCHAR(256) UNIQUE not null,
password VARCHAR(2048) NOT NULL,
  enabled BOOLEAN not null
);

CREATE TABLE IF NOT EXISTS authorities (
  username VARCHAR(256) FOREIGN KEY REFERENCES users(username),
  authority VARCHAR(50))