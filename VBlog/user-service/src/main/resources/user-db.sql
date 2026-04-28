CREATE DATABASE IF NOT EXISTS `user_db` DEFAULT CHARACTER SET utf8;
USE `user_db`;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `roles_user`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `enabled` tinyint(1) DEFAULT '1',
  `email` varchar(64) DEFAULT NULL,
  `userface` varchar(255) DEFAULT NULL,
  `regTime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_roles_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `roles_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rid` int(11) DEFAULT '2',
  `uid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_roles_user_rid` (`rid`),
  KEY `idx_roles_user_uid` (`uid`),
  CONSTRAINT `fk_roles_user_role` FOREIGN KEY (`rid`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_roles_user_user` FOREIGN KEY (`uid`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `roles` (`id`, `name`) VALUES (1, 'ADMIN'), (2, 'USER');

-- Default password is 123, encoded with the legacy VBlog MD5 password encoder.
INSERT INTO `user` (`id`, `username`, `nickname`, `password`, `enabled`, `email`, `regTime`)
VALUES (1, 'admin', 'admin', '202cb962ac59075b964b07152d234b70', 1, 'admin@vblog.local', NOW());

INSERT INTO `roles_user` (`rid`, `uid`) VALUES (1, 1), (2, 1);

SET FOREIGN_KEY_CHECKS=1;
