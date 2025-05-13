package org.ulpgc.business.operations.DAO;

import org.ulpgc.business.Exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database initialization including schema creation
 */
public class DatabaseInitializer {
    
    /**
     * Initialize the database schema if it doesn't exist
     */
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create tables if they don't exist
            createTables(stmt);
            
            // Commit changes
            DatabaseConnectionManager.commitTransaction();
            
            System.out.println("Database schema initialized successfully");
            
        } catch (SQLException e) {
            DatabaseConnectionManager.rollbackTransaction();
            throw new DatabaseException("Error initializing database", e);
        }
    }
    
    private static void createTables(Statement stmt) throws SQLException {
        // Create operaciones table
        stmt.execute("CREATE TABLE IF NOT EXISTS operaciones (" +
                "id INTEGER PRIMARY KEY, " +
                "COD_IOE TEXT, " +
                "nombre TEXT, " +
                "codigo TEXT UNIQUE, " +
                "url_operacion TEXT)");
        
        // Create tablas table
        stmt.execute("CREATE TABLE IF NOT EXISTS tablas (" +
                "id INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "codigo TEXT, " +
                "periodicidad INTEGER, " +
                "publicacion INTEGER, " +
                "periodo_ini INTEGER, " +
                "anyo_periodo_ini TEXT, " +
                "fecha_ref_fin TEXT, " +
                "ultima_modificacion INTEGER)");
        
        // Create relationship table between tablas and operaciones
        stmt.execute("CREATE TABLE IF NOT EXISTS tabla_operaciones (" +
                "tabla_id INTEGER, " +
                "operacion_id INTEGER, " +
                "PRIMARY KEY (tabla_id, operacion_id), " +
                "FOREIGN KEY (tabla_id) REFERENCES tablas(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE)");
        
        // Create variables table
        stmt.execute("CREATE TABLE IF NOT EXISTS variables (" +
                "id INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "tabla_id INTEGER, " +
                "FOREIGN KEY (tabla_id) REFERENCES tablas(id) ON DELETE CASCADE)");
        
        // Create variable_valores table
        stmt.execute("CREATE TABLE IF NOT EXISTS variable_valores (" +
                "id TEXT, " +
                "nombre TEXT, " +
                "variable_id INTEGER, " +
                "PRIMARY KEY (id, variable_id), " +
                "FOREIGN KEY (variable_id) REFERENCES variables(id) ON DELETE CASCADE)");
        
        // Create propiedades_variable table
        stmt.execute("CREATE TABLE IF NOT EXISTS propiedades_variable (" +
                "variable_id INTEGER PRIMARY KEY, " +
                "orden INTEGER, " +
                "tipo TEXT, " +
                "es_clave BOOLEAN, " +
                "FOREIGN KEY (variable_id) REFERENCES variables(id) ON DELETE CASCADE)");
        
        // Create datos table to store JSON data
        stmt.execute("CREATE TABLE IF NOT EXISTS datos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tabla_id INTEGER, " +
                "dato_json TEXT, " +
                "fecha_actualizacion INTEGER, " +
                "FOREIGN KEY (tabla_id) REFERENCES tablas(id) ON DELETE CASCADE)");
        
        // Create metadatos_tabla table
        stmt.execute("CREATE TABLE IF NOT EXISTS metadatos_tabla (" +
                "tabla_id INTEGER, " +
                "nombre TEXT, " +
                "valor TEXT, " +
                "PRIMARY KEY (tabla_id, nombre), " +
                "FOREIGN KEY (tabla_id) REFERENCES tablas(id) ON DELETE CASCADE)");
        
        // Create frecuencias table
        stmt.execute("CREATE TABLE IF NOT EXISTS frecuencias (" +
                "id INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "descripcion TEXT)");
    }
}