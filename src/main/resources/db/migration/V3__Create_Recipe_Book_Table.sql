CREATE TABLE recipe_book (
    id SERIAL PRIMARY KEY,
    user_id CHAR(24) NOT NULL,
    recipe_id CHAR(24) NOT NULL,

    UNIQUE(user_id, recipe_id)
);

