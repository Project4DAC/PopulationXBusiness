package org.ulpgc.IneSubscriberDataMart.query;

import org.ulpgc.IneSubscriberDataMart.Interfaces.Command;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CreateTableCommand implements Command {
    private final DataSource dataSource;
    private final List<String> sqlStatements;

    public CreateTableCommand(DataSource dataSource, List<String> sqlStatements) {
        this.dataSource = dataSource;
        this.sqlStatements = sqlStatements;
    }

    @Override
    public String execute() {
        try (Connection conn = dataSource.getConnection()) {
            for (String stmt : sqlStatements) {
                conn.createStatement().execute(stmt);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}