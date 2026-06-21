-- Speeds up the tag-suggestion query's `WHERE r.category = ?` filter (and future
-- category-scoped reads). Partial: the query never matches NULL, and uncategorized
-- recipes are just noise for this index.
CREATE INDEX idx_recipe_category ON recipe (category) WHERE category IS NOT NULL;
