--
--     Copyright (C) 2013 Contributors
--
--     This program is free software: you can redistribute it and/or modify
--     it under the terms of the GNU General Public License as published by
--     the Free Software Foundation, either version 3 of the License, or
--     (at your option) any later version.
--
--     This program is distributed in the hope that it will be useful,
--     but WITHOUT ANY WARRANTY; without even the implied warranty of
--     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--     GNU General Public License for more details.
--
--     You should have received a copy of the GNU General Public License
--     along with this program.  If not, see <http://www.gnu.org/licenses/>
--

create database wattsup;
use wattsup;

create table measurement
(
  id bigint unsigned not null primary key auto_increment,
  time long not null,
  watts double not null,
  volts double not null,
  amps double not null,
  wattskwh double not null,
  maxwatts double not null,
  maxvolts double not null,
  maxamps double not null,
  minwatts double not null,
  minvolts double not null,
  minamps double not null,
  powerfactor double not null,
  dutycycle double not null,
  powercycle double not null
);

create unique index idx_time on measurement (time(64));