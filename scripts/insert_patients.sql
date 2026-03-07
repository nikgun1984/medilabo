-- Create patients table if it doesn't already exist and insert sample rows
BEGIN;

CREATE TABLE IF NOT EXISTS patients (
  id SERIAL PRIMARY KEY,
  last_name VARCHAR(255),
  first_name VARCHAR(255),
  date_of_birth DATE,
  gender CHAR(1),
  address TEXT,
  phone VARCHAR(50),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

INSERT INTO patients (last_name, first_name, date_of_birth, gender, address, phone) VALUES
  ('TestNone', 'Test', '1966-12-31', 'F', '1 Brookside St', '100-222-3333'),
  ('TestBorderline', 'Test', '1945-06-24', 'M', '2 High St', '200-333-4444'),
  ('TestInDanger', 'Test', '2004-06-18', 'M', '3 Club Road', '300-444-5555'),
  ('TestEarlyOnset', 'Test', '2002-06-28', 'F', '4 Valley Dr', '400-555-6666');

COMMIT;
