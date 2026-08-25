--Subqueries

SELECT *
FROM users
WHERE dept IN (
	SELECT name
	FROM departments
)


SELECT name 
FROM customers c
WHERE EXISTS (
	SELECT 1 
	FROM orders o 
	WHERE o.customer_id = c.id
	);


--CREATE TABLE departments (
--	id INTEGER PRIMARY KEY AUTOINCREMENT,
--	name VARCHAR(20)
--)
--
--INSERT INTO departments (name) VALUES ("asd")