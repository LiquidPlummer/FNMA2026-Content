--Two types of function: Aggregate and Scalar

-- Aggregate functions take in X inputs and produce exactly 1 output always.

/* AVG()
 * SUM()
 * COUNT()
 * MIN()
 * MAX()
 */



SELECT AVG(U.id )
FROM users U

SELECT SUM(U.id )
FROM users U

SELECT COUNT(U.id )
FROM users U

SELECT MAX(U.id)
FROM users U

SELECT MIN(U.id)
FROM users U




-- Scalar functions take in X inputs and produce X outputs, 1 to 1 always.
/*
 * POW()
 * ABS()
 * LENGTH()
 * UCASE
 * LCASE
 * LENGTH
 */
SELECT POW(U.id, 2)
FROM users U

SELECT ABS(U.id)
FROM users U

SELECT LENGTH(U.first_name)
FROM users U

SELECT SUBSTRING(U.first_name, 2, 2)
FROM users U



-- User Defined Functions and Stored Procedures
-- Stored Procedure:
DELIMITER // 
CREATE PROCEDURE squareNum(IN num INT, OUT res INT)
BEGIN
	SELECT INTO res POW(num, 2);
END //
DELIMITER ;




--User Defined Function
DELIMITER //
CREATE FUNCTION FullName(first VARCHAR(50), last VARCHAR(50))
RETURNS VARCHAR(101)
DETERMINISTIC
BEGIN
    RETURN CONCAT(last, ', ', first);
END //
DELIMITER ;

SELECT FullName(first_name, last_name) FROM users;


