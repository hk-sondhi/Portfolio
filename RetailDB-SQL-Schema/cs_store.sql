-- CREATE DATABASE cs_store;
-- USE cs_store;

-- QUESTION 1 
CREATE TABLE Customers(
	birth_day DATE, 
	first_name VARCHAR(20), 
	last_name VARCHAR(20), 
	c_id INT,
		CONSTRAINT PK_Customers
	PRIMARY KEY (c_id)
);
CREATE TABLE Employees(
	birth_day DATE, 
    first_name VARCHAR(20), 
    last_name VARCHAR(20), 
    e_id INT,
		CONSTRAINT PK_Employees
PRIMARY KEY (e_id)
);
CREATE TABLE Transactions(
	e_id INT,
    c_id INT,
    date DATE, 
    t_id INT,
		CONSTRAINT FK_Transactions FOREIGN KEY (e_id)
			REFERENCES Employees (e_id),
		CONSTRAINT FK1_Transactions FOREIGN KEY (c_id)
			REFERENCES Customers (c_id),
		CONSTRAINT PK_Transactions
PRIMARY KEY (t_id)
);
CREATE TABLE Promotion(
	number_to_buy INT,
    how_many_are_free INT,
    type INT,
		CONSTRAINT PK_Promotion
PRIMARY KEY (type)
);
CREATE TABLE Items(
	price_for_each INT, 
    type INT, 
    amount_in_stock INT, 
    name VARCHAR(20),
		CONSTRAINT PK_Items
PRIMARY KEY (name)
);
CREATE TABLE ItemsInTransactions(
	name VARCHAR(20), 
    t_id INT,
    iit_id INT,
		CONSTRAINT FK_ItemsInTransactions FOREIGN KEY (name)
			REFERENCES Items (name),
		CONSTRAINT FK1_ItemsInTransactions FOREIGN KEY (t_id)
			REFERENCES Transactions (t_id),
		CONSTRAINT PK_ItemsInTransactions
PRIMARY KEY (iit_id)
);

-- QUESTION 2 
-- DROP VIEW LouisTransactions;
CREATE VIEW LouisTransactions AS 
SELECT DISTINCT COUNT(t_id) AS number_of_transactions 
FROM Transactions 
WHERE e_id = (SELECT DISTINCT e_id FROM Employees WHERE first_name = "Louis") AND YEAR(DATE) = 2022 AND MONTH(DATE) = 9;
SELECT * FROM LouisTransactions;


-- QUESTION 3 
-- DROP VIEW PeopleInShop;
CREATE VIEW PeopleInShop AS 
SELECT DISTINCT birth_day, first_name, last_name FROM Customers WHERE c_id IN (SELECT c_id FROM transactions WHERE date = "2022-9-28")
UNION 
SELECT DISTINCT birth_day, first_name, last_name FROM Employees WHERE e_id IN (SELECT e_id FROM transactions WHERE date = "2022-9-28")
ORDER BY birth_day;
SELECT * FROM PeopleInShop;


-- QUESTION 4 
-- DROP VIEW ItemsLeft1;
CREATE VIEW ItemsLeft1 AS
SELECT DISTINCT name, type, (amount_in_stock - COUNT(name)) AS amount_left FROM ItemsInTransactions NATURAL JOIN Items
WHERE type IN (SELECT type FROM items WHERE type = "2" OR type = "1")
GROUP BY name
ORDER BY type, name;
SELECT * FROM ItemsLeft1;

-- QUESTION 5
-- DROP VIEW ItemsLeft2;
CREATE VIEW ItemsLeft2 AS 
SELECT DISTINCT i.name, i.type, (amount_in_stock - COUNT(it.name)) AS amount_left FROM Items i LEFT JOIN ItemsInTransactions it ON i.name = it.name 
WHERE i.name IN (SELECT name FROM items WHERE type IN (SELECT type FROM items WHERE type = "3" OR type = "4"))
GROUP BY  i.name, i.type
ORDER BY i.type, i.name; 
SELECT * FROM ItemsLeft2;

-- QUESTION 6 
-- DROP VIEW IITRANKING;
CREATE VIEW IITRANKING AS
-- SELECT it.iit_id, it.t_id, i.type, i.price_for_each, row_number()OVER() AS rnk FROM ItemsInTransactions it INNER JOIN Items i ON it.name = i.name
SELECT it.iit_id, it.t_id, i.type, i.price_for_each, COUNT(it.t_id) AS rnk FROM ItemsInTransactions it INNER JOIN Items i ON it.name = i.name
-- WHERE it.t_id = it.t_id
GROUP BY it.iit_id
ORDER BY it.t_id DESC, i.price_for_each DESC, it.iit_id DESC;
SELECT * FROM IITRANKING;







