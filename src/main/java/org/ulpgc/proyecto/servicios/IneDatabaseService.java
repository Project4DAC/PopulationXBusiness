package org.ulpgc.proyecto.servicios;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class IneDatabaseService implements AutoCloseable {
    private final IneApiClient ineClient;
    private final Gson gson;
    private final HikariDataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(IneDatabaseService.class);
    private static final int BATCH_SIZE = 1000;
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static final String INSERT_OPERACION_QUERY = "INSERT OR REPLACE INTO operaciones (Id, Cod_IOE, nombre, Codigo, url_operacion) VALUES (?,?,?,?,?)";
    private static final String INSERT_TABLA_QUERY = "INSERT OR IGNORE INTO tablas " +
            "(id, nombre, codigo, periodicidad, publicacion, periodo_ini, anyo_periodo_ini, fecha_ref_fin, ultima_modificacion) " +
            "VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_TABLA_OPERACIONES_QUERY = "INSERT OR IGNORE INTO tabla_operaciones (tabla_id, operacion_id) VALUES (?, ?)";

    public IneDatabaseService() {
        this.ineClient = new IneApiClient();
        this.gson = new Gson();
        this.dataSource = createConnectionPool();
    }

    private HikariDataSource createConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:ine_data.db?journal_mode=WAL&busy_timeout=30000&cache=shared&mode=serialized");
        config.setMaximumPoolSize(CORE_POOL_SIZE);
        config.setMinimumIdle(CORE_POOL_SIZE / 2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        return new HikariDataSource(config);
    }

    public void procesarYGuardarOperaciones() throws IOException, InterruptedException {
        try {
            String jsonOperaciones = ineClient.getOperacionesDisponibles(IneApiClient.IneLanguage.ES, new HashMap<>());
            IneOperacionesResponse[] operaciones = validateAndParseOperaciones(jsonOperaciones);

            try (Connection conn = dataSource.getConnection()) {
                createOperacionesTable(conn);
                saveOperacionesWithBatchProcessing(conn, operaciones);
            }
        } catch (SQLException e) {
            logger.error("Error processing and saving operations", e);
            throw new RuntimeException("Failed to process and save operations", e);
        }
    }

    private IneOperacionesResponse[] validateAndParseOperaciones(String jsonOperaciones) {
        if (jsonOperaciones == null || jsonOperaciones.trim().isEmpty()) {
            logger.warn("Received empty operations JSON");
            return new IneOperacionesResponse[0];
        }
        return gson.fromJson(jsonOperaciones, IneOperacionesResponse[].class);
    }

    private void saveOperacionesWithBatchProcessing(Connection conn, IneOperacionesResponse[] operaciones) throws SQLException {
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_OPERACION_QUERY)) {
            int newOperationsCount = 0;

            for (int i = 0; i < operaciones.length; i++) {
                IneOperacionesResponse op = operaciones[i];
                prepareOperacionStatement(pstmt, op);
                pstmt.addBatch();
                newOperationsCount++;

                if ((i + 1) % BATCH_SIZE == 0 || i == operaciones.length - 1) {
                    pstmt.executeBatch();
                }
            }

            conn.commit();
            logger.info("Operations added: {}", newOperationsCount);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }

    public void procesarYGuardarTablas() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(CORE_POOL_SIZE);

        try (Connection conn = dataSource.getConnection()) {
            createTablasAndRelationTables(conn);

            List<Integer> operationIds = obtenerIdsOperaciones(conn);

            List<CompletableFuture<Void>> futures = operationIds.stream()
                    .map(operacionId -> CompletableFuture.runAsync(() -> {
                        try {
                            procesarTablaParaOperacion(operacionId);
                        } catch (IOException | SQLException e) {
                            logger.error("Error processing operation {}: {}", operacionId, e.getMessage(), e);
                        }
                    }, executorService))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (SQLException e) {
            logger.error("Error processing tables", e);
        } finally {
            shutdownExecutorService(executorService);
        }
    }

    private void procesarTablaParaOperacion(Integer operacionId) throws IOException, SQLException {
        if (operacionId == null) {
            logger.warn("Skipping null operation ID");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            Map<String, String> params = new HashMap<>();
            String tablesJson = ineClient.getTablasOperacion(
                    IneApiClient.IneLanguage.ES,
                    operacionId.toString(),
                    params
            );

            IneTablaResponse[] tablas = validateAndParseTablas(tablesJson);
            if (tablas == null || tablas.length == 0) {
                logger.info("No tables found for operation {}", operacionId);
                return;
            }

            conn.setAutoCommit(false);

            try (PreparedStatement pstmtTablas = conn.prepareStatement(INSERT_TABLA_QUERY);
                 PreparedStatement pstmtRelations = conn.prepareStatement(INSERT_TABLA_OPERACIONES_QUERY)) {

                int tablesProcessed = processBatchTablas(pstmtTablas, pstmtRelations, tablas, operacionId);

                conn.commit();
                logger.info("Operation {} - Tables processed: {}", operacionId, tablesProcessed);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private IneTablaResponse[] validateAndParseTablas(String tablesJson) {
        if (tablesJson == null || tablesJson.trim().isEmpty()) {
            logger.warn("Received empty tables JSON");
            return new IneTablaResponse[0];
        }
        return gson.fromJson(tablesJson, IneTablaResponse[].class);
    }

    private int processBatchTablas(PreparedStatement pstmtTablas, PreparedStatement pstmtRelations,
                                   IneTablaResponse[] tablas, Integer operacionId) throws SQLException {
        int tablesProcessed = 0;

        for (IneTablaResponse tabla : tablas) {
            if (tabla.getId() == null) continue;

            prepareTablaStatement(pstmtTablas, tabla);
            pstmtTablas.addBatch();
            tablesProcessed++;

            pstmtRelations.setInt(1, tabla.getId());
            pstmtRelations.setInt(2, operacionId);
            pstmtRelations.addBatch();

            if (tablesProcessed % BATCH_SIZE == 0) {
                pstmtTablas.executeBatch();
                pstmtRelations.executeBatch();
            }
        }

        pstmtTablas.executeBatch();
        pstmtRelations.executeBatch();

        return tablesProcessed;
    }

    private void createOperacionesTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS operaciones (" +
                    "id INT PRIMARY KEY, " +
                    "COD_IOE TEXT," +
                    "nombre TEXT, " +
                    "codigo TEXT," +
                    "url_operacion TEXT)");
        }
    }

    private void createTablasAndRelationTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tablas (" +
                    "id INTEGER PRIMARY KEY, " +
                    "nombre TEXT, " +
                    "codigo TEXT, " +
                    "periodicidad INTEGER, " +
                    "publicacion INTEGER, " +
                    "periodo_ini INTEGER, " +
                    "anyo_periodo_ini TEXT, " +
                    "fecha_ref_fin TEXT, " +
                    "ultima_modificacion BIGINT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS tabla_operaciones (" +
                    "tabla_id INTEGER, " +
                    "operacion_id INTEGER, " +
                    "PRIMARY KEY(tabla_id, operacion_id), " +
                    "FOREIGN KEY(tabla_id) REFERENCES tablas(id), " +
                    "FOREIGN KEY(operacion_id) REFERENCES operaciones(id))");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tabla_operaciones " +
                    "ON tabla_operaciones(tabla_id, operacion_id)");
        }
    }

    private void prepareOperacionStatement(PreparedStatement pstmt, IneOperacionesResponse op) throws SQLException {
        pstmt.setInt(1, op.getId());
        pstmt.setString(2, op.getCod_IOE() != null ? op.getCod_IOE() : "");
        pstmt.setString(3, op.getNombre() != null ? op.getNombre() : "");
        pstmt.setString(4, op.getCodigo() != null ? op.getCodigo() : "");
        pstmt.setString(5, op.getUrl() != null ? op.getUrl() : "");
    }

    private void prepareTablaStatement(PreparedStatement pstmt, IneTablaResponse tabla) throws SQLException {
        pstmt.setInt(1, tabla.getId());
        pstmt.setString(2, tabla.getNombre() != null ? tabla.getNombre() : "");
        pstmt.setString(3, tabla.getCodigo() != null ? tabla.getCodigo() : "");

        if (tabla.getFK_Periodicidad() != null)
            pstmt.setInt(4, tabla.getFK_Periodicidad());
        else
            pstmt.setNull(4, Types.INTEGER);

        if (tabla.getFK_Publicacion() != null)
            pstmt.setInt(5, tabla.getFK_Publicacion());
        else
            pstmt.setNull(5, Types.INTEGER);

        if (tabla.getFK_Periodo_ini() != null)
            pstmt.setInt(6, tabla.getFK_Periodo_ini());
        else
            pstmt.setNull(6, Types.INTEGER);

        pstmt.setString(7, tabla.getAnyo_Periodo_ini());
        pstmt.setString(8, tabla.getFechaRef_fin());

        if (tabla.getUltima_Modificacion() != null)
            pstmt.setLong(9, tabla.getUltima_Modificacion());
        else
            pstmt.setNull(9, Types.BIGINT);
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

    @Override
    public void close() {
        if (dataSource != null) {
            try {
                dataSource.close();
                logger.info("HikariCP connection pool closed successfully");
            } catch (Exception e) {
                logger.error("Error closing HikariCP connection pool", e);
            }
        }
    }

    private void shutdownExecutorService(ExecutorService executorService) {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.info("ExecutorService shutdown forced");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}