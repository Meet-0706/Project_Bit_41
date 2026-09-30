CREATE DATABASE catalog_db;
CREATE DATABASE inventory_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;

\c catalog_db;
CREATE SCHEMA IF NOT EXISTS catalog;

\c inventory_db;
CREATE SCHEMA IF NOT EXISTS inventory;

\c order_db;
CREATE SCHEMA IF NOT EXISTS orders;

\c payment_db;
CREATE SCHEMA IF NOT EXISTS payment;

\c notification_db;
CREATE SCHEMA IF NOT EXISTS notification;
