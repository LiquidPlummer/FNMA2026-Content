/* JOINS 
 * inner join
 * left join
 * right
 * full
 * cross
 * natural
 * Not a thing any more: outer - this is just "FULL JOIN" now.
 */
--
--INNER OUTER JOIN CROSS	NATURAL
--RIGHT	LEFT	FULL	

--I want every address for a specific user: LEFT JOIN where the users table is on the LEFT
SELECT *
FROM users U
LEFT JOIN users_addresses UA ON UA.user_id = U.user_id 
LEFT JOIN addresses A ON A.address_id = UA.address_id

SELECT *
FROM users U
FULL JOIN users_addresses UA ON UA.user_id = U.user_id
FULL JOIN addresses A ON A.address_id = UA.address_id

-- Cross join is multiplication of sets - the cross product.
SELECT *
FROM users U
CROSS JOIN addresses A


-- Natural Join example:
SELECT *
FROM users U
NATURAL JOIN users_addresses UA
NATURAL JOIN addresses A


--
SELECT *
FROM users U
LEFT JOIN users_addresses UA ON UA.user_id = U.user_id 
LEFT JOIN addresses A ON A.address_id = UA.address_id
WHERE U.first_name = "Kyle"
