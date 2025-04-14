package org.ulpgc.inefeeder.servicios.general.helpers;




import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.general.commands.CreateTableCommand;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class INETableCommandFactory {
    public static Command createInitializeDatabaseCommand(DataSource dataSource) {
        List<String> allStatements = Arrays.asList(
                "CREATE TABLE IF NOT EXISTS operaciones (" +
                        "id INT PRIMARY KEY, " +
                        "COD_IOE TEXT," +
                        "nombre TEXT, " +
                        "codigo TEXT," +
                        "url_operacion TEXT)",

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

                "CREATE TABLE IF NOT EXISTS tabla_operaciones (" +
                        "tabla_id INTEGER, " +
                        "operacion_id INTEGER, " +
                        "PRIMARY KEY(tabla_id, operacion_id), " +
                        "FOREIGN KEY(tabla_id) REFERENCES tablas(id), " +
                        "FOREIGN KEY(operacion_id) REFERENCES operaciones(id))",

                "CREATE INDEX IF NOT EXISTS idx_tabla_operaciones " +
                        "ON tabla_operaciones(tabla_id, operacion_id)"
        );
        return new CreateTableCommand(dataSource, allStatements);
    };
}