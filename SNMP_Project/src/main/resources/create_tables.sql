-- Create person table
CREATE TABLE IF NOT EXISTS person (
    id SERIAL PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    phoneNumber INTEGER NOT NULL,
    nationalID VARCHAR(20) NOT NULL UNIQUE,
    DOB DATE NOT NULL,
    password VARCHAR(64) NOT NULL, -- SHA-256 hash is 64 characters in base64
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on nationalID for faster lookups
CREATE INDEX IF NOT EXISTS idx_person_national_id ON person(nationalID);

-- Create index on name fields for faster searches
CREATE INDEX IF NOT EXISTS idx_person_names ON person(firstName, lastName);

-- Add a trigger to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_person_updated_at
    BEFORE UPDATE ON person
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert a test user (password: test123)
INSERT INTO person (firstName, lastName, phoneNumber, nationalID, DOB, password)
VALUES (
    'Test',
    'User',
    1234567890,
    '12345678901234567890',
    '2000-01-01',
    'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=' -- This is the SHA-256 hash of 'test123'
) ON CONFLICT (nationalID) DO NOTHING; 