-- Run this in Redshift Query Editor v2 (or via psql) before running the Java demo.
-- Mirrors "Section 2: Creating the Tables" from the DynamoDB demo, but as live SQL
-- instead of a console wizard -- Redshift doesn't have a table-creation UI.

CREATE TABLE stores (
    store_id   INTEGER      PRIMARY KEY,
    city       VARCHAR(50),
    region     VARCHAR(50)
);

CREATE TABLE products (
    product_id INTEGER      PRIMARY KEY,
    name       VARCHAR(100),
    category   VARCHAR(50),
    price      DECIMAL(10,2)
);

-- Note (same caveat as before): PRIMARY KEY here is a query-planner hint only --
-- Redshift does not enforce uniqueness, unlike a relational DB students already know.

CREATE TABLE sales (
    sale_id    INTEGER      PRIMARY KEY,
    store_id   INTEGER,
    product_id INTEGER,
    sale_date  DATE,
    quantity   INTEGER,
    total      DECIMAL(10,2)
)
DISTKEY(store_id)   -- co-locates a store's sales together for fast store/region joins & aggregates
SORTKEY(sale_date);  -- lets date-range queries skip blocks instead of scanning the whole table
