package com.poorknight.test.setup;

import com.poorknight.application.init.DatabaseSetup;
import com.poorknight.recipe.PostgresConnectionInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgresTestHelper {

	private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			"postgres:16-alpine"
	).withDatabaseName("recipe");

	// ONE pool per container lifecycle, shared by all of a test class's tests — mirrors how prod
	// builds a single shared HikariDataSource in RecipeServiceApplication.initializePostgres().
	// Previously buildDataSource() minted a fresh 10-connection pool per test and most classes
	// never closed it, so leaked pools piled up against the one Postgres → "too many clients already".
	private static HikariDataSource sharedDataSource;

	public static void startPostgresAndMigrateTables() {
		postgres.start();
		updateDatabaseQueryPrefixToMatchProd();
		DatabaseSetup.migrateDatabaseTables(buildCoonnectionInfo());
		sharedDataSource = createDataSource();
	}

	private static void updateDatabaseQueryPrefixToMatchProd() {
		try (Connection conn = getConnection()) {
			PreparedStatement statement = conn.prepareStatement("ALTER DATABASE recipe SET search_path TO recipe,recipe");
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void stopPostgres() {
		if (sharedDataSource != null) {
			sharedDataSource.close();
			sharedDataSource = null;
		}
		postgres.stop();
	}

	public static PostgresConnectionInfo buildCoonnectionInfo() {
		return new PostgresConnectionInfo(postgres.getUsername(), postgres.getPassword(), postgres.getJdbcUrl());
	}

	public static DataSource buildDataSource() {
		return sharedDataSource;
	}

	private static HikariDataSource createDataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(postgres.getJdbcUrl());
		config.setUsername(postgres.getUsername());
		config.setPassword(postgres.getPassword());
		config.setMaximumPoolSize(5);  // tests run sequentially; one shared pool, far under Postgres max_connections
		return new HikariDataSource(config);
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
