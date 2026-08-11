
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
	name VARCHAR(20) UNIQUE
);

DROP TABLE IF EXISTS reimbursements;
CREATE TABLE reimbursements (
    reimbursement_id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(500) NOT NULL,
    status VARCHAR(500) NOT NULL DEFAULT 'PENDING',
    author_id INTEGER NOT NULL,
    resolver_id INTEGER NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(user_id),
    FOREIGN KEY (resolver_id) REFERENCES users(user_id)
);

INSERT INTO departments (name)
VALUES ('IT')

INSERT INTO users (username, password, first_name, last_name, dept_id, role)
VALUES ('kplummer', 'pass123!', 'kyle', 'plummer', 1, 'EMPLOYEE')

INSERT INTO users (username, password, first_name, last_name, dept_id, role)
VALUES ('admin', 'pass123!', 'admin', 'admin', 1, 'ADMIN')

INSERT INTO reimbursements (amount, description, "type", status, author_id, resolver_id) 
VALUES(123.45, 'test', 'FOOD', 'PENDING', 1, 2);

SELECT *
FROM users;

SELECT *
FROM departments;


INSERT INTO departments (name) VALUES ("test");

INSERT INTO users (username, password, first_name, last_name, dept_id, role)
VALUES ('kplummer', 'pass123', 'Kyle', 'Plummer', 1, 'admin');



SELECT id, username, password, first_name, last_name, U.dept_id, ROLE, name AS dept_name FROM users U JOIN departments D ON D.dept_id = U.dept_id WHERE D.name = ?;

SELECT * FROM reimbursements;