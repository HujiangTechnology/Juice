# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 192.168.1.1 (MySQL 5.7.16)
# Database: juice
# Generation Time: 2017-03-28 04:40:36 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table juice_framework
# ------------------------------------------------------------

DROP TABLE IF EXISTS `juice_framework`;

CREATE TABLE `juice_framework` (
  `framework_tag` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `framework_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_update_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_active` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`framework_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



# Dump of table juice_task
# ------------------------------------------------------------

DROP TABLE IF EXISTS `juice_task`;

CREATE TABLE `juice_task` (
  `task_id` bigint(15) NOT NULL COMMENT '任务Id',
  `task_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务名称',
  `tenant_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门ID',
  `docker_image` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'docker镜像名',
  `commands` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'commands',
  `task_status` tinyint(1) NOT NULL COMMENT '任务状态',
  `message` text COLLATE utf8mb4_unicode_ci COMMENT '任务说明',
  `callback_at` datetime DEFAULT NULL COMMENT '回调时间',
  `callback_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回调URL',
  `submit_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务提交时间',
  `finish_at` datetime DEFAULT NULL,
  `agent_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'agent id',
  PRIMARY KEY (`task_id`,`submit_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
/*!50100 PARTITION BY RANGE (month(submit_at))
(PARTITION part0 VALUES LESS THAN (2) ENGINE = InnoDB,
 PARTITION part1 VALUES LESS THAN (3) ENGINE = InnoDB,
 PARTITION part2 VALUES LESS THAN (4) ENGINE = InnoDB,
 PARTITION part3 VALUES LESS THAN (5) ENGINE = InnoDB,
 PARTITION part4 VALUES LESS THAN (6) ENGINE = InnoDB,
 PARTITION part5 VALUES LESS THAN (7) ENGINE = InnoDB,
 PARTITION part6 VALUES LESS THAN (8) ENGINE = InnoDB,
 PARTITION part7 VALUES LESS THAN (9) ENGINE = InnoDB,
 PARTITION part8 VALUES LESS THAN (10) ENGINE = InnoDB,
 PARTITION part9 VALUES LESS THAN (11) ENGINE = InnoDB,
 PARTITION part10 VALUES LESS THAN (12) ENGINE = InnoDB,
 PARTITION part11 VALUES LESS THAN (13) ENGINE = InnoDB) */;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
