--liquibase formatted sql

--changeset admin:1 dbms:mysql
CREATE TABLE IF NOT EXISTS `NameDirectory` (
	`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	`firstName` VARCHAR(50) NOT NULL,
	`lastName` VARCHAR(50) NOT NULL,
	`createdTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`)
)
ENGINE=InnoDB;

--changeset admin:1 dbms:postgresql
CREATE TABLE "NameDirectory" (
	id SERIAL NOT NULL,
	"firstName" VARCHAR(50) NOT NULL,
	"lastName" VARCHAR(50) NOT NULL,
	"createdTimestamp" TIMESTAMP(0) WITHOUT TIME ZONE DEFAULT timestamp 'now ()' NOT NULL,
	PRIMARY KEY(id)
)
	WITH (oids = false);

--changeset admin:1 dbms:oracle
CREATE TABLE NameDirectory (
	id NUMBER(10) NOT NULL,
	firstName VARCHAR2(50) NOT NULL,
	lastName VARCHAR2(50) NOT NULL,
	createdTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT id_pk PRIMARY KEY (id)
);
CREATE SEQUENCE NameDirectory_seq;

