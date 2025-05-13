package org.ulpgc.business.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ulpgc.business.Exceptions.DatabaseException;
import org.ulpgc.business.operations.DAO.DAOFactory;
import org.ulpgc.business.operations.DAO.FrecuenciaDAO;
import org.ulpgc.business.operations.DAO.OperacionDAO;
import org.ulpgc.business.operations.DAO.TablaDAO;
import org.ulpgc.business.operations.POJO.Operacion;
import org.ulpgc.business.operations.DAO.DatabaseConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

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
     * @param operacionCodigo The operation code to fetch
     * @return The fetched and saved operation
     */
    public Optional<Operacion> fetchAndSaveOperacion(String operacionCodigo) {
        try {
            // Fetch operation data
            String url = BASE_URL + API_VERSION + "/operaciones/" + operacionCodigo;
            String jsonResponse = fetchDataFromAPI(url);
            
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
        }
    }
    
    /**
     * Fetch tables for a specific operation and save them to the database
     * @param operacionCodigo The operation code
     * @return Number of tables fetched and saved
     */
    public int fetchAndSaveTablasOperacion(String operacionCodigo) {
        try {
            // First ensure we have the operation
            Optional<Operacion> operacion = fetchAndSaveOperacion(operacionCodigo);
            
            if (!operacion.isPresent()) {
                return 0;
            }
            
            int operacionId = operacion.get().getId();
            
            // Fetch tables data
            String url = BASE_URL + API_VERSION + "/operaciones/" + operacionCodigo + "/tablas";
            String jsonResponse = fetchDataFromAPI(url);
            
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return 0;
            }
            
            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // Get a database connection
            Connection conn = DatabaseConnectionManager.getConnection();
            
            try {
                // Save tables data
                TablaDAO tablaDAO = daoFactory.getTablaDAO();
                tablaDAO.saveTablasOperacion(conn, jsonObject, operacionId);
                
                // Commit the transaction
                DatabaseConnectionManager.commitTransaction();
                
                // Count the number of tables saved
                int count = 0;
                if (jsonObject.has("datos")) {
                    count = jsonObject.getAsJsonArray("datos").size();
                } else {
                    // Try to find the array in the JSON object
                    for (String key : jsonObject.keySet()) {
                        if (jsonObject.get(key).isJsonArray()) {
                            count = jsonObject.getAsJsonArray(key).size();
                            break;
                        }
                    }
                }
                
                return count;
                
            } catch (SQLException e) {
                DatabaseConnectionManager.rollbackTransaction();
                throw new DatabaseException("Error saving tables data", e);
            }
        } catch (IOException e) {
            throw new DatabaseException("Error fetching tables data from API", e);
        }
    }
    
    /**
     * Fetch detailed data for a specific table and save it to the database
     * @param tablaId The table ID
     * @return true if successful, false otherwise
     */
    public boolean fetchAndSaveDatosTabla(int tablaId) {
        try {
            // Fetch table data
            String url = BASE_URL + API_VERSION + "/datos/tabla/" + tablaId;
            String jsonResponse = fetchDataFromAPI(url);
            
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return false;
            }
            
            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // Get a database connection
            Connection conn = DatabaseConnectionManager.getConnection();
            
            try {
                // Save table data
                TablaDAO tablaDAO = daoFactory.getTablaDAO();
                tablaDAO.saveDatosTabla(conn, jsonObject);
                
                // Commit the transaction
                DatabaseConnectionManager.commitTransaction();
                
                return true;
                
            } catch (SQLException e) {
                DatabaseConnectionManager.rollbackTransaction();
                throw new DatabaseException("Error saving table data", e);
            }
        } catch (IOException e) {
            throw new DatabaseException("Error fetching table data from API", e);
        }
    }
    
    /**
     * Fetch and save frequency information
     * @return true if successful, false otherwise
     */
    public boolean fetchAndSaveFrecuencias() {
        try {
            // Fetch frequencies data
            String url = BASE_URL + API_VERSION + "/frecuencias";
            String jsonResponse = fetchDataFromAPI(url);
            
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return false;
            }
            
            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // Get a database connection
            Connection conn = DatabaseConnectionManager.getConnection();
            
            try {
                // Save frequencies data
                FrecuenciaDAO frecuenciaDAO = daoFactory.getFrecuenciaDAO();
                frecuenciaDAO.saveFrecuencias(conn, jsonObject);
                
                // Commit the transaction
                DatabaseConnectionManager.commitTransaction();
                
                return true;
                
            } catch (SQLException e) {
                DatabaseConnectionManager.rollbackTransaction();
                throw new DatabaseException("Error saving frequencies data", e);
            }
        } catch (IOException e) {
            throw new DatabaseException("Error fetching frequencies data from API", e);
        }
    }
    
    /**
     * Helper method to fetch data from the API
     * @param urlString The URL to fetch
     * @return The response as a string
     */
    private String fetchDataFromAPI(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        
        int responseCode = connection.getResponseCode();
        
        if (responseCode != 200) {
            System.out.println("API request failed with response code: " + responseCode);
            return null;
        }
        
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        
        in.close();
        connection.disconnect();
        
        return content.toString();
    }
}