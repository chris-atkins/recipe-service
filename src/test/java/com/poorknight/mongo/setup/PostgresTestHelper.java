package com.poorknight.mongo.setup;

import com.poorknight.application.init.DatabaseSetup;
import com.poorknight.recipe.PostgresConnectionInfo;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgresTestHelper {

	private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			"postgres:16-alpine"
	).withDatabaseName("recipe");

	public static void startPostgresAndMigrateTables() {
		postgres.start();
		DatabaseSetup.migrateDatabaseTables(buildCoonnectionInfo());
	}

	public static void stopPostgres() {
		postgres.stop();
	}

	public static PostgresConnectionInfo buildCoonnectionInfo() {
		return new PostgresConnectionInfo(postgres.getUsername(), postgres.getPassword(), postgres.getJdbcUrl());
	}

	public static void deleteAllRecipes() {
		try (Connection conn = getConnection()) {
			PreparedStatement statement = conn.prepareStatement("DELETE from recipe");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static Connection getConnection() {
		try {
			return DriverManager.getConnection(
					postgres.getJdbcUrl(),
					postgres.getUsername(),
					postgres.getPassword());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteAllUsers() {
		try (Connection conn = getConnection()) {
			PreparedStatement statement = conn.prepareStatement("DELETE from recipe_user");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteAllRecipeBooks() {
		try (Connection conn = getConnection()) {
			PreparedStatement statement = conn.prepareStatement("DELETE from recipe_book");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteAllImages() {
		try (Connection conn = getConnection()) {
			PreparedStatement statement = conn.prepareStatement("DELETE from image");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
