package org.ulpgc.IneSubscriberDataMart.services;

import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageSaver;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseOutput implements MessageSaver {
    private final DataSource dataSource;

    // Constructor to initialize dataSource
    public DatabaseOutput(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Implement the saveMessage method from MessageSaver interface
    @Override
    public void saveMessage(String message) {
        // Adjust the SQL to insert the message into your database
        String sql = "INSERT INTO json_data (message) VALUES (?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, message); // Set the message to insert into the database
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error saving message to database: " + e.getMessage());
        }
    }
}
