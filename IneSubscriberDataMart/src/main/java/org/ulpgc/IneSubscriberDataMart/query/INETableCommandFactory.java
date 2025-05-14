package org.ulpgc.IneSubscriberDataMart.query;

import org.ulpgc.IneSubscriberDataMart.Interfaces.Command;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class INETableCommandFactory {
    public static Command createInitializeDatabaseCommand(DataSource dataSource) {
        List<String> allStatements = Arrays.asList(
                "CREATE TABLE IF NOT EXISTS grupos_operaciones (" +
                        "id INTEGER PRIMARY KEY, " +
                        "nombre TEXT)",

                // Operaciones estadísticas del INE
                "CREATE TABLE IF NOT EXISTS operaciones (" +
                        "id INTEGER PRIMARY KEY, " +
                        "COD_IOE TEXT, " +
                        "nombre TEXT, " +
                        "codigo TEXT, " +
                        "url_operacion TEXT, " +
                        "grupo_id INTEGER, " +
                        "FOREIGN KEY(grupo_id) REFERENCES grupos_operaciones(id))",

                // Tablas dentro de cada operación
                "CREATE TABLE IF NOT EXISTS tablas (" +
                        "id INTEGER PRIMARY KEY, " +
                        "nombre TEXT, " +
                        "codigo TEXT, " +
                        "periodicidad INTEGER, " +
                        "publicacion INTEGER, " +
                        "periodo_ini INTEGER, " +
                        "anyo_periodo_ini TEXT, " +
                        "fecha_ref_fin TEXT, " +
                        "ultima_modificacion BIGINT)",

                // Relación entre tablas y operaciones (n:n)
                "CREATE TABLE IF NOT EXISTS tabla_operaciones (" +
                        "tabla_id INTEGER, " +
                        "operacion_id INTEGER, " +
                        "PRIMARY KEY(tabla_id, operacion_id), " +
                        "FOREIGN KEY(tabla_id) REFERENCES tablas(id), " +
                        "FOREIGN KEY(operacion_id) REFERENCES operaciones(id))",

                // Variables (sexo, edad, tipo, etc.)
                "CREATE TABLE IF NOT EXISTS variables (" +
                        "id INTEGER PRIMARY KEY, " +
                        "nombre TEXT, " +
                        "tabla_id INTEGER, " +
                        "FOREIGN KEY(tabla_id) REFERENCES tablas(id))",

                // Posibles valores de cada variable
                "CREATE TABLE IF NOT EXISTS variable_valores (" +
                        "id TEXT, " +
                        "nombre TEXT, " +
                        "variable_id INTEGER, " +
                        "PRIMARY KEY(id, variable_id), " +
                        "FOREIGN KEY(variable_id) REFERENCES variables(id))",

                // Propiedades adicionales de cada variable (orden, tipo, etc.)
                "CREATE TABLE IF NOT EXISTS propiedades_variable (" +
                        "variable_id INTEGER PRIMARY KEY, " +
                        "orden INTEGER, " +
                        "tipo TEXT, " +
                        "es_clave BOOLEAN, " +
                        "FOREIGN KEY(variable_id) REFERENCES variables(id))",

                // Datos estadísticos completos en formato JSON
                "CREATE TABLE IF NOT EXISTS datos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "tabla_id INTEGER, " +
                        "dato_json TEXT, " +
                        "fecha_actualizacion BIGINT, " +
                        "FOREIGN KEY(tabla_id) REFERENCES tablas(id))",

                // Metadatos adicionales por tabla
                "CREATE TABLE IF NOT EXISTS metadatos_tabla (" +
                        "tabla_id INTEGER, " +
                        "nombre TEXT, " +
                        "valor TEXT, " +
                        "PRIMARY KEY(tabla_id, nombre), " +
                        "FOREIGN KEY(tabla_id) REFERENCES tablas(id))",

                // Frecuencias
                "CREATE TABLE IF NOT EXISTS frecuencias (" +
                        "id INTEGER PRIMARY KEY, " +
                        "nombre TEXT, " +
                        "descripcion TEXT)",

                // Índices para optimizar consultas comunes
                "CREATE INDEX IF NOT EXISTS idx_tabla_operaciones ON tabla_operaciones(tabla_id, operacion_id)",
                "CREATE INDEX IF NOT EXISTS idx_variables_tabla ON variables(tabla_id)",
                "CREATE INDEX IF NOT EXISTS idx_valores_variable ON variable_valores(variable_id)",
                "CREATE INDEX IF NOT EXISTS idx_datos_tabla ON datos(tabla_id)",
                "CREATE INDEX IF NOT EXISTS idx_metadatos_tabla ON metadatos_tabla(tabla_id)"
        );

        return new CreateTableCommand(dataSource, allStatements);
    }
}