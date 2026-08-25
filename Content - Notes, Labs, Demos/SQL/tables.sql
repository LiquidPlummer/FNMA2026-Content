--We can use this to tell DBeaver to turn the setting on for this script. This is a preprocess command.
PRAGMA foreign_keys = ON;

-- create two tables, add columns, and create a reference, a foreign key
CREATE TABLE persons (
	--	What stuff is in here? What are constraints?
	-- the table columns: name
	-- type(other stuff too)
	-- unique, not null, primary key, foreign key, check, range, validations...
	id INT PRIMARY KEY, --PK implies UNIQUE and NOT NULL and also gurantees an INDEX
	first_name VARCHAR(50),
	last_name VARCHAR(50)
)

CREATE TABLE addresses (
	id INT PRIMARY KEY,
	number INT,
	street VARCHAR(50),
	city VARCHAR(50),
	zip INT,
	person_id INT,
	CONSTRAINT fk_addresses_persons FOREIGN KEY (person_id) REFERENCES persons (id)
)

INSERT INTO persons (id, first_name, last_name) VALUES (1, "Kyle", "Plummer");
INSERT INTO addresses (id, number, street, city, zip, person_id) VALUES (99999, 21, "Jump st", "Scehenectady", "11111", 1);


DELETE FROM
persons
WHERE id = 1

select * FROM persons


SELECT * FROM addresses A
FULL JOIN persons P ON A.person_id = p.id;

-- The three normal forms: 1st, 2nd, 3rd.... the key the whole key and nothing but the key
-- 1NF: have a primary key - it should be atomic. All columns should be "atomic"
-- 2NF: all data in the table is not partially dependent on the key - all the data in the table should be 
-- related, and anything that isn't should go in another table.
-- 3NF: No transiotive partial dependencies 