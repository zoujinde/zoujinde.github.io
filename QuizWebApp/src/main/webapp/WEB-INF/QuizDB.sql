-- DROP DATABASE IF EXISTS quiz;
CREATE DATABASE quiz;
USE quiz;

-- User table
-- user_type  : 0 Admin,  1 Volunteer, 2 Parents, 3 Participant
-- birth_year : Only for participant
-- gender     : Only for participant (1 Male, 0 Female)
CREATE TABLE user (
  user_id     INT AUTO_INCREMENT NOT NULL,
  parent_id   INT         NOT NULL,
  user_type   INT         NOT NULL,
  user_name   VARCHAR(30) NOT NULL,
  password    VARCHAR(32) NOT NULL,
  nickname    VARCHAR(20) NOT NULL,
  birth_year  INT         NOT NULL,
  gender      INT         NOT NULL,
  address     VARCHAR(50) NOT NULL,
  email       VARCHAR(30) NOT NULL,
  phone       VARCHAR(20) NOT NULL,
  token       VARCHAR(50) NOT NULL,
  create_time TIMESTAMP   NOT NULL,
  signin_time TIMESTAMP   NOT NULL,
  PRIMARY KEY(user_id),
  UNIQUE KEY user_name_uniq(user_name)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Quiz main table
CREATE TABLE quiz (
  quiz_id     INT AUTO_INCREMENT NOT NULL,
  quiz_name   VARCHAR(50) NOT NULL,
  create_time TIMESTAMP   NOT NULL,
  PRIMARY KEY(quiz_id),
  UNIQUE KEY quiz_name_uniq(quiz_name)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Quiz item table
-- We can get the last_insert_id() for current session as below:
-- insert quiz_item values(last_insert_id(), 1, "?", "@", 1);
CREATE TABLE quiz_item (
  quiz_id      INT     NOT NULL,
  item_id      TINYINT NOT NULL,
  item_content VARCHAR(300) NOT NULL,
  item_answer  VARCHAR(300) NOT NULL,
  multi_select BIT          NOT NULL,
  PRIMARY KEY(quiz_id, item_id),
  CONSTRAINT fk_quiz_item FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Quiz result table
CREATE TABLE quiz_result (
  quiz_id      INT     NOT NULL,
  user_id      INT     NOT NULL,
  item_id      TINYINT NOT NULL,
  answer       VARCHAR(10) NOT NULL,
  answer_time  TIMESTAMP   NOT NULL,
  PRIMARY KEY(quiz_id, user_id, item_id),
  CONSTRAINT fk_quiz_result FOREIGN KEY (quiz_id, item_id) REFERENCES quiz_item(quiz_id, item_id),
  CONSTRAINT fk_quiz_result_user FOREIGN KEY (user_id) REFERENCES user(user_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Event table
CREATE TABLE event (
  event_id     INT AUTO_INCREMENT NOT NULL,
  event_type   INT          NOT NULL,
  title        VARCHAR(50)  NOT NULL,
  content      VARCHAR(200) NOT NULL,
  create_time  TIMESTAMP    NOT NULL,
  PRIMARY KEY(event_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Activity table
CREATE TABLE activity (
  activity_id  INT AUTO_INCREMENT NOT NULL,
  user_id      INT          NOT NULL,
  event_id     INT          NOT NULL,
  title        VARCHAR(50)  NOT NULL,
  content      VARCHAR(200) NOT NULL,
  create_time  TIMESTAMP    NOT NULL,
  PRIMARY KEY(activity_id),
  CONSTRAINT fk_activity_user  FOREIGN KEY (user_id)  REFERENCES user(user_id),
  CONSTRAINT fk_activity_event FOREIGN KEY (event_id) REFERENCES event(event_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Insert user (user_id, parent_id, user_type)
insert into user values(1, 0, 0, 'admin', '_TG3ufixa6JDL11AFE3A5w==', 'admin', 1980, 1, '', '', '', '', '2022-01-01', '2022-01-01');

-- Insert quiz 1
insert into quiz values(1, 'Quiz 2022', '2022-01-01');
insert into quiz_item values(1, 1, 'Question 1 : Are you in US?', '(a) Yes # (b) No', 0);
insert into quiz_result values(1, 1, 1, '0', '2022-01-01');

-- Insert quiz 2
insert into quiz values(2, 'Survey of Spanish Media', '2022-01-01');
insert into quiz_item values(2, 1, 'To what extent is your knowledge on the Spanish media?',
       '(a) A huge extent # (b) Quite a huge extent # (c) An average extent # (d) Quite a limited extent # (e) A limited extent # (f) Not sure',
       0);
insert into quiz_item values(2, 2, 'Through which media, the Spanish or English, do you most frequently retrieve the most updated news?',
       '(a) Spanish media # (b) English media # (c) Both # (d) Not sure',
       0);
insert into quiz_item values(2, 3, 'Through which channel of the Spanish media do you most frequently retrieve the latest news?',
       '(a) Mobile Phone # (b) Newspapers # (c) Television (Newscasts) # (d) Computer (Internet, webpages, blogs, etc) # (e) Radio/radio stations (FM 88.3, 93.3, 95.8, 100.3, etc)',
       1);
insert into quiz_item values(2, 4, 'Through which channel of the English media do you most frequently retrieve the latest news?',
       '(a) Handphone # (b) Newspapers (The Straits Times, Today, The Newpaper, etc) # (c) Television (Newscasts) # (d) Computer (Internet, webpages, blogs, etc) # (e) Radio/radio stations (FM 91.3, 98.7, etc)',
       1);
insert into quiz_item values(2, 5, 'In your opinion, which topic has the Spanish media been stressing on?',
       '(a) Gossipy news # (b) Information and technology # (c) Violence cases # (d) Sports # (e) Politics',
       1);

-- Insert event (event_id, event_type, title, content, create_time)
-- event_type : 0 default, 1 music, 2 art class, 3 game
insert into event values(1, 0, 'default', 'default', '2022-01-01');
insert into event values(2, 0, 'Happy New Year', 'Happy New Year : 2022', '2022-01-01');
insert into event values(3, 2, 'Art Class', 'description', '2022-02-15');
insert into event values(4, 3, 'Angry Bird', 'description', '2022-03-10');
insert into event values(5, 1, 'Music', 'description', '2022-03-20');

-- Insert activity (activity_id, user_id, event_id)
insert into activity values(1, 1, 1, 'Drawing', 'description', '2022-01-10');
insert into activity values(2, 1, 1, 'Reading', 'description', '2022-02-05');
insert into activity values(3, 1, 1, 'Drawing', 'description', '2022-03-15');

commit;

