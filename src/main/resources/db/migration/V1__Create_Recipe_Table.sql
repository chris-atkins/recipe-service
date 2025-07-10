CREATE TABLE recipe (
    id CHAR(24) PRIMARY KEY,
    name text,
    content text,
    owningUserId varchar(24),
    image json,
    search_vector tsvector GENERATED ALWAYS AS (to_tsvector('english', coalesce(name, '') || ' ' || coalesce(content, ''))) STORED
);

CREATE INDEX search_index ON recipe USING GIN (search_vector);