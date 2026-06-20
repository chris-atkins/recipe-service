CREATE TABLE recipe_rating (
    recipe_id CHAR(24) NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    user_id   CHAR(24) NOT NULL,
    rating    integer  NOT NULL CHECK (rating BETWEEN 1 AND 5),
    PRIMARY KEY (recipe_id, user_id)
);
