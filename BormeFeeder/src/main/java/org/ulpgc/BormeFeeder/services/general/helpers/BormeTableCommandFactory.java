package org.ulpgc.BormeFeeder.services.general.helpers;


import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.general.commands.CreateTableCommand;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

public class BormeTableCommandFactory {
    public static Command createInitializeDatabaseCommand(DataSource dataSource) {
        List<String> allStatements = Arrays.asList(
                "CREATE TABLE IF NOT EXISTS registros (" +
                        "    fecha TEXT," +
                        "    tipo TEXT," +
                        "    empresa_nombre TEXT," +
                        "    empresa_cif TEXT," +
                        "    PRIMARY KEY (fecha, empresa_cif)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS publicaciones (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT, " +
                        "fecha_publicacion TEXT, " +
                        "codigo_respuesta INTEGER, " +
                        "texto_respuesta TEXT)",

                "CREATE TABLE IF NOT EXISTS diarios (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "numero INTEGER, " +
                        "identificador TEXT, " +
                        "url_pdf TEXT, " +
                        "tamanyo_bytes INTEGER, " +
                        "tamanyo_kbytes INTEGER, " +
                        "publicacion_id INTEGER, " +
                        "FOREIGN KEY(publicacion_id) REFERENCES publicaciones(id))",

                "CREATE TABLE IF NOT EXISTS secciones (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "codigo TEXT, " +
                        "nombre TEXT, " +
                        "diario_id INTEGER, " +
                        "FOREIGN KEY(diario_id) REFERENCES diarios(id))",

                "CREATE TABLE IF NOT EXISTS apartados (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "codigo TEXT, " +
                        "nombre TEXT, " +
                        "seccion_id INTEGER, " +
                        "FOREIGN KEY(seccion_id) REFERENCES secciones(id))",

                "CREATE TABLE IF NOT EXISTS items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "identificador TEXT, " +
                        "titulo TEXT, " +
                        "url_pdf TEXT, " +
                        "url_html TEXT, " +
                        "url_xml TEXT, " +
                        "tamanyo_bytes INTEGER, " +
                        "tamanyo_kbytes INTEGER, " +
                        "pagina_inicial INTEGER, " +
                        "pagina_final INTEGER, " +
                        "seccion_id INTEGER, " +
                        "apartado_id INTEGER, " +
                        "FOREIGN KEY(seccion_id) REFERENCES secciones(id), " +
                        "FOREIGN KEY(apartado_id) REFERENCES apartados(id))",

                "CREATE INDEX IF NOT EXISTS idx_items_identificador " +
                        "ON items(identificador)",

                "CREATE INDEX IF NOT EXISTS idx_publicaciones_fecha " +
                        "ON publicaciones(fecha_publicacion)",

                "CREATE INDEX IF NOT EXISTS idx_items_seccion_apartado " +
                        "ON items(seccion_id, apartado_id)"
        );
        return new CreateTableCommand(dataSource, allStatements);
    }
}