package com.poorknight.application.init;

import com.poorknight.recipe.PostgresConnectionInfo;
import org.flywaydb.core.Flyway;

public class DatabaseSetup {

    public static void migrateDatabaseTables(PostgresConnectionInfo connectionInfo) {
        Flyway flyway = Flyway.configure()
                .dataSource(
                    connectionInfo.getJdbcConnectionString(),
                    connectionInfo.getUsername(),
                    connectionInfo.getPassword())
                .defaultSchema("recipe")
                .load();
        flyway.migrate();
    }
}
