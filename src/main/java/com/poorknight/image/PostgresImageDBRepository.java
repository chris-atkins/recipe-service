package com.poorknight.image;

import com.poorknight.recipe.PostgresConnectionInfo;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.*;

public class PostgresImageDBRepository extends ImageDBRepository {
    private final PostgresConnectionInfo connectionInfo;

    public PostgresImageDBRepository(PostgresConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    Image saveNewImage(Image image) {
        validateImage(image);
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO image(id, image_id, owning_user_id, url) VALUES (?, ?, ?, ?)");
            String id = generateNewId();
            statement.setString(1, id);
            statement.setString(2, image.getImageId());
            statement.setString(3, image.getOwningUserId());
            statement.setString(4, image.getImageUrl());
            statement.execute();
            statement.close();
            return image;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateImage(Image image) {
        if (image.getImageId() == null || image.getImageId().isBlank()) {
            throw new RuntimeException("imageId must contain a value");
        }
        if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
            throw new RuntimeException("url must contain a value");
        }
        if (image.getOwningUserId() == null || image.getOwningUserId().isBlank()) {
            throw new RuntimeException("userId must contain a value");
        }
    }

    @Override
    Image findImage(String imageId) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM image WHERE image_id = ?");
            statement.setString(1, imageId);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            Image image = buildImageFromResultSet(resultSet);
            statement.close();
            return image;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Image buildImageFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new Image(
                    resultSet.getString("image_id"),
                    resultSet.getString("url"),
                    resultSet.getString("owning_user_id")
            );
        }
        return null;
    }

    @Override
    void deleteImage(String imageId) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM image WHERE image_id=?");
            statement.setString(1, imageId);
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

    private String generateNewId() {
        return RandomStringUtils.randomAlphanumeric(24);
    }
}
