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
  `Version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `IsLive` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为线上展示的最新版本',
  `ParentModelID` bigint DEFAULT NULL COMMENT '父模型ID，用于版本追溯',
  PRIMARY KEY (`ModelID`),
  KEY `fk_Uploader` (`Uploader`),
  KEY `fk_ParentModel` (`ParentModelID`),
  KEY `idx_IsLive` (`IsLive`),
  KEY `idx_Uploader_IsLive` (`Uploader`,`IsLive`),
  CONSTRAINT `fk_ParentModel` FOREIGN KEY (`ParentModelID`) REFERENCES `model` (`ModelID`) ON DELETE SET NULL,
  CONSTRAINT `fk_Uploader` FOREIGN KEY (`Uploader`) REFERENCES `user` (`UserID`),
  CONSTRAINT `chk_AuditStatus` CHECK ((`AuditStatus` in (_utf8mb4'PENDING',_utf8mb4'APPROVED',_utf8mb4'REJECTED')))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.model 的数据：~6 rows (大约)
DELETE FROM `model`;
INSERT INTO `model` (`ModelID`, `ModelName`, `ThumbnailAddress`, `FileAddress`, `Uploader`, `UploadTime`, `AuditStatus`, `Version`, `IsLive`, `ParentModelID`) VALUES
	(1, '军用虎钳', '/thumbnails/jaw_vice_thumbnail.png', '/models/jaw_vice.glb', 1, '2025-11-07 16:39:50', 'APPROVED', 1, 1, NULL),
	(2, '小方块', '/thumbnails/red_cube.png', '/models/red_cube.glb', 4, '2025-11-07 16:16:44', 'APPROVED', 1, 1, NULL),
	(3, '测试上传新模型', '/thumbnails/f6e6cd38-7479-4013-934d-a8a46811dbe5.png', '/models/227232fc-3b4d-4678-ba01-76a297cf2825.glb', 4, '2025-11-11 19:33:35', 'APPROVED', 1, 1, NULL),
	(4, '你应该考虑驳回该模型', '/thumbnails/a0e47b26-afd4-4c3a-b192-78d2d3512628.png', '/models/3fc83778-083b-4e5d-bbec-f785b8e665b7.glb', 4, '2025-11-11 19:33:19', 'REJECTED', 1, 0, NULL),
	(9, '超级隐藏款！！！', '/thumbnails/16bae83d-1de4-4036-9f64-4665c5347d24.png', '/models/8387e891-734b-47c9-bb8e-53fd6eb47807.glb', 8, '2025-11-11 19:33:32', 'APPROVED', 1, 1, NULL),
	(10, '测试一下缩略图压缩', '/thumbnails/8d3442c5-90fd-4099-aafe-b7bdc1f3acb5.png', '/models/c72e8bd3-2f51-46ab-b91b-029f8c20e60c.glb', 4, '2025-11-11 15:44:18', 'PENDING', 1, 0, NULL);

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

-- 正在导出表  ovss_db.modeltag 的数据：~11 rows (大约)
DELETE FROM `modeltag`;
INSERT INTO `modeltag` (`ModelID`, `TagID`) VALUES
	(1, 1),
	(2, 2),
	(2, 3),
	(3, 3),
	(10, 3),
	(3, 4),
	(3, 5),
	(3, 6),
	(9, 7),
	(9, 8),
	(9, 9);

-- 导出  表 ovss_db.tag 结构
DROP TABLE IF EXISTS `tag`;
CREATE TABLE IF NOT EXISTS `tag` (
  `TagID` bigint NOT NULL AUTO_INCREMENT COMMENT '标签唯一主键',
  `TagName` varchar(50) NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`TagID`),
  UNIQUE KEY `TagName` (`TagName`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.tag 的数据：~9 rows (大约)
DELETE FROM `tag`;
INSERT INTO `tag` (`TagID`, `TagName`) VALUES
	(6, '123'),
	(2, 'Blender'),
	(5, '中文逗，号'),
	(7, '开发者'),
	(9, '彩蛋'),
	(4, '新标签'),
	(1, '机械'),
	(3, '测试用'),
	(8, '隐藏');

-- 导出  表 ovss_db.user 结构
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `UserID` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一主键',
  `UserName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `UserAuthority` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '权限等级',
  `Password` varchar(255) NOT NULL COMMENT '密码',
  PRIMARY KEY (`UserID`),
  CONSTRAINT `chk_UserAuthority` CHECK ((`UserAuthority` in (_utf8mb4'USER',_utf8mb4'ADMIN')))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  ovss_db.user 的数据：~4 rows (大约)
DELETE FROM `user`;
INSERT INTO `user` (`UserID`, `UserName`, `UserAuthority`, `Password`) VALUES
	(1, 'somebody', 'USER', '123456'),
	(2, 'hello', 'USER', 'HELLO?'),
	(4, '你好我是注册的新用户', 'USER', '123456'),
	(6, '孩子们我又注册了一个号', 'USER', '123456789'),
	(7, '孩子们我是管理员', 'ADMIN', 'admin'),
	(8, 'Latic7', 'USER', '123456');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
