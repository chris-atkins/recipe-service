package com.poorknight.recipe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.search.SearchTag;
import org.apache.commons.lang3.RandomStringUtils;
import org.postgresql.util.PGobject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class PostgresRecipeRepository implements RecipeRepository {

    private final DataSource dataSource;

    public PostgresRecipeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Recipe saveNewRecipe(Recipe recipe) {
        if (recipe.getId() != null) {
            throw new RuntimeException("Only new recipes can be saved in this way.  There should not be a RecipeId, but one was found: " + recipe.getId().getValue());
        }

        String newId = generateNewId();
        try (Connection conn = this.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement statement = conn.prepareStatement(
                        "insert into recipe(id,name,content,owning_user_id,image,category) values(?,?,?,?,?,?)"
                );
                statement.setString(1, newId);
                statement.setString(2, recipe.getName());
                statement.setString(3, recipe.getContent());
                statement.setString(4, recipe.getOwningUserId().getValue());
                statement.setObject(5, buildImageJsonObject(recipe));
                statement.setString(6, recipe.getCategory());
                statement.execute();
                statement.close();

                insertTags(conn, newId, recipe.getTags());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            return new Recipe(
                    new Recipe.RecipeId(newId),
                    recipe.getName(),
                    recipe.getContent(),
                    recipe.getOwningUserId(),
                    recipe.getImage(),
                    recipe.getCategory(),
                    recipe.getTags());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static PGobject buildImageJsonObject(Recipe recipe) throws SQLException {
        PGobject jsonObject = null;
        if (recipe.getImage() != null) {
            jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(recipe.getImage().toJsonString());
        }
        return jsonObject;
    }

    private void insertTags(Connection conn, String recipeId, List<String> tags) throws SQLException {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        Set<String> distinctTags = new LinkedHashSet<>(tags);
        PreparedStatement statement = conn.prepareStatement("insert into recipe_tag(recipe_id,tag) values(?,?)");
        for (String tag : distinctTags) {
            statement.setString(1, recipeId);
            statement.setString(2, tag);
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();
    }

    private void deleteTags(Connection conn, String recipeId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("DELETE FROM recipe_tag WHERE recipe_id = ?");
        statement.setString(1, recipeId);
        statement.execute();
        statement.close();
    }

    private List<Recipe> attachTags(Connection conn, List<Recipe> recipes) throws SQLException {
        if (recipes.isEmpty()) {
            return recipes;
        }
        Map<String, List<String>> tagsByRecipeId = fetchTagsByRecipeId(conn, recipes);
        List<Recipe> result = new ArrayList<>(recipes.size());
        for (Recipe recipe : recipes) {
            List<String> tags = tagsByRecipeId.getOrDefault(recipe.getId().getValue(), Collections.emptyList());
            result.add(new Recipe(recipe.getId(), recipe.getName(), recipe.getContent(), recipe.getOwningUserId(), recipe.getImage(), recipe.getCategory(), tags));
        }
        return result;
    }

    private Map<String, List<String>> fetchTagsByRecipeId(Connection conn, List<Recipe> recipes) throws SQLException {
        List<String> ids = recipes.stream().map(recipe -> recipe.getId().getValue()).toList();
        String inClause = buildIdInClause(ids.size());
        PreparedStatement statement = conn.prepareStatement("SELECT recipe_id, tag FROM recipe_tag WHERE recipe_id IN (" + inClause + ")");
        for (int i = 0; i < ids.size(); i++) {
            statement.setString(i + 1, ids.get(i));
        }
        statement.execute();
        ResultSet resultSet = statement.getResultSet();
        Map<String, List<String>> tagsByRecipeId = new HashMap<>();
        while (resultSet.next()) {
            String recipeId = resultSet.getString("recipe_id");
            String tag = resultSet.getString("tag");
            tagsByRecipeId.computeIfAbsent(recipeId, key -> new ArrayList<>()).add(tag);
        }
        statement.close();
        return tagsByRecipeId;
    }

    private String generateNewId() {
        return RandomStringUtils.randomAlphanumeric(24);
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Recipe updateRecipe(Recipe recipeToUpdate) {
        if (recipeToUpdate.getId() == null) {
            throw new NoRecipeExistsForIdException(recipeToUpdate.getId());
        }

        Recipe originalRecipe = findRecipeById(recipeToUpdate.getId());

        if (originalRecipe == null) {
            throw new NoRecipeExistsForIdException(recipeToUpdate.getId());
        }

        try (Connection conn = this.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement statement = conn.prepareStatement(
                        "UPDATE recipe SET name=?,content=?,image=?,category=? WHERE id=?"
                );
                statement.setString(1, recipeToUpdate.getName());
                statement.setString(2, recipeToUpdate.getContent());
                PGobject jsonObject = buildImageJsonObject(recipeToUpdate);
                statement.setObject(3, jsonObject);
                statement.setString(4, recipeToUpdate.getCategory());
                statement.setString(5, recipeToUpdate.getId().getValue());
                statement.execute();
                statement.close();

                deleteTags(conn, recipeToUpdate.getId().getValue());
                insertTags(conn, recipeToUpdate.getId().getValue(), recipeToUpdate.getTags());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            return new Recipe(
                    new Recipe.RecipeId(recipeToUpdate.getId().getValue()),
                    recipeToUpdate.getName(),
                    recipeToUpdate.getContent(),
                    originalRecipe.getOwningUserId(),
                    recipeToUpdate.getImage(),
                    recipeToUpdate.getCategory(),
                    recipeToUpdate.getTags());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Recipe findRecipeById(Recipe.RecipeId id) {
        try (Connection conn = this.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT * FROM recipe WHERE id = ?"
            );
            statement.setString(1, id.getValue());
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            Recipe recipe = recipeFrom(resultSet);
            statement.close();
            if (recipe == null) {
                return null;
            }
            return attachTags(conn, Collections.singletonList(recipe)).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Recipe recipeFrom(ResultSet resultSet) throws SQLException, JsonProcessingException {
        if (!resultSet.next()) {
            return null;
        }
        return getRecipeFrom(resultSet);

    }

    /**
     * Assumes result set is in a correct position and has values
     */
    private Recipe getRecipeFrom(ResultSet resultSet) throws SQLException, JsonProcessingException {
        Recipe.RecipeId id = new Recipe.RecipeId(resultSet.getString("id"));
        String name = resultSet.getString("name");
        String content = resultSet.getString("content");
        Recipe.UserId owningUserId = new Recipe.UserId(resultSet.getString("owning_user_id"));
        RecipeImage image = imageFromJson(resultSet.getString("image"));
        String category = resultSet.getString("category");
        return new Recipe(id, name, content, owningUserId, image, category, Collections.emptyList());
    }

    private RecipeImage imageFromJson(String jsonString) throws JsonProcessingException {
        if (jsonString == null) {
            return null;
        }
        return new ObjectMapper().readValue(jsonString, RecipeImage.class);
    }

    @Override
    public List<Recipe> findAllRecipes() {
        try (Connection conn = this.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * from recipe");
            statement.execute();
            List<Recipe> recipes = buildRecipeListFromResultSet(statement.getResultSet());
            statement.close();
            return attachTags(conn, recipes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Recipe> buildRecipeListFromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        List<Recipe> recipes = new LinkedList<>();
        while (resultSet.next()) {
            recipes.add(getRecipeFrom(resultSet));
        }
        return recipes;
    }

    @Override
    public List<Recipe> findRecipesWithIds(List<Recipe.RecipeId> recipeIdsToFind) {
        List<Recipe.RecipeId> validatedRecipeIds = recipeIdsToFind.stream().filter(Objects::nonNull).filter(r -> r.getValue() != null).toList();
        try (Connection conn = this.getConnection()) {
            String inClause = buildIdInClause(validatedRecipeIds.size());
            PreparedStatement statement = conn.prepareStatement("SELECT * from recipe WHERE id in (" + inClause + ")");
            for (int i = 0; i < validatedRecipeIds.size(); i++) {
                statement.setString(i + 1, validatedRecipeIds.get(i).getValue());
            }
            statement.execute();
            List<Recipe> recipes = buildRecipeListFromResultSet(statement.getResultSet());
            statement.close();
            return attachTags(conn, recipes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildIdInClause(int size) {
        List<String> questionMarks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            questionMarks.add("?");
        }
        return String.join(",", questionMarks);
    }

    @Override
    public List<Recipe> searchRecipes(List<SearchTag> searchTags) {
        try (Connection conn = this.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT * from recipe WHERE search_vector @@ to_tsquery('english',?)");

            statement.setString(1, buildTsQueryClause(searchTags));
            statement.execute();
            List<Recipe> recipes = buildRecipeListFromResultSet(statement.getResultSet());
            statement.close();
            return attachTags(conn, recipes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildTsQueryClause(List<SearchTag> searchTags) {
        List<String> clauseElements = searchTags.stream().map(SearchTag::getValue).toList();
        return String.join(" | ", clauseElements);
    }

    @Override
    public void deleteRecipe(Recipe.RecipeId id) {
        try (Connection conn = this.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM recipe WHERE id = ?"
            );
            statement.setString(1, id.getValue());
            statement.execute();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
