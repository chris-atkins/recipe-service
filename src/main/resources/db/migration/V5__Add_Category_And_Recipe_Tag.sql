ALTER TABLE recipe ADD COLUMN category text;

CREATE TABLE recipe_tag (
    recipe_id CHAR(24) NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    tag       text     NOT NULL,
    PRIMARY KEY (recipe_id, tag)
);

CREATE INDEX recipe_tag_tag_index ON recipe_tag (tag);
