package org.ulpgc.business.interfaces;

import java.sql.SQLException;

public interface Command {
    String execute() throws SQLException;
}
