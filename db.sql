CREATE DATABASE IF NOT EXISTS garage_db;
USE garage_db;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS parking_spots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL UNIQUE,
  x INT,
  y INT,
  zone_load INT,
  occupied BIT,
  near_pillar BIT,
  near_elevator BIT
);

CREATE TABLE IF NOT EXISTS map_elements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(30) NOT NULL,
  x INT NOT NULL,
  y INT NOT NULL,
  width INT,
  height INT,
  label VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS parking_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plate_number VARCHAR(30),
  spot_id BIGINT,
  entry_time DATETIME,
  exit_time DATETIME,
  fee DECIMAL(10,2),
  payment_status VARCHAR(50),
  payment_intent_id VARCHAR(100),
  active BIT,
  CONSTRAINT fk_session_spot FOREIGN KEY (spot_id) REFERENCES parking_spots(id)
);
CREATE INDEX idx_parking_sessions_exit_time ON parking_sessions(exit_time);
CREATE INDEX idx_parking_sessions_payment_intent ON parking_sessions(payment_intent_id);

CREATE TABLE IF NOT EXISTS blacklist_vehicles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plate_number VARCHAR(30) UNIQUE,
  reason VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS alerts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(50),
  message VARCHAR(500),
  plate_number VARCHAR(30),
  resolved BIT,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100),
  action VARCHAR(100),
  method VARCHAR(200),
  created_at DATETIME
);
