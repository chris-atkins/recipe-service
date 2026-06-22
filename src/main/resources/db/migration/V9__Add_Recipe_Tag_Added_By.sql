ALTER TABLE recipe_tag ADD COLUMN added_by_user_id varchar(24);

UPDATE recipe_tag rt SET added_by_user_id = r.owning_user_id FROM recipe r WHERE rt.recipe_id = r.id;
