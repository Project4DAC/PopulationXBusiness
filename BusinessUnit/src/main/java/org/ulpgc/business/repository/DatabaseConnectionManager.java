package org.ulpgc.business.repository;

import org.ulpgc.business.Exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static Connection connection;
    private static final String DB_URL = "jdbc:sqlite:busxpop.db"; // Adjust path as needed

    private DatabaseConnectionManager() {} // Prevent instantiation

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");

                // Create or connect to the database
                connection = DriverManager.getConnection(DB_URL);

                // Optional: Configure connection properties
                connection.setAutoCommit(false); // Use transactions
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            throw new DatabaseException("Error establishing database connection", e);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error closing database connection", e);
        }
    }

    public static void commitTransaction() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error committing transaction", e);
        }
    }

    public static void rollbackTransaction() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error rolling back transaction", e);
        }
    }
}