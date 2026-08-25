--views
--INSERT INTO departments (name) VALUES ("test");
--
--INSERT INTO users (username, password, first_name, last_name, dept_id, role)
--VALUES ('kplummer', 'pass123', 'Kyle', 'Plummer', 1, 'admin');


CREATE VIEW employees_depts AS
SELECT * FROM users U JOIN departments D ON D.dept_id = U.dept_id;


SELECT * FROM employees_depts







