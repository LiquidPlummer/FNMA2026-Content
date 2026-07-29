/* Constraints are added to tables to enforce certain rules about the data. 
 * PRIMARY KEY and FOREIGN KEY are two constraints, as well as:
 * UNIQUE
 * NOT NULL
 * DEFAULT
 * CHECK
 * and more.
 */


DROP TABLE constraints

CREATE TABLE "constraints" (
	id PRIMARY KEY,
	uniqueness INT UNIQUE,
	notnullness INT NOT NULL,
	default_values INT DEFAULT(66),
	checked INT CHECK (checked >= 1)--must be positive
)


INSERT INTO "constraints" (uniqueness, notnullness, checked) VALUES (1, 2, 3)