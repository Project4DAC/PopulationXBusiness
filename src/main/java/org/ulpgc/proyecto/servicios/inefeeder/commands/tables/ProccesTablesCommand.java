package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ProccesTablesCommand implements Command {
    private final Input input;
    private final Output output;
    private final int corePoolSize;

    public ProccesTablesCommand(Input input, Output output, int corePoolSize) {
        this.input = input;
        this.output = output;
        this.corePoolSize = corePoolSize;
    }

    @Override
    public String execute() {

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(corePoolSize);
            DataSource dataSource = input.getValue("dataSource");
            if (dataSource == null) {
                throw new IllegalArgumentException("DataSource is not set in Input");
            }
            try (Connection conn = dataSource.getConnection()) {
                createTablasAndRelationTables(conn);

                List<Integer> operationIds = obtenerIdsOperaciones(conn);

                List<CompletableFuture<Void>> futures = operationIds.stream()
                        .map(operacionId -> CompletableFuture.runAsync(() -> {
                            try {
                                procesarTablaParaOperacion(operacionId);
                            } catch (Exception ignored) {
                            }
                        }, executorService))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (SQLException ignored) {
            } finally {
                shutdownExecutorService(executorService);
            }

            return "Processed tables for all operations";
        } catch (Exception e) {
            throw new RuntimeException("Error executing ProcessTables command", e);
        }
    }

    private void procesarTablaParaOperacion(Integer operacionId) {

    }

    private void createTablasAndRelationTables(Connection conn) throws SQLException {
    }

    private List<Integer> obtenerIdsOperaciones(Connection conn) throws SQLException {
        List<Integer> operationIds = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM operaciones")) {

            while (rs.next()) {
                operationIds.add(rs.getInt("id"));
            }
        }

        return operationIds;
    }

    private void shutdownExecutorService(ExecutorService executorService) {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}