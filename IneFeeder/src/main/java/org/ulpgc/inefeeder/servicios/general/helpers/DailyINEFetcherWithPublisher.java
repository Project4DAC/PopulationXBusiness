package org.ulpgc.inefeeder.servicios.general.helpers;

import org.ulpgc.inefeeder.commands.update.INEFetchAndSaveDataCommand;
import org.ulpgc.inefeeder.commands.publisher.PublishINEDataCommand;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyINEFetcherWithPublisher implements Runnable {
    private final DataSource dataSource;
    private final Publisher publisher;
    private static final long PAUSE_MS = 300;
    private final boolean publishEnabled;

    public DailyINEFetcherWithPublisher(DataSource dataSource) {
        this(dataSource, null, false);
    }
    
    public DailyINEFetcherWithPublisher(DataSource dataSource, Publisher publisher) {
        this(dataSource, publisher, true);
    }
    
    private DailyINEFetcherWithPublisher(DataSource dataSource, Publisher publisher, boolean publishEnabled) {
        this.dataSource = dataSource;
        this.publisher = publisher;
        this.publishEnabled = publishEnabled;
    }

    @Override
    public void run() {
        try {
            // 1. Fetch operaciones disponibles
            runSingle("OPERACIONES_DISPONIBLES", null);

            // 2. Por cada operación, fetch tablas
            for (Map.Entry<Integer, String> entry : fetchOperaciones().entrySet()) {
                int operId = entry.getKey();
                // Use ID para las operaciones en lugar del código
                runSingle("TABLAS_OPERACION", String.valueOf(operId));
            }

            // 3. Por cada tabla, fetch datos y metadatos
            for (int tablaId : fetchIdsFromTable("tablas")) {
                // Use ID para los datos de tabla
                runSingle("DATOS_TABLA", String.valueOf(tablaId));
                runSingle("METADATOS_TABLA", String.valueOf(tablaId));
            }

            // 4. Por cada variable, fetch valores y propiedades
            for (int varId : fetchIdsFromTable("variables")) {
                runSingle("VALORES_VARIABLE", String.valueOf(varId));
                runSingle("PROPIEDADES_VARIABLE", String.valueOf(varId));
            }

            // 5. Fetch frecuencias
            runSingle("FRECUENCIAS", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close publisher connection if it was used
            if (publishEnabled && publisher != null) {
                publisher.close();
            }
        }
    }

    /**
     * Ejecuta una sola consulta INE, guarda resultados y opcionalmente publica a ActiveMQ.
     * @param function  función INE a invocar (sin parámetros CSV)
     * @param inputValue  valor único que se añade al path (puede ser null)
     */
    private void runSingle(String function, String inputValue) {
        // Saltar IDs no válidos (0)
        if (inputValue != null && "0".equals(inputValue)) {
            System.out.println("Saltando consulta " + function + " para ID inválido 0");
            return;
        }

        try {
            Input input = new SimpleInput();
            Output output = new SimpleOutput();
            input.setValue("function", function);
            input.setValue("language", "es");
            input.setValue("inputValue", inputValue);

            // Fetch and save data to database
            new INEFetchAndSaveDataCommand(input, output, dataSource).execute();
            System.out.println("Consulta realizada: " + function + (inputValue != null ? " (" + inputValue + ")" : ""));
            
            // Publish to ActiveMQ if enabled
            if (publishEnabled && publisher != null) {
                String jsonResponse = output.getValue("jsonResponse");
                if (jsonResponse != null && !jsonResponse.isEmpty()) {
                    input.setValue("jsonResponse", jsonResponse);
                    new PublishINEDataCommand(input, output, publisher).execute();
                    System.out.println("Datos publicados a ActiveMQ: " + function);
                }
            }
            
            Thread.sleep(PAUSE_MS);
        } catch (Exception e) {
            System.err.println("Error en función: " + function + (inputValue != null ? " con inputValue=" + inputValue : ""));
            e.printStackTrace();
        }
    }


    private Map<Integer, String> fetchOperaciones() {
        Map<Integer, String> operaciones = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, codigo FROM operaciones")) {
            while (rs.next()) {
                operaciones.put(rs.getInt("id"), rs.getString("codigo"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching operaciones", e);
        }
        return operaciones;
    }

    private List<Integer> fetchIdsFromTable(String tableName) {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM " + tableName)) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching IDs from " + tableName, e);
        }
        return ids;
    }

    private List<String> fetchCodesFromTable(String tableName, String codeColumn) {
        List<String> codes = new ArrayList<>();
        String query = "SELECT " + codeColumn + " FROM " + tableName + " WHERE " + codeColumn + " IS NOT NULL";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                codes.add(rs.getString(codeColumn));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching codes from " + tableName, e);
        }
        return codes;
    }
}