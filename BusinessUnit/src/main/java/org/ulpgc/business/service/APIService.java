package org.ulpgc.business.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ulpgc.business.Exceptions.DatabaseException;
import org.ulpgc.business.operations.DAO.DAOFactory;
import org.ulpgc.business.operations.DAO.OperacionDAO;
import org.ulpgc.business.operations.POJO.Dato;
import org.ulpgc.business.operations.POJO.Indicador;
import org.ulpgc.business.operations.POJO.Operacion;
import org.ulpgc.business.operations.DAO.DatabaseConnectionManager;

import java.net.URI;
import java.net.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.io.FileWriter;

/**
 * Service for fetching and processing data from the API
 */
public class APIService {
    private static final String BASE_URL = "https://servicios.ine.es/wstempus/js/";
    private static final String API_VERSION = "ES";
    
    private final DAOFactory daoFactory;
    private final Gson gson;
    
    public APIService() {
        this.daoFactory = DAOFactory.getInstance();
        this.gson = daoFactory.getGson();
    }
    
    /**
     * Fetch data for a specific operation and save it to the database
     * @return The fetched and saved operation
     */
    public List<Indicador> getAllIndicators() {
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            return daoFactory.getIndicadorDAO().findAll(conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all indicators", e);
        }
    }

    public List<Dato> getIndicatorData(String code) {
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            return daoFactory.getDatoDAO().findByIndicadorCodigo(conn, code);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching data for indicator: " + code, e);
        }
    }

    public List<Dato> getIndicatorDataFiltered(String code, int startYear, int endYear) {
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            return daoFactory.getDatoDAO().findByIndicadorAndYearRange(conn, code, startYear, endYear);
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering data for indicator: " + code, e);
        }
    }

    public Optional<Indicador> getIndicatorMetadata(String code) {
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            return Optional.ofNullable(daoFactory.getIndicadorDAO().findByCod(conn, code));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching metadata for indicator: " + code, e);
        }
    }

    public boolean exportIndicatorDataToCSV(String code) {
        List<Dato> data = getIndicatorData(code);
        if (data.isEmpty()) return false;

        String fileName = "indicador_" + code + ".csv";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Año,Valor,Unidad\n");
            for (Dato d : data) {
                writer.write(d.getFecha() + "," + d.getValor() + "," + d.getUnidad() + "\n");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int fetchAndSaveTablasOperacion(String codigoOperacion) {
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            return daoFactory.getTablaDAO().fetchAndSaveByOperacionCodigo(conn, codigoOperacion);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching tables for operation: " + codigoOperacion, e);
        }
    }

    public boolean fetchAndSaveDatosTabla(int tablaId) {
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            return daoFactory.getDatoDAO().fetchAndSaveByTablaId(conn, tablaId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching data for table ID: " + tablaId, e);
        }
    }


    public Optional<Operacion> fetchAndSaveOperacion(String operacionCodigo) {
        try {
            // Fetch operation data
            String url = BASE_URL + API_VERSION + "/operaciones/" + operacionCodigo;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return Optional.empty();
            }

            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Get a database connection
            Connection conn = DatabaseConnectionManager.getConnection();

            try {
                // Save operation data
                OperacionDAO operacionDAO = daoFactory.getOperacionDAO();
                operacionDAO.saveOperacion(conn, jsonObject);

                // Get operation ID
                int operacionId = operacionDAO.findOperacionIdByCode(conn, operacionCodigo);

                if (operacionId == -1) {
                    throw new DatabaseException("Failed to retrieve operacion ID after saving");
                }

                // Commit the transaction
                DatabaseConnectionManager.commitTransaction();

                // Return the operation
                Operacion operacion = gson.fromJson(jsonObject, Operacion.class);
                return Optional.of(operacion);

            } catch (SQLException e) {
                DatabaseConnectionManager.rollbackTransaction();
                throw new DatabaseException("Error saving operation data", e);
            }
        } catch (IOException e) {
            throw new DatabaseException("Error fetching operation data from API", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}