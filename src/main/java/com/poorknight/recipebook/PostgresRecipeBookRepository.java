package com.poorknight.recipebook;

import com.poorknight.recipe.PostgresConnectionInfo;
import com.poorknight.user.User;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresRecipeBookRepository implements RecipeBookRepository {

    private final PostgresConnectionInfo connectionInfo;

    public PostgresRecipeBookRepository(PostgresConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public RecipeBook getRecipeBook(RecipeBook.UserId userId) {
        validateId(userId.getValue());
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM recipe_book WHERE user_id=?");
            statement.setString(1, userId.getValue());
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            RecipeBook foundUser = buildRecipeBookFromResultSet(resultSet);
            statement.close();
            return foundUser;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateId(String idToValidate) {
        if (idToValidate.length() != 24) {
            throw new InvalidIdException(idToValidate);
        }
    }

    private RecipeBook buildRecipeBookFromResultSet(ResultSet resultSet) throws SQLException {
        String userId = null;
        List<RecipeBook.RecipeId> recipeIds = new ArrayList<>();

        while(resultSet.next()) {
            if (userId == null) {
                userId = resultSet.getString("user_id");
            }
            recipeIds.add(new RecipeBook.RecipeId(resultSet.getString("recipe_id")));
        }

        return userId == null ? null : new RecipeBook(new RecipeBook.UserId(userId), recipeIds);
    }

    @Override
    public RecipeBook.RecipeId addRecipeIdToRecipeBook(RecipeBook.UserId userId, RecipeBook.RecipeId recipeId) {
        validateId(userId.getValue());
        validateId(recipeId.getValue());
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO recipe_book(user_id, recipe_id) VALUES (?,?)");
            statement.setString(1, userId.getValue());
            statement.setString(2, recipeId.getValue());
            statement.execute();
            statement.close();
            return recipeId;
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key value violates unique constraint")) {
                return recipeId;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRecipeFromRecipeBook(RecipeBook.UserId userId, RecipeBook.RecipeId recipeId) {
        RecipeBook recipeBook = getRecipeBook(userId);
        if (recipeBook == null) {
            throw new RecipeBookNotFoundException(userId);
        }
        if (!recipeBook.getRecipeIds().contains(recipeId)) {
            throw new RecipeNotInBookException(recipeId);
        }

        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM recipe_book WHERE user_id=? AND recipe_id=?");
            statement.setString(1, userId.getValue());
            statement.setString(2, recipeId.getValue());
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    this.connectionInfo.getJdbcConnectionString(),
                    this.connectionInfo.getUsername(),
                    this.connectionInfo.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
