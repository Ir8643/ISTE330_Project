/*
Group 3
ISTE 330 - Database Management Systems
RIT Research Database System
Team Members:
  - Innocenzio Rizzuto
  - Sanjay Charitesh Makam
  - Joseph McEnroe
  - Mohamed Abdullah Najumudeen
  - Jake Paczkowski
  - Muzammilkhan Pathan
*/
DROP DATABASE IF EXISTS Abstracts;
CREATE DATABASE Abstracts;
USE Abstracts;

-- ==========================================
-- 1. TABLE CREATION
-- ==========================================

CREATE TABLE `account` (
  `account_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45),
  `password` varchar(64), -- Fits SHA-256
  PRIMARY KEY (`account_id`)
);

CREATE TABLE `college` (
  `college_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100), -- FIXED: Increased from 45 to 100 to fit "Golisano..."
  PRIMARY KEY (`college_id`)
);

CREATE TABLE `interests` (
  `interest_id` int NOT NULL AUTO_INCREMENT,
  `keyword` varchar(50), -- Increased slightly to be safe
  PRIMARY KEY (`interest_id`)
);

CREATE TABLE `abstract` (
  `abs_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100),
  `abstract` varchar(800),
  `authors` varchar(100), -- Increased from 40 to 100 in case of many authors
  PRIMARY KEY (`abs_id`)
);

-- Dependent Tables

CREATE TABLE `guest` (
  `guest_id` int NOT NULL AUTO_INCREMENT,
  `fname` varchar(45),
  `lname` varchar(45),
  `email` varchar(50),
  `account_id` int,
  PRIMARY KEY (`guest_id`),
  FOREIGN KEY (`account_id`) REFERENCES `account`(`account_id`)
);

CREATE TABLE `major` (
  `major_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100),
  `college_id` int,
  PRIMARY KEY (`major_id`),
  FOREIGN KEY (`college_id`) REFERENCES `college`(`college_id`)
);

CREATE TABLE `faculty` (
  `fac_id` int NOT NULL AUTO_INCREMENT,
  `fname` varchar(45),
  `lname` varchar(45),
  `building_no` int,
  `officer_no` int, 
  `email` varchar(45),
  `account_id` int,
  `college_id` int, 
  PRIMARY KEY (`fac_id`),
  FOREIGN KEY (`account_id`) REFERENCES `account`(`account_id`),
  FOREIGN KEY (`college_id`) REFERENCES `college`(`college_id`)
);

CREATE TABLE `student` (
  `stu_id` int NOT NULL AUTO_INCREMENT,
  `fname` varchar(45),
  `lname` varchar(45),
  `email` varchar(50),
  `program` varchar(100),
  `major_id` int,
  `account_id` int,
  PRIMARY KEY (`stu_id`),
  FOREIGN KEY (`major_id`) REFERENCES `major`(`major_id`),
  FOREIGN KEY (`account_id`) REFERENCES `account`(`account_id`)
);

-- Junction Tables

CREATE TABLE `faculty_interest` (
  `fac_id` int NOT NULL,
  `interest_id` int NOT NULL,
  PRIMARY KEY (`fac_id`, `interest_id`),
  FOREIGN KEY (`fac_id`) REFERENCES `faculty`(`fac_id`),
  FOREIGN KEY (`interest_id`) REFERENCES `interests`(`interest_id`)
);

CREATE TABLE `faculty_abstracts` (
  `prof_id` int NOT NULL,
  `abs_id` int NOT NULL,
  PRIMARY KEY (`prof_id`, `abs_id`),
  FOREIGN KEY (`prof_id`) REFERENCES `faculty`(`fac_id`),
  FOREIGN KEY (`abs_id`) REFERENCES `abstract`(`abs_id`)
);

CREATE TABLE `student_interests` (
  `stu_id` int NOT NULL,
  `interest_id` int NOT NULL,
  PRIMARY KEY (`stu_id`, `interest_id`),
  FOREIGN KEY (`stu_id`) REFERENCES `student`(`stu_id`),
  FOREIGN KEY (`interest_id`) REFERENCES `interests`(`interest_id`)
);

-- ==========================================
-- 2. DATA INSERTION
-- ==========================================

-- Insert Accounts
INSERT INTO `account` (`username`, `password`) VALUES 
('jhabermas', SHA2('enigma123', 256)),
('bhartpence', SHA2('radium456', 256)),
('szilora', SHA2('algo789', 256)),
('guest_jane', SHA2('visitor1', 256)),
('guest_john', SHA2('visitor2', 256)),
('irizzuto', SHA2('rabbit_hole', 256)),
('scharitesh', SHA2('can_we_fix_it', 256));

-- Insert Colleges
INSERT INTO `college` (`name`) VALUES 
('Golisano College of Computing and Information Sciences'), -- This is 54 chars long
('College of Science'),
('College of Liberal Arts');

-- Insert Interests
INSERT INTO `interests` (`keyword`) VALUES 
('Artificial Intelligence'),
('Quantum Physics'),
('Algorithm Design'),
('Renaissance Art'),
('Nuclear Chemistry');

-- Insert Abstracts
INSERT INTO `abstract` (`title`, `abstract`, `authors`) VALUES 
('Computing Machinery and Intelligence', 'This paper investigates the question: "Can machines think?"', 'B. Hartpence'),
('Radioactive Substances', 'A detailed study of radiation phenomena.', 'J. Habermas, S. Zilora'),
('The Art of Computer Programming', 'A comprehensive overview of fundamental algorithms.', 'S. Zilora');

-- Insert Majors
INSERT INTO `major` (`name`, `college_id`) VALUES 
('Computer Science', 1),
('Software Engineering', 1),
('Physics', 2),
('History', 3);

-- Insert Guests
INSERT INTO `guest` (`fname`, `lname`, `email`, `account_id`) VALUES 
('Jane', 'Doe', 'jane.doe@gmail.com', 4),
('John', 'Smith', 'john.smith@yahoo.com', 5);

-- Insert Faculty
INSERT INTO `faculty` (`fname`, `lname`, `building_no`, `officer_no`, `email`, `account_id`, `college_id`) VALUES 
('Jim', 'Habermas', 70, 101, 'jhabermas@rit.edu', 1, 1),
('Bruce', 'Hartpence', 45, 205, 'bhartpence@rit.edu', 2, 2),
('Steve', 'Zilora', 70, 303, 'szilora@rit.edu', 3, 1);

-- Insert Students
INSERT INTO `student` (`fname`, `lname`, `email`, `program`, `major_id`, `account_id`) VALUES 
('Innocenzio', 'Rizzuto', 'irizzuto@rit.edu', 'Undergraduate', 1, 6), 
('Sanjay', 'Charitesh', 'scharitesh@rit.edu', 'Undergraduate', 2, 7);   

-- Link Faculty to Interests
INSERT INTO `faculty_interest` (`fac_id`, `interest_id`) VALUES 
(1, 1), (1, 3), (2, 5), (2, 2), (3, 3);

-- Link Faculty to Abstracts
INSERT INTO `faculty_abstracts` (`prof_id`, `abs_id`) VALUES 
(1, 1), (2, 2), (3, 3);

-- Link Students to Interests
INSERT INTO `student_interests` (`stu_id`, `interest_id`) VALUES 
(1, 1), (1, 3), (2, 3);