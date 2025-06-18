# RetailDB-SQL-Schema
#University - 2nd year

## Overview
This project defines a relational database schema for a retail store system, along with a set of SQL queries and views designed to extract meaningful insights. It models key entities like customers, employees, transactions, items, and promotions.

## Features
- Normalised schema with 6 interconnected tables
- Foreign key constraints to maintain referential integrity
- Views that provide:
  - Number of transactions handled by a specific employee
  - People present in the store on a specific day
  - Items remaining in stock by type
  - Ranking of items in transactions

## Schema Components
- `Customers`: Stores customer details
- `Employees`: Stores employee details
- `Transactions`: Records all customer-employee interactions
- `Items`: Contains product info including stock levels and type
- `Promotion`: Describes promotional offers
- `ItemsInTransactions`: Many-to-many mapping of items to transactions

## How to Use
1. Create the database and run the `cs_store.sql` script in any MySQL-compatible database environment.
2. Explore the provided views such as:
   - `LouisTransactions`
   - `PeopleInShop`
   - `ItemsLeft1`, `ItemsLeft2`
   - `IITRANKING`
3. Modify queries or views to suit different business questions or reporting needs.

## Requirements
- MySQL / MariaDB / SQLite (or any SQL-compatible RDBMS)
- Basic SQL knowledge

## Author
hk-sondhi

