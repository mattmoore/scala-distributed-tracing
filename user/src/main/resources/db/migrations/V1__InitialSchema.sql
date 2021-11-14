CREATE TABLE users (
    id uuid PRIMARY KEY,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    address VARCHAR NULL
);
