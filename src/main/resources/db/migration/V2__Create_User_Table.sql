CREATE TABLE recipe_user (
    id CHAR(24) PRIMARY KEY,
    email text UNIQUE,
    name text
);