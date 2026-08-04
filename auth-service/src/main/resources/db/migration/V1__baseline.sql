CREATE EXTENSION IF NOT EXISTS citext;

CREATE DOMAIN email_address AS citext
CHECK (
    value ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
);

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

CREATE TABLE users(
id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
created_at DATE,
email email_address NOT NULL,
enabled BOOLEAN NOT NULL,
password TEXT NOT NULL,
role user_role NOT NULL DEFAULT 'USER',
terms_accepted BOOLEAN NOT NULL,
username TEXT NOT NULL,
date_of_birth date NOT NULL,

CONSTRAINT chk_terms_accepted CHECK (terms_accepted = true),
CONSTRAINT chk_username_min_length CHECK (char_length(username) >= 5),
CONSTRAINT chk_username_max_length CHECK (char_length(username) <= 20),
CONSTRAINT chk_username_pattern CHECK (username ~ '^[A-Za-z]{5}.*'),
CONSTRAINT uk_users_email UNIQUE(email),
CONSTRAINT uk_users_username UNIQUE(username)
);