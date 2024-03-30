-- MySQL dump 10.13  Distrib 5.7.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sky_take_out
-- ------------------------------------------------------
-- Server version	5.7.44

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
-- Table structure for table `address_book`
--

DROP TABLE IF EXISTS `address_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `address_book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `consignee` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
  `sex` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT '性别',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT '手机号',
  `province_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '省级区划编号',
  `province_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '省级名称',
  `city_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '市级区划编号',
  `city_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '市级名称',
  `district_code` varchar(12) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '区级区划编号',
  `district_name` varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '区级名称',
  `detail` varchar(200) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '详细地址',
  `label` varchar(100) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '标签',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '默认 0 否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='地址簿';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address_book`
--

LOCK TABLES `address_book` WRITE;
/*!40000 ALTER TABLE `address_book` DISABLE KEYS */;
INSERT INTO `address_book` VALUES (1,1,'张三','0','13112344321','44','广东省','4412','肇庆市','441202','端州区','广东工商职业技术大学','3',1),(2,1,'李四','0','13556789001','11','北京市','1101','市辖区','110101','东城区','翻斗花园','1',0);
/*!40000 ALTER TABLE `address_book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` int(11) DEFAULT NULL COMMENT '类型   1 菜品分类 2 套餐分类',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '分类名称',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '顺序',
  `status` int(11) DEFAULT NULL COMMENT '分类状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='菜品及套餐分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (11,1,'酒水饮料',10,1,'2022-06-09 22:09:18','2022-06-09 22:09:18',1,1),(12,1,'传统主食',9,1,'2022-06-09 22:09:32','2022-06-09 22:18:53',1,1),(13,2,'人气套餐',12,1,'2022-06-09 22:11:38','2022-06-10 11:04:40',1,1),(15,2,'商务套餐',13,1,'2022-06-09 22:14:10','2022-06-10 11:04:48',1,1),(16,1,'蜀味烤鱼',4,1,'2022-06-09 22:15:37','2024-03-20 00:57:00',1,1),(17,1,'蜀味牛蛙',5,1,'2022-06-09 22:16:14','2024-03-20 00:57:01',1,1),(18,1,'特色蒸菜',6,1,'2022-06-09 22:17:42','2024-03-20 00:57:03',1,1),(19,1,'新鲜时蔬',7,1,'2022-06-09 22:18:12','2024-03-20 00:57:04',1,1),(20,1,'水煮鱼',8,1,'2022-06-09 22:22:29','2024-03-20 00:57:06',1,1),(21,1,'汤类',11,1,'2022-06-10 10:51:47','2022-06-10 10:51:47',1,1);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish`
--

DROP TABLE IF EXISTS `dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '菜品名称',
  `category_id` bigint(20) NOT NULL COMMENT '菜品分类id',
  `price` decimal(10,2) DEFAULT NULL COMMENT '菜品价格',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '描述信息',
  `status` int(11) DEFAULT '1' COMMENT '0 停售 1 起售',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dish_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='菜品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish`
--

LOCK TABLES `dish` WRITE;
/*!40000 ALTER TABLE `dish` DISABLE KEYS */;
INSERT INTO `dish` VALUES (46,'王老吉',11,6.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png','',1,'2022-06-09 22:40:47','2024-02-01 23:27:11',1,1),(47,'北冰洋',11,4.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png','还是小时候的味道',1,'2022-06-10 09:18:49','2024-02-01 18:43:08',1,1),(48,'雪花啤酒',11,4.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png','',1,'2022-06-10 09:22:54','2024-02-01 18:43:22',1,1),(49,'米饭',12,2.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/67d0a66e-7a3f-4dcf-a2a8-936da5baa907.png','精选五常大米',1,'2022-06-10 09:30:17','2024-02-01 18:43:35',1,1),(50,'馒头',12,1.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/c1b1379f-5d82-4ca8-8da4-2c16d9ceee17.png','优质面粉',1,'2022-06-10 09:34:28','2024-02-01 18:43:48',1,1),(51,'老坛酸菜鱼',20,56.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/5039caff-7866-4d14-afbc-f486edf97366.png','原料：汤，草鱼，酸菜',1,'2022-06-10 09:40:51','2024-02-01 18:44:02',1,1),(52,'经典酸菜鮰鱼',20,66.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/4b549c0f-a228-44cd-a089-a1d85e9a3d85.png','原料：酸菜，江团，鮰鱼',1,'2022-06-10 09:46:02','2024-02-01 18:44:15',1,1),(53,'蜀味水煮草鱼',20,38.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/67586611-a9d7-4401-ad2f-0a9d42c704e8.png','原料：草鱼，汤',1,'2022-06-10 09:48:37','2024-02-01 18:44:28',1,1),(54,'清炒小油菜',19,18.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/4ca5754c-259e-4677-82da-d8ad7befaede.png','原料：小油菜',1,'2022-06-10 09:51:46','2024-02-01 18:44:40',1,1),(55,'蒜蓉娃娃菜',19,18.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/f8635d8e-7ff6-4fb3-916c-585b05670ac7.png','原料：蒜，娃娃菜',1,'2022-06-10 09:53:37','2024-02-01 18:44:52',1,1),(56,'清炒西兰花',19,18.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/542528ca-f7cf-4b73-8272-36708445bbaf.png','原料：西兰花',1,'2022-06-10 09:55:44','2024-02-01 18:45:07',1,1),(57,'炝炒圆白菜',19,18.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/b46190cd-cabf-4e90-b9b8-55abd25ae041.png','原料：圆白菜',1,'2022-06-10 09:58:35','2024-02-01 18:45:17',1,1),(58,'清蒸鲈鱼',18,98.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/22302335-ffba-419a-acdd-684e67b78258.png','原料：鲈鱼',1,'2022-06-10 10:12:28','2024-02-01 18:45:30',1,1),(59,'东坡肘子',18,138.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/f8c56714-ac98-48f0-9ebf-e8935c90087c.png','原料：猪肘棒',1,'2022-06-10 10:24:03','2024-02-01 18:45:40',1,1),(60,'梅菜扣肉',18,58.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e0efb119-bc80-4f91-9475-47dcad00ff58.png','原料：猪肉，梅菜',1,'2022-06-10 10:26:03','2024-02-01 18:45:51',1,1),(61,'剁椒鱼头',18,66.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8eccdea9-9262-437e-9b60-26e7a8cad54e.png','原料：鲢鱼，剁椒',1,'2022-06-10 10:28:54','2024-02-01 18:46:02',1,1),(62,'金汤酸菜牛蛙',17,88.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/7bd3fa57-6adc-4935-9c32-743a09690d58.png','原料：鲜活牛蛙，酸菜',1,'2022-06-10 10:33:05','2024-02-01 18:46:12',1,1),(63,'香锅牛蛙',17,88.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8d617835-4391-4c87-be19-9b2717e463d0.png','配料：鲜活牛蛙，莲藕，青笋',1,'2022-06-10 10:35:40','2024-02-01 18:46:21',1,1),(64,'馋嘴牛蛙',17,88.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/5e81235a-18c0-4011-acd3-2ccf41f97693.png','配料：鲜活牛蛙，丝瓜，黄豆芽',1,'2022-06-10 10:37:52','2024-02-01 18:46:29',1,1),(65,'草鱼2斤',16,68.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/963880f4-3163-4f13-9ce8-80d374b90dd8.png','原料：草鱼，黄豆芽，莲藕',1,'2022-06-10 10:41:08','2024-02-01 18:46:38',1,1),(66,'江团鱼2斤',16,119.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/043018f9-ce35-4b18-b367-82f7ac2b9097.png','配料：江团鱼，黄豆芽，莲藕',1,'2022-06-10 10:42:42','2024-02-01 18:46:45',1,1),(67,'鮰鱼2斤',16,72.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/4bd16ca1-c390-4c57-8d41-489e539f9eb7.png','原料：鮰鱼，黄豆芽，莲藕',1,'2022-06-10 10:43:56','2024-02-01 18:46:52',1,1),(68,'鸡蛋汤',21,4.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/1ab5d727-6d82-4952-8b36-35566c91144f.png','配料：鸡蛋，紫菜',1,'2022-06-10 10:54:25','2024-02-06 22:10:23',1,1),(69,'平菇豆腐汤',21,6.00,'https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/5dd386eb-50be-4afa-a377-1a011f1877ed.png','配料：豆腐，平菇',1,'2022-06-10 10:55:02','2024-02-01 18:47:28',1,1);
/*!40000 ALTER TABLE `dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish_flavor`
--

DROP TABLE IF EXISTS `dish_flavor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dish_flavor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dish_id` bigint(20) NOT NULL COMMENT '菜品',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '口味名称',
  `value` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '口味数据list',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='菜品口味关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_flavor`
--

LOCK TABLES `dish_flavor` WRITE;
/*!40000 ALTER TABLE `dish_flavor` DISABLE KEYS */;
INSERT INTO `dish_flavor` VALUES (40,10,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(41,7,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(42,7,'温度','[\"热饮\",\"常温\",\"去冰\",\"少冰\",\"多冰\"]'),(45,6,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(46,6,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(47,5,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(48,5,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(49,2,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(50,4,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(51,3,'甜味','[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]'),(52,3,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(107,51,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(108,51,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(109,52,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(110,52,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(111,53,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(112,53,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(113,54,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\"]'),(114,56,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(115,57,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(116,60,'忌口','[\"不要葱\",\"不要蒜\",\"不要香菜\",\"不要辣\"]'),(117,65,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(118,66,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]'),(119,67,'辣度','[\"不辣\",\"微辣\",\"中辣\",\"重辣\"]');
/*!40000 ALTER TABLE `dish_flavor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '姓名',
  `username` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) COLLATE utf8_bin NOT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8_bin NOT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8_bin NOT NULL COMMENT '身份证号',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='员工信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'管理员','admin','e10adc3949ba59abbe56e057f20f883e','13812312312','1','110101199001010047',1,'2022-02-15 15:51:20','2022-02-17 09:16:20',10,1),(2,'张三','zhangsan','e10adc3949ba59abbe56e057f20f883e','13812344321','1','111222333444555666',1,'2024-01-29 22:27:04','2024-03-19 15:19:44',10,10),(3,'李四','lisi','e10adc3949ba59abbe56e057f20f883e','13812344321','1','111222333444555666',0,'2024-01-29 22:29:20','2024-03-19 17:09:13',10,10),(4,'王五','wangwu','e10adc3949ba59abbe56e057f20f883e','13812344321','1','111222333444555666',1,'2024-01-29 23:39:53','2024-03-19 02:07:03',1,1);
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_detail`
--

DROP TABLE IF EXISTS `order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '名字',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `dish_id` bigint(20) DEFAULT NULL COMMENT '菜品id',
  `setmeal_id` bigint(20) DEFAULT NULL COMMENT '套餐id',
  `dish_flavor` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '口味',
  `number` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
INSERT INTO `order_detail` VALUES (34,'馋嘴牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/5e81235a-18c0-4011-acd3-2ccf41f97693.png',16,64,NULL,NULL,5,88.00),(35,'香锅牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8d617835-4391-4c87-be19-9b2717e463d0.png',17,63,NULL,NULL,1,88.00),(36,'金汤酸菜牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/7bd3fa57-6adc-4935-9c32-743a09690d58.png',17,62,NULL,NULL,1,88.00),(37,'剁椒鱼头','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8eccdea9-9262-437e-9b60-26e7a8cad54e.png',18,61,NULL,NULL,1,66.00),(38,'东坡肘子','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/f8c56714-ac98-48f0-9ebf-e8935c90087c.png',18,59,NULL,NULL,1,138.00),(39,'馒头','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/c1b1379f-5d82-4ca8-8da4-2c16d9ceee17.png',19,50,NULL,NULL,1,1.00),(40,'馒头','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/c1b1379f-5d82-4ca8-8da4-2c16d9ceee17.png',19,50,NULL,NULL,1,1.00),(41,'米饭','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/67d0a66e-7a3f-4dcf-a2a8-936da5baa907.png',19,49,NULL,NULL,2,2.00),(42,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',20,48,NULL,NULL,1,4.00),(43,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',20,47,NULL,NULL,1,4.00),(44,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',20,46,NULL,NULL,1,6.00),(47,'鸡蛋汤','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/1ab5d727-6d82-4952-8b36-35566c91144f.png',23,68,NULL,NULL,1,4.00),(48,'测试套餐','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/bf9cee9b-21a4-4272-9407-b9b1d377afb6.png',24,NULL,1,NULL,1,50.00),(49,'鸡蛋汤','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/1ab5d727-6d82-4952-8b36-35566c91144f.png',25,68,NULL,NULL,1,4.00),(50,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',25,47,NULL,NULL,1,4.00),(51,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',26,46,NULL,NULL,1,6.00),(52,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',27,48,NULL,NULL,1,4.00),(53,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',27,47,NULL,NULL,1,4.00),(54,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',27,46,NULL,NULL,1,6.00),(55,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',28,48,NULL,NULL,1,4.00),(56,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',28,47,NULL,NULL,1,4.00),(57,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',28,46,NULL,NULL,1,6.00),(58,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',29,48,NULL,NULL,1,4.00),(59,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',29,47,NULL,NULL,1,4.00),(60,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',29,46,NULL,NULL,1,6.00),(61,'剁椒鱼头','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8eccdea9-9262-437e-9b60-26e7a8cad54e.png',30,61,NULL,NULL,1,66.00),(62,'东坡肘子','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/f8c56714-ac98-48f0-9ebf-e8935c90087c.png',30,59,NULL,NULL,1,138.00),(63,'剁椒鱼头','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8eccdea9-9262-437e-9b60-26e7a8cad54e.png',31,61,NULL,NULL,1,66.00),(64,'东坡肘子','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/f8c56714-ac98-48f0-9ebf-e8935c90087c.png',31,59,NULL,NULL,1,138.00),(65,'馋嘴牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/5e81235a-18c0-4011-acd3-2ccf41f97693.png',32,64,NULL,NULL,1,88.00),(66,'测试套餐','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/bf9cee9b-21a4-4272-9407-b9b1d377afb6.png',32,NULL,1,NULL,1,50.00),(67,'金汤酸菜牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/7bd3fa57-6adc-4935-9c32-743a09690d58.png',33,62,NULL,NULL,1,88.00),(68,'米饭','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/67d0a66e-7a3f-4dcf-a2a8-936da5baa907.png',33,49,NULL,NULL,1,2.00),(69,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',33,48,NULL,NULL,1,4.00),(70,'馋嘴牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/5e81235a-18c0-4011-acd3-2ccf41f97693.png',34,64,NULL,NULL,1,88.00),(71,'香锅牛蛙','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/8d617835-4391-4c87-be19-9b2717e463d0.png',34,63,NULL,NULL,1,88.00),(72,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',35,48,NULL,NULL,1,4.00),(73,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',35,47,NULL,NULL,1,4.00),(74,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',35,46,NULL,NULL,1,6.00),(75,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',36,48,NULL,NULL,1,4.00),(76,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',36,47,NULL,NULL,1,4.00),(77,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',36,46,NULL,NULL,1,6.00),(78,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',37,48,NULL,NULL,1,4.00),(79,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',37,47,NULL,NULL,1,4.00),(80,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',37,46,NULL,NULL,1,6.00),(81,'雪花啤酒','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e9a8aebf-7778-4dc6-8266-5c114736d86d.png',38,48,NULL,NULL,1,4.00),(82,'北冰洋','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/571e30dd-ffd0-47f8-ba27-bd4f4827ae05.png',38,47,NULL,NULL,1,4.00),(83,'王老吉','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/e1cb2ffc-87f9-4a93-8ed1-d4db37214536.png',38,46,NULL,NULL,1,6.00);
/*!40000 ALTER TABLE `order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '订单号',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款',
  `user_id` bigint(20) NOT NULL COMMENT '下单用户',
  `address_book_id` bigint(20) NOT NULL COMMENT '地址id',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `checkout_time` datetime DEFAULT NULL COMMENT '结账时间',
  `pay_method` int(11) NOT NULL DEFAULT '1' COMMENT '支付方式 1微信,2支付宝',
  `pay_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '支付状态 0未支付 1已支付 2退款',
  `amount` decimal(10,2) NOT NULL COMMENT '实收金额',
  `remark` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '备注',
  `phone` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT '手机号',
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '地址',
  `user_name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '用户名称',
  `consignee` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
  `cancel_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '订单取消原因',
  `rejection_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '订单拒绝原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '订单取消时间',
  `estimated_delivery_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `delivery_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '配送状态  1立即送出  0选择具体时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '送达时间',
  `pack_amount` int(11) DEFAULT NULL COMMENT '打包费',
  `tableware_number` int(11) DEFAULT NULL COMMENT '餐具数量',
  `tableware_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '餐具数量状态  1按餐量提供  0选择具体数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (16,'1707495784969',5,1,1,'2024-02-10 00:23:05','2024-02-10 00:23:06',1,1,451.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-02-10 01:23:00',1,'2024-02-10 20:35:55',5,0,1),(17,'1707565847842',6,1,1,'2024-02-10 19:50:48','2024-02-10 19:50:49',1,2,184.00,'少加糖','13112344321','广东工商职业技术大学',NULL,'张三','订单超时,自动取消',NULL,'2024-02-11 00:15:35','2024-02-10 20:50:00',1,NULL,2,0,1),(18,'1707566378227',6,1,1,'2024-02-10 19:59:38','2024-02-10 19:59:40',1,2,212.00,'','13112344321','广东工商职业技术大学',NULL,'张三','订单量较多，暂时无法接单',NULL,NULL,'2024-02-10 20:59:00',1,NULL,2,10,1),(19,'1707566480525',6,1,1,'2024-02-10 20:01:21','2024-02-10 20:01:22',1,2,16.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'菜品已销售完，暂时无法接单',NULL,'2024-02-10 21:01:00',1,NULL,4,10,1),(20,'1707566678221',6,1,1,'2024-02-10 20:04:38','2024-02-10 20:04:41',1,2,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'菜品已销售完，暂时无法接单','2024-02-10 20:04:57','2024-02-10 21:04:00',1,NULL,3,10,1),(23,'1707587600813',6,1,1,'2024-02-11 01:53:21','2024-02-11 01:53:22',1,2,11.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'订单量较多，暂时无法接单','2024-02-11 01:54:41','2024-02-11 02:53:00',1,NULL,1,0,1),(24,'1707587692521',6,1,1,'2024-02-11 01:54:53','2024-02-11 01:54:54',1,2,57.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'订单量较多，暂时无法接单','2024-02-11 01:55:36','2024-02-11 02:54:00',1,NULL,1,0,1),(25,'1707587760406',6,1,1,'2024-02-11 01:56:00','2024-02-11 01:56:02',1,2,16.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'订单量较多，暂时无法接单','2024-02-11 01:56:17','2024-02-11 02:55:00',1,NULL,2,0,1),(26,'1707587837061',6,1,1,'2024-02-11 01:57:17','2024-02-11 01:57:19',1,2,13.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'订单量较多，暂时无法接单','2024-02-11 01:57:37','2024-02-11 02:57:00',1,NULL,1,0,1),(27,'1707588007511',5,1,1,'2024-02-11 02:00:08','2024-02-11 02:00:10',1,1,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-02-11 03:00:00',1,'2024-02-14 21:45:04',3,0,1),(28,'1707588047566',5,1,1,'2024-02-11 02:00:48','2024-02-11 02:00:52',1,1,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-02-11 03:00:00',1,'2024-02-14 21:45:04',3,0,1),(29,'1707661031453',5,1,1,'2024-02-11 22:17:11','2024-02-11 22:17:12',1,1,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-02-11 23:17:00',1,'2024-02-14 21:45:03',3,0,1),(30,'1708008030063',5,1,1,'2024-02-15 22:40:30','2024-02-15 22:40:32',1,1,212.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-02-15 23:40:00',1,'2024-02-15 22:41:19',2,0,1),(31,'1708008094206',6,1,1,'2024-02-15 22:41:34','2024-02-15 22:41:36',1,2,212.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,'订单量较多，暂时无法接单','2024-02-15 22:41:55','2024-02-15 23:41:00',1,NULL,2,0,1),(32,'1711028215458',5,1,1,'2024-03-21 21:36:55','2024-03-21 21:36:57',1,1,146.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-03-21 22:36:00',1,'2024-03-21 21:37:34',2,0,1),(33,'1711028920452',5,1,1,'2024-03-21 21:48:40','2024-03-21 21:52:04',1,1,103.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-03-21 22:47:00',1,'2024-03-21 22:41:05',3,0,1),(34,'1711029255171',5,1,1,'2024-03-21 21:54:15','2024-03-21 21:54:18',1,1,184.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-03-21 22:54:00',1,'2024-03-21 22:41:04',2,0,1),(35,'1711101333957',6,1,1,'2024-03-22 17:55:34','2024-03-22 17:55:36',1,2,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三','用户取消',NULL,'2024-03-22 17:58:41','2024-03-22 18:55:00',1,NULL,3,0,1),(36,'1711102104009',6,1,1,'2024-03-22 18:08:24','2024-03-22 18:08:25',1,1,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三','骑手不足无法配送',NULL,'2024-03-22 18:18:00','2024-03-22 19:08:00',1,NULL,3,0,1),(37,'1711102969868',5,1,1,'2024-03-22 18:22:50','2024-03-22 18:22:51',1,1,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-03-22 19:22:00',1,'2024-03-22 18:27:54',3,0,1),(38,'1711104320099',5,1,1,'2024-03-22 18:45:20','2024-03-22 18:45:22',1,1,23.00,'','13112344321','广东工商职业技术大学',NULL,'张三',NULL,NULL,NULL,'2024-03-22 19:45:00',1,'2024-03-22 18:46:26',3,0,1);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `setmeal`
--

DROP TABLE IF EXISTS `setmeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `setmeal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint(20) NOT NULL COMMENT '菜品分类id',
  `name` varchar(32) COLLATE utf8_bin NOT NULL COMMENT '套餐名称',
  `price` decimal(10,2) NOT NULL COMMENT '套餐价格',
  `status` int(11) DEFAULT '1' COMMENT '售卖状态 0:停售 1:起售',
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '描述信息',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_setmeal_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='套餐';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setmeal`
--

LOCK TABLES `setmeal` WRITE;
/*!40000 ALTER TABLE `setmeal` DISABLE KEYS */;
INSERT INTO `setmeal` VALUES (1,13,'测试套餐',50.00,1,'味道挺好！！','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/bf9cee9b-21a4-4272-9407-b9b1d377afb6.png','2024-02-06 20:53:03','2024-02-07 00:04:32',1,1),(2,15,'测试套餐2',100.00,1,'家乡的味道','https://lhj-web-tlias.oss-cn-hangzhou.aliyuncs.com/7fdfafc2-81da-4a47-9468-463492052e13.png','2024-03-20 22:36:13','2024-03-20 22:36:17',1,1);
/*!40000 ALTER TABLE `setmeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `setmeal_dish`
--

DROP TABLE IF EXISTS `setmeal_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `setmeal_dish` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `setmeal_id` bigint(20) DEFAULT NULL COMMENT '套餐id',
  `dish_id` bigint(20) DEFAULT NULL COMMENT '菜品id',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '菜品名称 （冗余字段）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '菜品单价（冗余字段）',
  `copies` int(11) DEFAULT NULL COMMENT '菜品份数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='套餐菜品关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setmeal_dish`
--

LOCK TABLES `setmeal_dish` WRITE;
/*!40000 ALTER TABLE `setmeal_dish` DISABLE KEYS */;
INSERT INTO `setmeal_dish` VALUES (1,1,63,'香锅牛蛙',88.00,1),(2,1,49,'米饭',2.00,1),(3,1,54,'清炒小油菜',18.00,1),(4,1,68,'鸡蛋汤',4.00,1),(5,2,51,'老坛酸菜鱼',56.00,1),(6,2,46,'王老吉',6.00,1),(7,2,68,'鸡蛋汤',4.00,1),(8,2,49,'米饭',2.00,1);
/*!40000 ALTER TABLE `setmeal_dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shopping_cart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '商品名称',
  `image` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '图片',
  `user_id` bigint(20) NOT NULL COMMENT '主键',
  `dish_id` bigint(20) DEFAULT NULL COMMENT '菜品id',
  `setmeal_id` bigint(20) DEFAULT NULL COMMENT '套餐id',
  `dish_flavor` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '口味',
  `number` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='购物车';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '微信用户唯一标识',
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL COMMENT '姓名',
  `phone` varchar(11) COLLATE utf8_bin DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) COLLATE utf8_bin DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) COLLATE utf8_bin DEFAULT NULL COMMENT '身份证号',
  `avatar` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '头像',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'o0B2i6--FO77cAKlzdJSf929_IZc',NULL,NULL,NULL,NULL,NULL,'2024-02-06 02:32:06');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-03-30 17:08:18
