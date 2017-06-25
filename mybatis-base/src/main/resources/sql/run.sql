/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50718
Source Host           : localhost:3306
Source Database       : mybatis

Target Server Type    : MYSQL
Target Server Version : 50718
File Encoding         : 65001

Date: 2017-06-24 14:24:24
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cf_order
-- ----------------------------
DROP TABLE IF EXISTS `cf_order`;
CREATE TABLE `cf_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) DEFAULT NULL,
  `order_name` varchar(255) DEFAULT NULL,
  `uid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cf_order
-- ----------------------------
INSERT INTO `cf_order` VALUES ('1', 'e71346c4-58a4-11e7-907b-a6006ad3dba0', '衣服', '1');
INSERT INTO `cf_order` VALUES ('2', '3b518ac0-58a5-11e7-907b-a6006ad3dba0', '珠宝', '1');
INSERT INTO `cf_order` VALUES ('3', '59289b60-58a5-11e7-907b-a6006ad3dba0', '箱包', '3');
INSERT INTO `cf_order` VALUES ('4', '6218b020-58a5-11e7-907b-a6006ad3dba0', '花卉', '10');
INSERT INTO `cf_order` VALUES ('5', '66445cc6-58a5-11e7-907b-a6006ad3dba0', '3c', '11');
INSERT INTO `cf_order` VALUES ('6', '6a16dd1a-58a5-11e7-907b-a6006ad3dba0', '驾校', '2');
INSERT INTO `cf_order` VALUES ('7', '6e0186dc-58a5-11e7-907b-a6006ad3dba0', '自行车', '5');
INSERT INTO `cf_order` VALUES ('8', '71dd15be-58a5-11e7-907b-a6006ad3dba0', '汽车', '2');
INSERT INTO `cf_order` VALUES ('9', '74f26e8e-58a5-11e7-907b-a6006ad3dba0', '手表', '13');
INSERT INTO `cf_order` VALUES ('10', '78077aec-58a5-11e7-907b-a6006ad3dba0', '厨具', '11');
INSERT INTO `cf_order` VALUES ('11', '7da6d1dc-58a5-11e7-907b-a6006ad3dba0', '床上用品', '10');
INSERT INTO `cf_order` VALUES ('12', '814a4d46-58a5-11e7-907b-a6006ad3dba0', '鞋子', '8');
INSERT INTO `cf_order` VALUES ('13', '84d99f02-58a5-11e7-907b-a6006ad3dba0', '书桌', '5');
INSERT INTO `cf_order` VALUES ('14', '884ce34c-58a5-11e7-907b-a6006ad3dba0', '台灯', '1');
INSERT INTO `cf_order` VALUES ('15', '8bc4b54a-58a5-11e7-907b-a6006ad3dba0', '纸巾', '12');
INSERT INTO `cf_order` VALUES ('16', '8f72a210-58a5-11e7-907b-a6006ad3dba0', '电脑', '11');

-- ----------------------------
-- Table structure for cf_role
-- ----------------------------
DROP TABLE IF EXISTS `cf_role`;
CREATE TABLE `cf_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cf_role
-- ----------------------------
INSERT INTO `cf_role` VALUES ('1', '管理员');
INSERT INTO `cf_role` VALUES ('2', '客服');
INSERT INTO `cf_role` VALUES ('3', '开发');

-- ----------------------------
-- Table structure for cf_user
-- ----------------------------
DROP TABLE IF EXISTS `cf_user`;
CREATE TABLE `cf_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `sex` int(255) DEFAULT NULL,
  `role_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cf_user
-- ----------------------------
INSERT INTO `cf_user` VALUES ('1', 'Jack', '1', '1');
INSERT INTO `cf_user` VALUES ('2', 'Lucy', '2', '1');
INSERT INTO `cf_user` VALUES ('3', 'Sunny', '2', '3');
INSERT INTO `cf_user` VALUES ('4', 'Marshall', '1', '2');
INSERT INTO `cf_user` VALUES ('5', 'June', '2', '2');
INSERT INTO `cf_user` VALUES ('6', 'Benjamin', '1', '1');
INSERT INTO `cf_user` VALUES ('7', 'Caroline', '2', '1');
INSERT INTO `cf_user` VALUES ('8', 'Turos', '1', '3');
INSERT INTO `cf_user` VALUES ('9', 'Abns', '2', '2');
INSERT INTO `cf_user` VALUES ('10', 'Wasj', '1', '2');
INSERT INTO `cf_user` VALUES ('11', 'Fisher', '1', '1');
INSERT INTO `cf_user` VALUES ('12', 'Pors', '2', '2');
INSERT INTO `cf_user` VALUES ('13', 'Yons', '1', '3');

-- ----------------------------
-- Table structure for user_login
-- ----------------------------
DROP TABLE IF EXISTS `user_login`;
CREATE TABLE `user_login` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(32) NOT NULL,
  `logindate` datetime DEFAULT NULL,
  `loginip` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_login
-- ----------------------------
INSERT INTO `user_login` VALUES ('1', 'test1', '2014-10-11 12:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('2', 'test1', '2014-10-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('3', 'test1', '2014-10-21 14:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('4', 'test1', '2014-11-21 11:20:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('5', 'test1', '2014-11-21 13:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('6', 'test2', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('7', 'test2', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('8', 'test3', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('9', 'test4', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login` VALUES ('10', 'test5', '2014-11-21 12:00:00', '192.168.1.123');

-- ----------------------------
-- Table structure for user_login2
-- ----------------------------
DROP TABLE IF EXISTS `user_login2`;
CREATE TABLE `user_login2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(32) NOT NULL,
  `logindate` datetime DEFAULT NULL,
  `loginip` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_login2
-- ----------------------------
INSERT INTO `user_login2` VALUES ('1', 'test1', '2014-10-11 12:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('2', 'test1', '2014-10-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('3', 'test1', '2014-10-21 14:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('4', 'test1', '2014-11-21 11:20:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('5', 'test1', '2014-11-21 13:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('6', 'test2', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('7', 'test2', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('8', 'test3', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('9', 'test4', '2014-11-21 12:00:00', '192.168.1.123');
INSERT INTO `user_login2` VALUES ('10', 'test5', '2014-11-21 12:00:00', '192.168.1.123');
