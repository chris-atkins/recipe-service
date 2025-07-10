package com.poorknight.application.init;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.poorknight.image.ImageCollectionInitializer;
import com.poorknight.recipe.PostgresConnectionInfo;
import com.poorknight.recipe.RecipeCollectionInitializer;
import com.poorknight.recipebook.RecipeBookCollectionInitializer;
import com.poorknight.user.UserCollectionInitializer;
import org.flywaydb.core.Flyway;

public class DatabaseSetup {

    public static final String DB_NAME = "recipe_db";

    public static void setupDatabaseCollections(final MongoClient client) {
        final MongoDatabase database = client.getDatabase(DatabaseSetup.DB_NAME);

        new RecipeCollectionInitializer().initializeCollection(database);
        new UserCollectionInitializer().initializeCollection(database);
        new RecipeBookCollectionInitializer().initializeCollection(database);
        new ImageCollectionInitializer().initializeCollection(database);
    }

    public static void migrateDatabaseTables(PostgresConnectionInfo connectionInfo) {
        Flyway flyway = Flyway.configure().dataSource(
                connectionInfo.getJdbcConnectionString(),
                connectionInfo.getUsername(),
                connectionInfo.getPassword()).load();
        flyway.migrate();
    }
}
