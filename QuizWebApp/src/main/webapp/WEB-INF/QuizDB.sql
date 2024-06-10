-- DROP DATABASE IF EXISTS quiz;
CREATE DATABASE quiz;
USE quiz;

SET SQL_MODE='ALLOW_INVALID_DATES';

-- User table
-- user_type  : 0 Admin,  1 Volunteer, 2 Parents, 3 Participant
-- birth_year : Only for participant
-- gender     : Only for participant (1 Male, 0 Female)
CREATE TABLE user (
  user_id     INT AUTO_INCREMENT NOT NULL,
  parent_id   INT         NOT NULL,
  user_type   TINYINT     NOT NULL,
  user_name   VARCHAR(30) NOT NULL,
  password    VARCHAR(32) NOT NULL,
  nickname    VARCHAR(30) NOT NULL,
  birth_year  INT         NOT NULL,
  gender      TINYINT     NOT NULL,
  address     VARCHAR(50) NOT NULL,
  email       VARCHAR(30) NOT NULL,
  phone       VARCHAR(20) NOT NULL,
  token       VARCHAR(50) NOT NULL,
  create_time TIMESTAMP   NOT NULL,
  signin_time TIMESTAMP   NOT NULL,
  PRIMARY KEY(user_id),
  UNIQUE KEY user_name_uniq(user_name)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- User index
CREATE INDEX user_parent_id ON user(parent_id);

-- Quiz main table
CREATE TABLE quiz (
  quiz_id     INT AUTO_INCREMENT NOT NULL,
  quiz_name   VARCHAR(100) NOT NULL,
  user_type   TINYINT      NOT NULL,
  create_time TIMESTAMP    NOT NULL,
  PRIMARY KEY(quiz_id),
  UNIQUE KEY quiz_name_uniq(quiz_name)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Quiz item table
-- We can get the last_insert_id() for current session as below:
-- insert quiz_item values(last_insert_id(), 1, "?", "@", 1);
-- item_row  : 0 is question, for 1 to n are answers
-- item_type : 0 single, 1 multiple, 2 text
CREATE TABLE quiz_item (
  quiz_id      INT     NOT NULL,
  item_id      TINYINT NOT NULL,
  item_row     TINYINT NOT NULL,
  item_content VARCHAR(500) NOT NULL,
  item_type    TINYINT NOT NULL,
  PRIMARY KEY(quiz_id, item_id, item_row),
  CONSTRAINT fk_quiz_item FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Quiz result table
CREATE TABLE quiz_result (
  quiz_id      INT     NOT NULL,
  user_id      INT     NOT NULL,
  item_id      TINYINT NOT NULL,
  item_row     TINYINT NOT NULL,
  answer       VARCHAR(500) NOT NULL,
  answer_time  TIMESTAMP    NOT NULL,
  PRIMARY KEY(quiz_id, user_id, item_id, item_row),
  CONSTRAINT fk_quiz_result FOREIGN KEY (quiz_id, item_id, item_row) REFERENCES quiz_item(quiz_id, item_id, item_row),
  CONSTRAINT fk_quiz_result_user FOREIGN KEY (user_id) REFERENCES user(user_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Event table
CREATE TABLE event (
  event_id     INT AUTO_INCREMENT NOT NULL,
  event_type   TINYINT      NOT NULL,
  title        VARCHAR(100) NOT NULL,
  content      VARCHAR(500) NOT NULL,
  create_time  TIMESTAMP    NOT NULL,
  PRIMARY KEY(event_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Activity table
CREATE TABLE activity (
  activity_id  INT AUTO_INCREMENT NOT NULL,
  user_id      INT          NOT NULL,
  event_id     INT          NOT NULL,
  title        VARCHAR(100) NOT NULL,
  content      VARCHAR(500) NOT NULL,
  create_time  TIMESTAMP    NOT NULL,
  PRIMARY KEY(activity_id),
  CONSTRAINT fk_activity_user  FOREIGN KEY (user_id)  REFERENCES user(user_id),
  CONSTRAINT fk_activity_event FOREIGN KEY (event_id) REFERENCES event(event_id)
) Engine=INNODB DEFAULT CHARSET=UTF8MB4 COLLATE=UTF8MB4_BIN;

-- Insert user (user_id, parent_id, user_type)
insert into user values(1, 0, 0, 'admin', '_TG3ufixa6JDL11AFE3A5w==', 'admin', 1980, 1, '', '', '', '', '2022-01-01', '2022-01-01');

-- delete from quiz_result;
-- delete from quiz_item where quiz_id > 0;
-- delete from quiz where quiz_id > 0;

-- Insert quiz 1 : user_type = 1 for Volunteer
insert into quiz(quiz_id, quiz_name, user_type, create_time)
    values(1, 'Survey for Volunteer', 1, '2022-01-01');

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(1, 1, 0, 'Are you in US?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(1, 1, 1, 'Yes', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(1, 1, 2, 'No', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(1, 2, 0, 'What are your hobbies and interests?', 2);

-- Insert quiz 2 : user_type = 1 for Volunteer
insert into quiz(quiz_id, quiz_name, user_type, create_time)
    values(2, 'Survey of Spanish Media', 1, '2022-01-01');

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 0, 'To what extent is your knowledge on the Spanish media?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 1, '(a) A huge extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 2, '(b) Quite a huge extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 3, '(c) An average extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 4, '(d) Quite a limited extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 5, '(e) A limited extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 1, 6, '(f) Not sure', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 2, 0, 'Through which media, the Spanish or English, do you most frequently retrieve the most updated news?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 2, 1, '(a) Spanish media', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 2, 2, '(b) English media', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 2, 3, '(c) Both', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 2, 4, '(d) Not sure', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 3, 0, 'Through which channel of the Spanish media do you most frequently retrieve the latest news?', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 3, 1, '(a) Mobile Phone', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 3, 2, '(b) Newspapers', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 3, 3, '(c) Television (Newscasts)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 3, 4, '(d) Computer (Internet, webpages, blogs, etc)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 3, 5, '(e) Radio/radio stations (FM 88.3, 93.3, 95.8, 100.3, etc)', 1);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 4, 0, 'Through which channel of the English media do you most frequently retrieve the latest news?', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 4, 1, '(a) Handphone', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 4, 2, '(b) Newspapers (The Straits Times, Today, The Newpaper, etc)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 4, 3, '(c) Television (Newscasts)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 4, 4, '(d) Computer (Internet, webpages, blogs, etc)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 4, 5, '(e) Radio/radio stations (FM 91.3, 98.7, etc)', 1);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 5, 0, 'In your opinion, which topic has the Spanish media been stressing on?', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 5, 1, '(a) Gossipy news', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 5, 2, '(b) Information and technology', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 5, 3, '(c) Violence cases', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 5, 4, '(d) Sports', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(2, 5, 5, '(e) Politics', 1);

-- Insert quiz 3 : user_type = 2 for Parents
insert into quiz(quiz_id, quiz_name, user_type, create_time)
    values(3, 'Questionnaire for Parents', 2, '2022-01-01');

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 1, 0, 'Your child`s diagnosis?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 2, 0, 'When first symptom been found?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 3, 0, 'When was your child diagnosed?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 4, 0, 'What treatment has your child received?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 5, 0, 'Which types of treatment have better effects?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 6, 0, 'What is the approximate annual cost of child treatment?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 7, 0, 'For the annual cost of child treatment, how much do you pay yourselves?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 8, 0, 'Have you heard/received art therapy?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 8, 1, 'Yes', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 8, 2, 'No', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 9, 0, 'Do you think art therapy is useful?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 9, 1, 'Yes', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 9, 2, 'No', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 10, 0, 'In what ways is art therapy useful?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 11, 0, 'What extent are you willing to participate in art therapy research?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 11, 1, 'A huge extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 11, 2, 'Quite a huge extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 11, 3, 'An average extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 11, 4, 'Quite a limited extent', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 11, 5, 'A limited extent', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 12, 0, 'Do you have any suggestions for treatment in future?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 13, 0, 'What’s the most difficult thing you need to handle?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 14, 0, 'What do you need most to help you?', 2);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 15, 0, 'How do you know ‘Purple Sense Non-Profit Organization’?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 15, 1, 'Friends', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 15, 2, 'Website', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 15, 3, 'Facebook', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 15, 4, 'YouTube', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(3, 15, 5, 'Others', 0);

-- Insert quiz 4 : user_type = 3 for Kid Participant
insert into quiz(quiz_id, quiz_name, user_type, create_time)
    values(4, 'Questionnaire for Kid', 3, '2022-01-01');

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 0, 'Do you like art ? (Check all that apply)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 1, 'Drawing', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 2, 'Painting', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 3, 'Music', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 4, 'Drama', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 5, 'Film', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 1, 6, 'Others', 1);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 2, 0, 'Do you want to art with us ?', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 2, 1, 'Yes', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 2, 2, 'No', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 0, 'Do you like sport ? (Check all that apply)', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 1, 'Soccer', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 2, 'Football', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 3, 'Swimming', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 4, 'Basketball', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 5, 'Running', 1);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(4, 3, 6, 'Others', 1);

-- Insert quiz 5 : user_type = 2 for Parent
insert into quiz(quiz_id, quiz_name, user_type, create_time)
    values(5, 'Autism Spectrum Screening Questionnaire (ASSQ)', 2, '2024-05-15');

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 1, 0, 'is old-fashioned or precocious', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 1, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 1, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 1, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 2, 0, 'is regarded as an eccentric-professor by the other children', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 2, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 2, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 2, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 3, 0, 'lives somewhat in a world of his/her own with restricted idiosyncratic intellectual interests', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 3, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 3, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 3, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 4, 0, 'accumulates facts on certain subjects (good rote memory) but does not really understand the meaning', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 4, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 4, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 4, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 5, 0, 'has a literal understanding of ambiguous and metaphorical language', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 5, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 5, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 5, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 6, 0, 'has a deviant style of communication with a formal, fussy, old-fashioned or robot-like language', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 6, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 6, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 6, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 7, 0, 'invents idiosyncratic words and expressions', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 7, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 7, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 7, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 8, 0, 'has a different voice or speech', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 8, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 8, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 8, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 9, 0, 'expresses sounds involuntarily; clears throat, grunts, smacks, cries or screams', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 9, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 9, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 9, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 10, 0, 'is surprisingly good at some things and surprisingly poor at others', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 10, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 10, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 10, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 11, 0, 'uses language freely but fails to make adjustment to fit social contexts or the needs of different listeners', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 11, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 11, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 11, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 12, 0, 'lacks empathy', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 12, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 12, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 12, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 13, 0, 'makes naive and embarrassing remarks', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 13, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 13, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 13, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 14, 0, 'has a deviant style of gaze', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 14, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 14, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 14, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 15, 0, 'wishes to be sociable but fails to make relationships with peers', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 15, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 15, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 15, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 16, 0, 'can be with other children but only on his/her terms', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 16, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 16, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 16, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 17, 0, 'lacks best friend', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 17, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 17, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 17, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 18, 0, 'lacks common sense', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 18, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 18, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 18, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 19, 0, 'is poor at games: no idea of cooperating in a team, scores own-goals', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 19, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 19, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 19, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 20, 0, 'has clumsy, ill coordinated, ungainly, awkward movements or gestures', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 20, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 20, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 20, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 21, 0, 'has involuntary face or body movements', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 21, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 21, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 21, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 22, 0, 'has difficulties in completing simple daily activities because of compulsory repetition of certain actions or thoughts', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 22, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 22, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 22, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 23, 0, 'has special routines: insists on no change', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 23, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 23, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 23, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 24, 0, 'shows idiosyncratic attachment to objects', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 24, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 24, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 24, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 25, 0, 'is bullied by other children', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 25, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 25, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 25, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 26, 0, 'has markedly unusual facial expression', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 26, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 26, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 26, 3, 'Yes', 0);

insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 27, 0, 'has markedly unusual posture', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 27, 1, 'No', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 27, 2, 'Somewhat', 0);
insert into quiz_item(quiz_id, item_id, item_row, item_content, item_type)
    values(5, 27, 3, 'Yes', 0);


-- Insert event
-- event_type : 0 default, 1 music, 2 art class, 3 game
insert into event(event_id, event_type, title, content, create_time)
    values(1, 0, 'default', 'default', '2022-01-01');
insert into event(event_id, event_type, title, content, create_time)
    values(2, 0, 'Happy New Year', 'Happy New Year : 2022', '2022-01-01');
insert into event(event_id, event_type, title, content, create_time)
    values(3, 2, 'Art Class', 'description', '2022-02-15');
insert into event(event_id, event_type, title, content, create_time)
    values(4, 3, 'Angry Bird', 'description', '2022-03-10');
insert into event(event_id, event_type, title, content, create_time)
    values(5, 1, 'Music', 'description', '2022-03-20');

-- Insert activity
insert into activity(activity_id, user_id, event_id, title, content, create_time)
    values(1, 1, 1, 'Drawing', 'description', '2022-01-10');
insert into activity(activity_id, user_id, event_id, title, content, create_time)
    values(2, 1, 1, 'Reading', 'description', '2022-02-05');
insert into activity(activity_id, user_id, event_id, title, content, create_time)
    values(3, 1, 1, 'Drawing', 'description', '2022-03-15');

commit;

