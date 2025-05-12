package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Factory class for creating DAO instances with properly configured Gson
 */
public class DAOFactory {
    private static DAOFactory instance;
    private final Gson gson;
    
    private final OperacionDAO operacionDAO;
    private final TablaDAO tablaDAO;
    private final VariableDAO variableDAO;
    private final FrecuenciaDAO frecuenciaDAO;

    private DAOFactory() {
        // Set up Gson with custom type adapters for problematic fields
        this.gson = new GsonBuilder()
                .registerTypeAdapter(JsonArray.class, new JsonArrayAdapter())
                .create();
                
        // Initialize DAOs
        this.variableDAO = new VariableDAO(gson);
        this.operacionDAO = new OperacionDAO(gson);
        this.tablaDAO = new TablaDAO(gson, variableDAO);
        this.frecuenciaDAO = new FrecuenciaDAO(gson);
    }
    
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }
    
    public OperacionDAO getOperacionDAO() {
        return operacionDAO;
    }
    
    public TablaDAO getTablaDAO() {
        return tablaDAO;
    }
    
    public VariableDAO getVariableDAO() {
        return variableDAO;
    }
    
    public FrecuenciaDAO getFrecuenciaDAO() {
        return frecuenciaDAO;
    }
    
    public Gson getGson() {
        return gson;
    }

    // Custom adapter for JsonArray - converts primitives and objects to arrays when needed
    private static class JsonArrayAdapter implements JsonDeserializer<JsonArray> {
        @Override
        public JsonArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonArray()) {
                return json.getAsJsonArray();
            } else {
                // Convert non-array to a single-element array
                JsonArray array = new JsonArray();
                if (!json.isJsonNull()) {
                    array.add(json);
                }
                return array;
            }
        }
    }
}