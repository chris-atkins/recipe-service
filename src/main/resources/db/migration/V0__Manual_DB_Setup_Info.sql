--CREATE SCHEMA recipe;
--GRANT CONNECT ON DATABASE recipe TO "recipe-service";
--ALTER DATABASE recipe SET search_path TO recipe,recipe;
--grant create,usage on schema recipe to "recipe-service";

-- I manually ran all this in prod after creating the recipe-service user in Digital Ocean's UI
-- Also important to make the tests match what we expect to see in prod - see unit test postgres setup