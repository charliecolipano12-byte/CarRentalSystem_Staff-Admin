package com.mycompany.carrentalforms;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Single place that knows how to reach MySQL.
 * Credentials come from /db.properties (src/main/resources), never from code.
 * The environment variable DB_PASSWORD, if set, overrides the file.
 */
public final class Database {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                    "db.properties not found. Put it in src/main/resources/.");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
    }

    private Database() { }

    
    /** Always use with try-with-resources so the connection is closed. */
    public static Connection getConnection() throws SQLException {
        String password = System.getenv("DB_PASSWORD");
        if (password == null) password = PROPS.getProperty("db.password");
        
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                password);
    }

    /** Quick check you can call at startup to show a friendly error. */
  public static boolean isReachable() {
    try (Connection c = getConnection()) {
        return c.isValid(2);
    } catch (SQLException e) {
        System.err.println("DB connection failed: " + e.getMessage()
                + "  (SQLState " + e.getSQLState() + ", code " + e.getErrorCode() + ")");
        return false;
    }

    }
}
