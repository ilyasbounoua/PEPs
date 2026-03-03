-- Run this script in your PostgreSQL database (pgAdmin or terminal)
-- It creates the notification table to store module offline alerts.

CREATE TABLE IF NOT EXISTS peps.notification (
    id SERIAL PRIMARY KEY,
    message VARCHAR(500) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    owner_role VARCHAR(50) NOT NULL
);

-- Note: In PostgreSQL, "user" and "role" are reserved or common keywords,
-- we use "owner_role" to match the logic used in modules and sounds.
