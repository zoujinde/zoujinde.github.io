-- DROP DATABASE IF EXISTS quzi;
CREATE DATABASE quzi;
USE quzi;

-- User table
CREATE TABLE user (
  user_id     BIGINT AUTO_INCREMENT NOT NULL,
  user_name   VARCHAR(30) NOT NULL,
  password    VARCHAR(20) NOT NULL,
  email       VARCHAR(30) NOT NULL,
  phone       VARCHAR(20) NOT NULL,
  address     VARCHAR(50) NOT NULL,
  token       VARCHAR(50) NOT NULL,
  create_time DATETIME    NOT NULL,
  PRIMARY KEY(user_id),
  UNIQUE KEY user_name_uniq(user_name)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4;

-- Quiz main table
CREATE TABLE quiz (
  quiz_id     BIGINT AUTO_INCREMENT NOT NULL,
  quiz_name   VARCHAR(50) NOT NULL,
  create_time DATETIME    NOT NULL,
  PRIMARY KEY(quiz_id),
  UNIQUE KEY quiz_name_uniq(quiz_name)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4;

-- Quiz item table
-- We can get the last_insert_id() for current session as below:
-- insert quiz_item values(last_insert_id(), 1, "?", "@", 1);
CREATE TABLE quiz_item (
  quiz_id      BIGINT  NOT NULL,
  item_id      TINYINT NOT NULL,
  item_content VARCHAR(300) NOT NULL,
  item_answer  VARCHAR(300) NOT NULL,
  multi_select BIT          NOT NULL,
  PRIMARY KEY(quiz_id, item_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4;

-- Quiz result table
CREATE TABLE quiz_result (
  quiz_id      BIGINT  NOT NULL,
  item_id      TINYINT NOT NULL,
  user_id      BIGINT  NOT NULL,
  answer       VARCHAR(10) NOT NULL,
  answer_time  DATETIME    NOT NULL,
  PRIMARY KEY(quiz_id, item_id, user_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4;

