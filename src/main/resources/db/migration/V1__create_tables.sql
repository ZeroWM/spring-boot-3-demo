CREATE TABLE `users`
(
    `username` varchar(255) NOT NULL,
    `email`    varchar(255) DEFAULT NULL,
    `phone`    varchar(32)  DEFAULT NULL,
    PRIMARY KEY (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;