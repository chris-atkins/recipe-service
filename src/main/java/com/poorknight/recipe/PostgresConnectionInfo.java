package com.poorknight.recipe;

public class PostgresConnectionInfo {
    private final String username;
    private final String password;
    private final String jdbcConnectionString;

    public PostgresConnectionInfo(String username, String password, String jdbcConnectionString) {
        this.username = username;
        this.password = password;
        this.jdbcConnectionString = jdbcConnectionString;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getJdbcConnectionString() {
        return jdbcConnectionString;
    }
}
