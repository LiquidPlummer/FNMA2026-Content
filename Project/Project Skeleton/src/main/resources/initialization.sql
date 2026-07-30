
DROP TABLE IF EXISTS users;
CREATE TABLE users (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	username VARCHAR(20) UNIQUE CHECK(LENGTH(username) >= 4),
	password VARCHAR(20),
	first_name VARCHAR(20),
	last_name VARCHAR(40),
	dept_id INT REFERENCES departments(dept_id),
	"role" VARCHAR(20)
);

DROP TABLE IF EXISTS departments;
CREATE TABLE departments (
	dept_id INTEGER PRIMARY KEY AUTOINCREMENT,
	name VARCHAR(20)
);

SELECT *
FROM users;

SELECT *
FROM departments;


INSERT INTO departments (name) VALUES ("test");

INSERT INTO users (username, password, first_name, last_name, dept_id, role)
VALUES ('kplummer', 'pass123', 'Kyle', 'Plummer', 1, 'admin');