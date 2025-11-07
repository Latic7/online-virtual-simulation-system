-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        8.0.42 - MySQL Community Server - GPL
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 ovss_db 的数据库结构
DROP DATABASE IF EXISTS `ovss_db`;
CREATE DATABASE IF NOT EXISTS `ovss_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ovss_db`;

-- 导出  表 ovss_db.model 结构
DROP TABLE IF EXISTS `model`;
CREATE TABLE IF NOT EXISTS `model` (
  `ModelID` bigint NOT NULL AUTO_INCREMENT COMMENT '模型唯一主键',
  `ModelName` varchar(255) NOT NULL COMMENT '模型名称',
  `ThumbnailAddress` varchar(512) DEFAULT NULL COMMENT '缩略图地址',
  `FileAddress` varchar(512) NOT NULL COMMENT '模型文件地址',
  `Uploader` bigint NOT NULL COMMENT '上传者ID',
  `UploadTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上传时间',
  `AuditStatus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
  PRIMARY KEY (`ModelID`),
  KEY `fk_Uploader` (`Uploader`),
  CONSTRAINT `fk_Uploader` FOREIGN KEY (`Uploader`) REFERENCES `user` (`UserID`),
  CONSTRAINT `chk_AuditStatus` CHECK ((`AuditStatus` in (_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'REJECTED')))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.model 的数据：~2 rows (大约)
DELETE FROM `model`;
INSERT INTO `model` (`ModelID`, `ModelName`, `ThumbnailAddress`, `FileAddress`, `Uploader`, `UploadTime`, `AuditStatus`) VALUES
	(1, '军用虎钳', '/thumbnails/jaw_vice_thumbnail.png', '/models/jaw_vice.glb', 1, '2025-11-06 22:22:41', 'PENDING'),
	(2, '小方块', '/thumbnails/red_cube.png', '/models/red_cube.glb', 4, '2025-11-07 11:38:11', 'APPROVED');

-- 导出  表 ovss_db.modeltag 结构
DROP TABLE IF EXISTS `modeltag`;
CREATE TABLE IF NOT EXISTS `modeltag` (
  `ModelID` bigint NOT NULL COMMENT '关联的模型ID',
  `TagID` bigint NOT NULL COMMENT '关联的标签ID',
  PRIMARY KEY (`ModelID`,`TagID`) COMMENT '复合主键 模型ID+标签ID',
  KEY `modeltag_ibfk_2` (`TagID`),
  CONSTRAINT `modeltag_ibfk_1` FOREIGN KEY (`ModelID`) REFERENCES `model` (`ModelID`),
  CONSTRAINT `modeltag_ibfk_2` FOREIGN KEY (`TagID`) REFERENCES `tag` (`TagID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.modeltag 的数据：~3 rows (大约)
DELETE FROM `modeltag`;
INSERT INTO `modeltag` (`ModelID`, `TagID`) VALUES
	(1, 1),
	(2, 2),
	(2, 3);

-- 导出  表 ovss_db.tag 结构
DROP TABLE IF EXISTS `tag`;
CREATE TABLE IF NOT EXISTS `tag` (
  `TagID` bigint NOT NULL AUTO_INCREMENT COMMENT '标签唯一主键',
  `TagName` varchar(50) NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`TagID`),
  UNIQUE KEY `TagName` (`TagName`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.tag 的数据：~3 rows (大约)
DELETE FROM `tag`;
INSERT INTO `tag` (`TagID`, `TagName`) VALUES
	(2, 'Blender'),
	(1, '机械'),
	(3, '测试用');

-- 导出  表 ovss_db.user 结构
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `UserID` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一主键',
  `UserName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `UserAuthority` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '权限等级',
  `Password` varchar(255) NOT NULL COMMENT '密码',
  PRIMARY KEY (`UserID`),
  CONSTRAINT `chk_UserAuthority` CHECK ((`UserAuthority` in (_utf8mb4'USER',_utf8mb4'ADMIN')))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.user 的数据：~3 rows (大约)
DELETE FROM `user`;
INSERT INTO `user` (`UserID`, `UserName`, `UserAuthority`, `Password`) VALUES
	(1, 'somebody', 'USER', '123456'),
	(2, 'hello', 'USER', 'HELLO?'),
	(4, '你好我是注册的新用户', 'USER', '123456'),
	(6, '孩子们我又注册了一个号', 'USER', '123456789');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
