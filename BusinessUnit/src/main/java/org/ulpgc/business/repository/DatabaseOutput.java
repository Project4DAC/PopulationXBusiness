package org.ulpgc.business.repository;

import com.zaxxer.hikari.HikariDataSource;
import org.ulpgc.business.interfaces.MessageSaver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseOutput implements MessageSaver {
    private static final Logger LOGGER = Logger.getLogger(DatabaseOutput.class.getName());
    private final HikariDataSource dataSource;

    public DatabaseOutput(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(String functionName, String message) {
        String sql = "INSERT INTO json_data (function_name, message) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, functionName);
            stmt.setString(2, message);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.warning("Error saving message to database: " + e.getMessage());
        }
    }

    @Override
    public void saveMessage(String var1) {

    }
}
