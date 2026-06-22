-- Stable, deterministic ordering for recipe list reads (newest-first). The list reads
-- (findAllRecipes/searchRecipes/findRecipesWithIds) ORDER BY created_at DESC, id DESC.
-- Existing rows get the migration timestamp (they tie, broken by the id tiebreaker);
-- new rows fill automatically via DEFAULT now(), so the INSERT statement is unchanged.
ALTER TABLE recipe ADD COLUMN created_at timestamptz NOT NULL DEFAULT now();

-- Column order/direction matches the read ORDER BY exactly, so newest-first can be served
-- straight from the index (no sort step) if the table ever grows large.
CREATE INDEX idx_recipe_created_at_id ON recipe (created_at DESC, id DESC);
