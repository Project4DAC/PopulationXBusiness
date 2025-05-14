package org.ulpgc.IneSubscriberDataMart.services;

import org.ulpgc.IneSubscriberDataMart.Interfaces.DataProcessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class INEDataMartProcessor implements DataProcessor {

    private static final Logger LOGGER = Logger.getLogger(INEDataMartProcessor.class.getName());
    private final String dataMartRoot;

    // DataSource for database connection
    private final DataSource dataSource;

    public INEDataMartProcessor(String dataMartRoot, DataSource dataSource) {
        this.dataMartRoot = dataMartRoot;
        this.dataSource = dataSource;
    }

    @Override
    public void process() {
        // Here, you would handle your data processing
        // For example, let's say you want to aggregate or process some data in the database.

        // In this example, we will just log and perform a simple query operation.
        LOGGER.info("Starting data processing...");

        try (Connection connection = dataSource.getConnection()) {
            // For illustration, let's say we want to insert some dummy data or perform a query
            String sql = "INSERT INTO some_table (column1, column2) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Example of inserting data
                statement.setString(1, "some data");
                statement.setString(2, "other data");

                int rowsAffected = statement.executeUpdate();
                LOGGER.info("Rows affected: " + rowsAffected);
            }

            // You could also perform data transformations, aggregations, etc., here
        } catch (SQLException e) {
            LOGGER.severe("Error processing data: " + e.getMessage());
        }
        
        LOGGER.info("Data processing completed.");
    }
}
