package org.ulpgc.business.repository;

import java.sql.Connection;

public class DatabaseConnectionManager {
    private static Connection connection;

    public static Connection getConnection() {
        // Implementar lógica de conexión singleton
        if (connection == null) {
            // Configurar conexión JDBC
        }
        return connection;
    }

    public static void closeConnection() {
        // Cerrar conexión de manera segura
    }
}