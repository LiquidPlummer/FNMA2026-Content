--Triggers
CREATE TABLE employee_log (id INTEGER PRIMARY KEY, action TEXT, employee_id INTEGER);

CREATE TABLE employees(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	username VARCHAR(20),
	password VARCHAR(20)
)

CREATE TABLE employee_audit_log (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	old_username VARCHAR(20),
	new_username VARCHAR(20)
)

CREATE TRIGGER log_new_employee
AFTER INSERT ON employees
BEGIN
    INSERT INTO employee_log (action, employee_id) VALUES ('INSERT', NEW.id);
END;


CREATE TRIGGER audit_changes
AFTER UPDATE ON employees
BEGIN
    INSERT INTO employee_audit_log (old_username, new_username) VALUES (OLD.username, NEW.username);
END;


INSERT INTO employees (username, password) VALUES ('kplummer', 'pass');

SELECT * FROM employee_log;
SELECT * FROM employee_audit_log;

UPDATE employees
SET username = "kplummer123"
WHERE username = "kplummer"