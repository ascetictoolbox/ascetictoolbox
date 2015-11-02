-- MySQL dump 10.13  Distrib 5.5.24, for Win64 (x86)
--
-- Host: localhost    Database: saas1
-- ------------------------------------------------------
-- Server version	5.5.24-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `applications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applications`
--

LOCK TABLES `applications` WRITE;
/*!40000 ALTER TABLE `applications` DISABLE KEYS */;
INSERT INTO `applications` VALUES (1,'NewsAsset');
/*!40000 ALTER TABLE `applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deploymentitems`
--

DROP TABLE IF EXISTS `deploymentitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `deploymentitems` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deployment` bigint(20) NOT NULL,
  `instance` bigint(20) NOT NULL,
  `component` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deploymentitems`
--

LOCK TABLES `deploymentitems` WRITE;
/*!40000 ALTER TABLE `deploymentitems` DISABLE KEYS */;
INSERT INTO `deploymentitems` VALUES (1,1,1,1),(2,1,4,2),(3,2,1,1),(4,2,5,2),(5,3,1,1),(6,3,6,2),(7,4,2,1),(8,4,4,2),(9,5,2,1),(10,5,5,2),(11,6,2,1),(12,6,6,2),(13,7,3,1),(14,7,4,2),(15,8,3,1),(16,8,5,2),(17,9,3,1),(18,9,6,2),(19,10,1,1),(20,10,1,2),(21,11,2,1),(22,11,2,2),(23,12,3,1),(24,12,3,2);
/*!40000 ALTER TABLE `deploymentitems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deployments`
--

DROP TABLE IF EXISTS `deployments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `deployments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deployments`
--

LOCK TABLES `deployments` WRITE;
/*!40000 ALTER TABLE `deployments` DISABLE KEYS */;
INSERT INTO `deployments` VALUES (1,'mydeployment1'),(2,'mydeployment2'),(3,'mydeployment3'),(4,'mydeployment4'),(5,'mydeployment5'),(6,'mydeployment6'),(7,'mydeployment7'),(8,'mydeployment8'),(9,'mydeployment9'),(10,'mydeployment10'),(11,'mydeployment11'),(12,'mydeployment12');
/*!40000 ALTER TABLE `deployments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `elements`
--

DROP TABLE IF EXISTS `elements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `elements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  `application` bigint(20) NOT NULL,
  `type` varchar(32) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `elements`
--

LOCK TABLES `elements` WRITE;
/*!40000 ALTER TABLE `elements` DISABLE KEYS */;
INSERT INTO `elements` VALUES (1,'NewsAssetServer',1,'component'),(2,'NewsAsserRDBMS',1,'component'),(3,'Search for News Items',1,'feature'),(4,'Save for a News Item',1,'feature');
/*!40000 ALTER TABLE `elements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goals`
--

DROP TABLE IF EXISTS `goals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(256) COLLATE utf8_bin NOT NULL,
  `application` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goals`
--

LOCK TABLES `goals` WRITE;
/*!40000 ALTER TABLE `goals` DISABLE KEYS */;
INSERT INTO `goals` VALUES (1,'Determine Energy Consumption Behaviour Effectiveness of the application',1),(2,'Determine Energy Efficiency of application feature',1);
/*!40000 ALTER TABLE `goals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instances`
--

DROP TABLE IF EXISTS `instances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instancename` varchar(256) COLLATE utf8_bin NOT NULL,
  `cpunumber` int(11) NOT NULL,
  `ram` int(11) NOT NULL,
  `disksize` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instances`
--

LOCK TABLES `instances` WRITE;
/*!40000 ALTER TABLE `instances` DISABLE KEYS */;
INSERT INTO `instances` VALUES (1,'NA_TestVM_1',1,4,40),(2,'NA_TestVM_1',2,4,40),(3,'NA_TestVM_1',1,8,40),(4,'NA_TestVM_2',1,4,40),(5,'NA_TestVM_2',2,4,40),(6,'NA_TestVM_2',1,8,40);
/*!40000 ALTER TABLE `instances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `measurevalues`
--

DROP TABLE IF EXISTS `measurevalues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measurevalues` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session` bigint(20) NOT NULL,
  `instance` bigint(20) NOT NULL,
  `processor` decimal(6,2) NOT NULL,
  `idle` int(11) NOT NULL,
  `workingset` bigint(20) NOT NULL,
  `ioread` decimal(6,2) NOT NULL,
  `iowrite` decimal(6,2) NOT NULL,
  `iodata` decimal(6,2) NOT NULL,
  `vm` decimal(6,2) NOT NULL,
  `node` decimal(8,2) NOT NULL,
  `time` bigint(20) NOT NULL,
  `step` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `measurevalues`
--

LOCK TABLES `measurevalues` WRITE;
/*!40000 ALTER TABLE `measurevalues` DISABLE KEYS */;
INSERT INTO `measurevalues` VALUES (1,1,1,2.05,80,500244388,0.00,0.98,0.98,34.00,71.00,1409672417548,1),(2,1,1,2.53,31,500059042,6.26,1.04,7.30,15.93,66.54,1409672418548,2),(3,1,1,8.70,15,500189424,1.15,1.12,2.27,33.56,83.65,1409672419548,3);
/*!40000 ALTER TABLE `measurevalues` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metrics`
--

DROP TABLE IF EXISTS `metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metrics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  `element` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metrics`
--

LOCK TABLES `metrics` WRITE;
/*!40000 ALTER TABLE `metrics` DISABLE KEYS */;
INSERT INTO `metrics` VALUES (1,'TotalWattHourConsumption',1),(2,'ChronologicWattHourConsumption',1),(3,'ComparativeWattHourConsumption',1),(4,'WattHourConsumption',1),(5,'TotalWattHourConsumption',2),(6,'ChronologicWattHourConsumption',2),(7,'ComparativeWattHourConsumption',2),(8,'WattHourConsumption',2),(9,'TotalWattHourConsumption',3),(10,'ChronologicWattHourConsumption',3),(11,'ComparativeWattHourConsumption',3),(12,'WattHourConsumption',3),(13,'TotalWattHourConsumption',4),(14,'ChronologicWattHourConsumption',4),(15,'ComparativeWattHourConsumption',4),(16,'WattHourConsumption',4);
/*!40000 ALTER TABLE `metrics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(256) COLLATE utf8_bin NOT NULL,
  `element` bigint(20) NOT NULL,
  `goal` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questions`
--

LOCK TABLES `questions` WRITE;
/*!40000 ALTER TABLE `questions` DISABLE KEYS */;
INSERT INTO `questions` VALUES (1,'How much energy is consumed by VMs hosting the component ?',1,1),(2,'How much energy is consumed by VMs hosting the component ?',2,1),(3,'How much energy is consumed on the server side by the feature',3,2),(4,'How much energy is consumed on the server side by the feature',4,2);
/*!40000 ALTER TABLE `questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sessions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sessionname` varchar(256) COLLATE utf8_bin NOT NULL,
  `application` bigint(20) NOT NULL,
  `deployment` bigint(20) NOT NULL,
  `usecase` bigint(20) NOT NULL,
  `starttime` bigint(20) NOT NULL,
  `frequency` decimal(5,2) NOT NULL,
  `numberofmeasure` int(11) NOT NULL,
  `status` varchar(32) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sessions`
--

LOCK TABLES `sessions` WRITE;
/*!40000 ALTER TABLE `sessions` DISABLE KEYS */;
INSERT INTO `sessions` VALUES (1,'My first test',1,1,1,1409672417548,1.00,3,'completed'),(2,'My 2d test',1,12,2,1411128541748,1.00,30,'inprogress');
/*!40000 ALTER TABLE `sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usecases`
--

DROP TABLE IF EXISTS `usecases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usecases` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8_bin NOT NULL,
  `feature` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usecases`
--

LOCK TABLES `usecases` WRITE;
/*!40000 ALTER TABLE `usecases` DISABLE KEYS */;
INSERT INTO `usecases` VALUES (1,'searches with one criterion',3),(2,'searches with several criterion',3),(3,'create simple items',4),(4,'create complex items',4);
/*!40000 ALTER TABLE `usecases` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-10-06 16:35:06
