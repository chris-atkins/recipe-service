package com.poorknight.recipe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.search.SearchTag;
import org.apache.commons.lang3.RandomStringUtils;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.*;

public class PostgresRecipeRepository implements RecipeRepository {

    private final PostgresConnectionInfo postgresConnectionInfo;

    public PostgresRecipeRepository(PostgresConnectionInfo postgresConnectionInfo) {
        this.postgresConnectionInfo = postgresConnectionInfo;
    }

    @Override
    public Recipe saveNewRecipe(Recipe recipe) {
        if (recipe.getId() != null) {
            throw new RuntimeException("Only new recipes can be saved in this way.  There should not be a RecipeId, but one was found: " + recipe.getId().getValue());
        }

        try (Connection conn = this.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "insert into recipe(id,name,content,owningUserId,image) values(?,?,?,?,?)"
            );
            String newId = generateNewId();
            statement.setString(1, newId);
            statement.setString(2, recipe.getName());
            statement.setString(3, recipe.getContent());
            statement.setString(4, recipe.getOwningUserId().getValue());
            statement.setObject(5, buildImageJsonObject(recipe));
            statement.execute();
            statement.close();

            return new Recipe(
                    new Recipe.RecipeId(newId),
                    recipe.getName(),
                    recipe.getContent(),
                    recipe.getOwningUserId(),
                    recipe.getImage());
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

    private String generateNewId() {
        return RandomStringUtils.randomAlphanumeric(24);
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    this.postgresConnectionInfo.getJdbcConnectionString(),
                    this.postgresConnectionInfo.getUsername(),
                    this.postgresConnectionInfo.getPassword());
        } catch (Exception e) {
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
            PreparedStatement statement = conn.prepareStatement(
                    "UPDATE recipe SET name=?,content=?,image=? WHERE id=?"
            );
            statement.setString(1, recipeToUpdate.getName());
            statement.setString(2, recipeToUpdate.getContent());
            PGobject jsonObject = buildImageJsonObject(recipeToUpdate);
            statement.setObject(3, jsonObject);
            statement.setString(4, recipeToUpdate.getId().getValue());

            statement.execute();
            statement.close();
            return new Recipe(
                    new Recipe.RecipeId(recipeToUpdate.getId().getValue()),
                    recipeToUpdate.getName(),
                    recipeToUpdate.getContent(),
                    originalRecipe.getOwningUserId(),
                    recipeToUpdate.getImage());
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
            return recipe;
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
        Recipe.UserId owningUserId = new Recipe.UserId(resultSet.getString("owningUserId"));
        RecipeImage image = imageFromJson(resultSet.getString("image"));
        return new Recipe(id, name, content, owningUserId, image);
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
            return recipes;
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
            return recipes;
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
            return recipes;
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
