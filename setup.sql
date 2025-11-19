DROP DATABASE IF EXISTS Abstracts;
CREATE DATABASE Abstracts;
USE Abstracts;

-- 1. Create Independent Tables (No Foreign Keys) first

CREATE TABLE `account` (
  `account_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45),
  `password` varchar(50),
  PRIMARY KEY (`account_id`)
);

CREATE TABLE `college` (
  `college_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45),
  PRIMARY KEY (`college_id`)
);

CREATE TABLE `interests` (
  `interest_id` int NOT NULL AUTO_INCREMENT,
  `keyword` varchar(25),
  PRIMARY KEY (`interest_id`)
);

CREATE TABLE `abstract` (
  `abs_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100),
  `abstract` varchar(800),
  `authors` varchar(40),
  PRIMARY KEY (`abs_id`)
);

-- 2. Create Dependent Tables (Tables with Foreign Keys)

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
  `name` varchar(45),
  `college_id` int,
  PRIMARY KEY (`major_id`),
  FOREIGN KEY (`college_id`) REFERENCES `College`(`college_id`)
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
  FOREIGN KEY (`college_id`) REFERENCES `College`(`college_id`)
);

CREATE TABLE `student` (
  `stu_id` int NOT NULL AUTO_INCREMENT,
  `fname` varchar(45),
  `lname` varchar(45),
  `email` varchar(50),
  `program` varchar(100),
  `major_id` int,
  PRIMARY KEY (`stu_id`),
  FOREIGN KEY (`major_id`) REFERENCES `Major`(`major_id`)
);

-- 3. Create Junction Tables (Many-to-Many relationships)

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