package com.poorknight.user;

import com.poorknight.recipe.PostgresConnectionInfo;
import com.poorknight.user.save.NonUniqueEmailException;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.*;

public class PostgresUserRepository implements UserRepository {

    private final PostgresConnectionInfo connectionInfo;

    public PostgresUserRepository(PostgresConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public User saveNewUser(User userToSave) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO recipe_user(id, email, name) VALUES (?, ?, ?)");
            String id = generateNewId();
            statement.setString(1, id);
            statement.setString(2, userToSave.getEmail());
            statement.setString(3, userToSave.getName());
            statement.execute();
            statement.close();
            return new User(new User.UserId(id), userToSave.getName(), userToSave.getEmail());
        } catch (SQLException e) {
            if (isDuplicateEmailError(e)) {
                throw new NonUniqueEmailException(userToSave.getEmail());
            }
            throw new RuntimeException(e);
        }
    }

    private boolean isDuplicateEmailError(SQLException e) {
        return e.getMessage().contains("duplicate key value violates unique constraint") && e.getMessage().contains("recipe_user_email_key");
    }

    @Override
    public User findUserById(User.UserId id) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM recipe_user WHERE id=?");
            statement.setString(1, id.getValue());
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            User foundUser = buildUserFromResultSet(resultSet);
            statement.close();
            return foundUser;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User buildUserFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            String id = resultSet.getString("id");
            String email = resultSet.getString("email");
            String name = resultSet.getString("name");
            return new User(new User.UserId(id), name, email);
        }
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM recipe_user WHERE email=?");
            statement.setString(1, email);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            User foundUser = buildUserFromResultSet(resultSet);
            statement.close();
            return foundUser;
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

    private String generateNewId() {
        return RandomStringUtils.randomAlphanumeric(24);
    }
}
