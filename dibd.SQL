/*
Created: 3/8/2016
Modified: 3/9/2016
Model: Logical model
Database: PostgreSQL 9.4
*/

-- Create tables section -------------------------------------------------

-- Table article

CREATE TABLE article(
 id Int NOT NULL,
 thread_id Int,
 message_id Character varying(256) NOT NULL,
 message_id_host Character varying(128) NOT NULL,
 hash int,
 a_name Character varying(256),
 subject Character varying(256),
 message Text,
 post_time Bigint NOT NULL,
 path_header Character varying(1000),
 UNIQUE (message_id)
)
;

-- Create indexes for table article

CREATE INDEX IX_Relationship2 ON article (thread_id)
;

-- Add keys for table article

ALTER TABLE article ADD CONSTRAINT Unique_Identifier1 PRIMARY KEY (id)
;

-- Table thread

CREATE TABLE thread(
 thread_id Int NOT NULL,
 group_id Int NOT NULL,
 last_post_time Bigint NOT NULL
)
;

-- Add keys for table thread

ALTER TABLE thread ADD CONSTRAINT Unique_Identifier2 PRIMARY KEY (thread_id)
;

-- Table attachment

CREATE TABLE attachment(
 article_id Int NOT NULL,
 file_path Character varying(256) NOT NULL,
 media_type Character varying(256) NOT NULL
)
;

-- Add keys for table attachment

ALTER TABLE attachment ADD CONSTRAINT Unique_Identifier3 PRIMARY KEY (article_id)
;

-- Create relationships section ------------------------------------------------- 

ALTER TABLE thread ADD CONSTRAINT article_thread FOREIGN KEY (thread_id) REFERENCES article (id) ON DELETE NO ACTION ON UPDATE CASCADE
;

ALTER TABLE article ADD CONSTRAINT thread_article_replays FOREIGN KEY (thread_id) REFERENCES thread (thread_id) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE attachment ADD CONSTRAINT article_attachment FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE article OWNER TO sonews
;
ALTER TABLE thread OWNER TO sonews
;
ALTER TABLE attachment OWNER TO sonews
;
