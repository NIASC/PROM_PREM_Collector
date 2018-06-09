-- MySQL dump 10.13  Distrib 5.7.19, for Linux (x86_64)
--
-- Host: localhost    Database: prom_prem_db
-- ------------------------------------------------------
-- Server version	5.5.5-10.1.25-MariaDB

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
-- Table structure for table `clinics`
--

DROP TABLE IF EXISTS `clinics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clinics` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clinics`
--

LOCK TABLES `clinics` WRITE;
/*!40000 ALTER TABLE `clinics` DISABLE KEYS */;
/*!40000 ALTER TABLE `clinics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `error_messages`
--

DROP TABLE IF EXISTS `error_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `error_messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` int(10) NOT NULL,
  `name` varchar(64) NOT NULL,
  `locale` varchar(8) NOT NULL,
  `message` varchar(512) NOT NULL,
  PRIMARY KEY (`name`),
  KEY `INDEX` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `error_messages`
--

LOCK TABLES `error_messages` WRITE;
/*!40000 ALTER TABLE `error_messages` DISABLE KEYS */;
INSERT INTO `error_messages` VALUES (9,4,'NOT_LOGGED_IN','en','You may not perform this action unless you log in.'),(2,2,'NULL_SELECTED','en','Nothing selected!'),(8,3,'OPERATION_NOT_PERMITTED','en','Your account does not have permission to perform this operation.'),(13,401,'QP_INVALID_PID','en','Valid personal id numbers formats are: yymmddxxxx, yymmdd-xxxx, yyyymmddxxxx, yyyymmdd-xxxx'),(12,108,'REG_REQUEST_FAILED','en','Could not send request'),(10,106,'UH_ALREADY_ONLINE','en','That user is already online.'),(3,101,'UH_INVALID_LOGIN','en','Invalid username or password.'),(4,102,'UH_PR_INVALID_CURRENT','en','Current password is invalid.'),(6,104,'UH_PR_INVALID_LENGTH','en','Password must contain between 6 and 32 characters.'),(5,103,'UH_PR_MISMATCH_NEW','en','New passwords must match.'),(7,105,'UH_PR_PASSWORD_SIMPLE','en','Password should contain ASCII characters from at least 2 different groups out of: lowercase, uppercase, digits, punctuation (including space).'),(11,107,'UH_SERVER_FULL','en','The server is full. Try again later.'),(1,1,'UNKNOWN_RESPONSE','en','Unknown response.'),(15,502,'VD_FEW_ENTRIES','en','For privacy reasons the selected period must contain at leas 5 entries. Please extend the period.'),(14,501,'VD_INVALID_PERIOD','en','Invalid time period');
/*!40000 ALTER TABLE `error_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `info_messages`
--

DROP TABLE IF EXISTS `info_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `info_messages` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `code` int(10) NOT NULL,
  `name` varchar(64) NOT NULL,
  `message` varchar(512) NOT NULL,
  `locale` varchar(8) NOT NULL,
  PRIMARY KEY (`name`),
  KEY `INDEX` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `info_messages`
--

LOCK TABLES `info_messages` WRITE;
/*!40000 ALTER TABLE `info_messages` DISABLE KEYS */;
INSERT INTO `info_messages` VALUES (6,102,'CURRENT_PASSWORD','Enter current password','en'),(4,2,'EXIT','Exit','en'),(2,3,'LOGIN','Login','en'),(11,7,'LOGOUT','Log out','en'),(7,103,'NEW_PASSWORD','Enter new password','en'),(5,101,'NEW_PASS_INFO','The new password must be between 6 and 32 characters in length and contain ASCII characters from at least 2 different groups out of: lowercase, uppercase, digits, punctuation (including space).','en'),(38,401,'Q_PATIENT_FORENAME','Patient forename','en'),(40,403,'Q_PATIENT_PNR','Patient id number','en'),(39,402,'Q_PATIENT_SURNAME','Patient surname','en'),(3,4,'REGISTER','Register','en'),(15,201,'REG_CLINIC_NAME','Clinic','en'),(22,207,'REG_REQUEST_SENT','Registration request sent','en'),(17,203,'REG_USER_EMAIL','E-mail','en'),(16,202,'REG_USER_NAME','Name','en'),(8,104,'RE_NEW_PASSWORD','Reenter new password','en'),(1,1,'SELECT_OPTION','What would you like to do?','en'),(9,5,'START_QUESTIONNAIRE','Start questionnaire','en'),(13,106,'UH_ENTER_PASSWORD','Enter password','en'),(12,105,'UH_ENTER_USERNAME','Enter username','en'),(14,107,'UH_UPDATE_PASSWORD','Your account have been flagged for password update','en'),(29,307,'UI_FILLED','filled','en'),(27,305,'UI_FORM_BACK','Back','en'),(24,302,'UI_FORM_CONTINUE','Continue','en'),(23,301,'UI_FORM_FINISH','Finish','en'),(26,304,'UI_FORM_NEXT','Next','en'),(28,306,'UI_FORM_OPTIONAL','Optional','en'),(25,303,'UI_FORM_PREVIOUS','Previous','en'),(30,308,'UI_UNFILLED','unfilled','en'),(37,507,'VD_DATE_DAY','Day','en'),(33,503,'VD_DATE_FROM','From','en'),(36,506,'VD_DATE_MONTH','Month','en'),(34,504,'VD_DATE_TO','To','en'),(35,505,'VD_DATE_YEAR','Year','en'),(32,501,'VD_SELECT_PREIOD','Select which period you want to see statistics for.','en'),(31,502,'VD_SELECT_QUESTIONS','Select which questions you would like to view','en'),(10,6,'VIEW_STATISTICS','View statistics','en');
/*!40000 ALTER TABLE `info_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `patients` (
  `clinic_id` int(10) NOT NULL,
  `identifier` varchar(128) NOT NULL,
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`identifier`),
  KEY `INDEX` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questionnaire`
--

DROP TABLE IF EXISTS `questionnaire`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questionnaire` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(128) NOT NULL,
  `optional` int(1) unsigned NOT NULL DEFAULT '0',
  `question` varchar(512) NOT NULL,
  `description` varchar(512) NOT NULL,
  `option0` varchar(128) DEFAULT NULL,
  `option1` varchar(128) DEFAULT NULL,
  `option2` varchar(128) DEFAULT NULL,
  `max_val` int(10) DEFAULT NULL,
  `min_val` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questionnaire`
--

LOCK TABLES `questionnaire` WRITE;
/*!40000 ALTER TABLE `questionnaire` DISABLE KEYS */;
INSERT INTO `questionnaire` VALUES (1,'SingleOption',0,'Mobility','','I have no problems in walking about','I have some problems in walking about','I am confined to bed',NULL,NULL),(2,'SingleOption',0,'Self-Care','','I have no problems with self-care','I have some problems with washing or dressing myself','I am unable to wash or dress myself',NULL,NULL),(3,'SingleOption',0,'Usual Activities','Examples: work, study, housework, family or leisure activities','I have no problems with performing my usual activities','I have some problems with performing my usual activities','I am unable to perform my usual activities',NULL,NULL),(4,'SingleOption',0,'Pain / Discomfort','','I have no pain or discomfort','I have moderate pain or discomfort','I have extreme pain or discomfort',NULL,NULL),(5,'SingleOption',0,'Anxiety / Depression','','I am not anxious or depressed','I am moderately anxious or depressed','I am extremely anxious or depressed',NULL,NULL),(6,'Slider',0,'Your own health state today','Please indicate on this scale how good or bad your own health state is today.\r\n0: The worst health state you can imagine.\r\n10: The best health state you can imagine.',NULL,NULL,NULL,10,0),(7,'Slider',0,'Beskedet om cellförändringar.','Hur upplevde du svarsbrevet/beskedet med att du har/hade cellförändringar?\r\n0: Mycket dålig.\r\n10: Mycket bra.',NULL,NULL,NULL,10,0),(8,'Slider',0,'Tiden från svarsbeskedet till mottagningsbesöket.','Hur upplevde du tiden till mottagningsbesöket från svaret om att du har cellförändringar på livmoderhalsen?\r\n0: Mycket dålig.\r\n10: Mycket bra.',NULL,NULL,NULL,10,0),(9,'Slider',0,'Min upplevelse av mottagningsbesöket','0: Mycket dålig.\r\n10: Mycket bra.',NULL,NULL,NULL,10,0),(10,'Area',1,'Vad skulle kunna förbättras?','Har du något förslag på något särskillt som skulle kunna förbättras kan du skriva det här',NULL,NULL,NULL,NULL,NULL),(11,'Slider',0,'Informationen från personal vid besöket.','Hur upplevde du att informationen du fick av läkaren/sköterskan vid besöket var?\r\n0: Mycket dålig.\r\n10: Mycket bra.',NULL,NULL,NULL,10,0),(12,'SingleOption',0,'Fick information om svaret på behandlingen.','Jag fick information om hur svaret på behandlingen kommer ske','Ja','Nej',NULL,NULL,NULL),(13,'SingleOption',0,'Rekommendera mottagningen till andra.','Jag kan rekommendera denna mottagning till andra kvinnor som behöver behandla cellförändringar?','Ja','Nej',NULL,NULL,NULL);
/*!40000 ALTER TABLE `questionnaire` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questionnaire_answers`
--

DROP TABLE IF EXISTS `questionnaire_answers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questionnaire_answers` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `clinic_id` int(11) NOT NULL,
  `question0` varchar(256) NOT NULL,
  `question1` varchar(256) NOT NULL,
  `question2` varchar(256) NOT NULL,
  `question3` varchar(256) NOT NULL,
  `question4` varchar(256) NOT NULL,
  `question5` varchar(128) NOT NULL,
  `question6` varchar(256) NOT NULL,
  `question7` varchar(256) NOT NULL,
  `question8` varchar(256) NOT NULL,
  `question9` varchar(512) NOT NULL,
  `question10` varchar(256) NOT NULL,
  `question11` varchar(256) NOT NULL,
  `question12` varchar(256) NOT NULL,
  `patient_identifier` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questionnaire_answers`
--

LOCK TABLES `questionnaire_answers` WRITE;
/*!40000 ALTER TABLE `questionnaire_answers` DISABLE KEYS */;
/*!40000 ALTER TABLE `questionnaire_answers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `clinic_id` int(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `password` varchar(256) NOT NULL,
  `email` varchar(128) NOT NULL,
  `registered` date NOT NULL,
  `salt` varchar(32) NOT NULL,
  `update_password` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-03 10:12:56
