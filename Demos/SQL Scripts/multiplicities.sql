-- "Multiplicity" refers to the different types of relationships between data in multiple tables. You can call
-- these "relationships" as well, this is common, but "relation" specifically refers to tables in SQL.

-- 1-to-1 - UNIQUE Constraint on the foreign key column, and the referenced column (usually a primary key, which is also unique)
-- 1-to-many - foreign key is not unique.
-- many-to-many - there's always an extra table called the "junction table"

CREATE TABLE users (
	user_id INTEGER PRIMARY KEY AUTOINCREMENT,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	age INT
)

CREATE TABLE addresses (
	address_id INTEGER PRIMARY KEY AUTOINCREMENT,
	street_address VARCHAR(200),
	city VARCHAR(200),
	state CHAR(2),
	zip INT
)

CREATE TABLE users_addresses_2 (
	user_id INTEGER REFERENCES users(user_id),
	address_id INTEGER REFERENCES addresses(address_id),
	CONSTRAINT pk_users_addresses PRIMARY KEY (user_id, address_id)
)


CREATE INDEX idx_users_first_name ON users (first_name);
CREATE INDEX idx_users_last_name ON users (last_name);

INSERT INTO users (first_name, last_name, age) VALUES ("Danielle", "Plummer", 41);

SELECT * from users

INSERT INTO addresses (street_address, city, state, zip) VALUES ('21 jump st', 'Another town', 'NY', '12345')

SELECT * FROM addresses

INSERT INTO users_addresses VALUES (1, 1);


SELECT * FROM users_addresses







