
package org.ulpgc.business.query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.business.interfaces.Command;
import org.ulpgc.business.operations.DAO.DAOFactory;
import org.ulpgc.business.operations.DAO.IndicadorDAO;
import org.ulpgc.business.operations.POJO.Indicador;
import org.ulpgc.business.operations.POJO.Dato;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class INETableCommandFactory {
    public static Command createInitializeDatabaseCommand(Connection connection) {
        return () -> {
            IndicadorDAO indicadorDAO = DAOFactory.getInstance().getIndicadorDAO();
            indicadorDAO.setConnection(connection);

            // Parse JSON data and create Indicador objects
            JSONArray indicadoresArray = loadIndicadorData();

            List<Indicador> indicadores = new ArrayList<>();

            for (int i = 0; i < indicadoresArray.length(); i++) {
                JSONObject indicadorJson = indicadoresArray.getJSONObject(i);
                Indicador indicador = createIndicadorFromJson(indicadorJson);
                indicadores.add(indicador);
            }

            // Store indicadores in the database
            for (Indicador indicador : indicadores) {
                indicadorDAO.save(indicador);
            }
            return indicadoresArray.toString();
        };
    }
    private static JSONArray loadIndicadorData() {
        return new JSONArray();
    }
    private static Indicador createIndicadorFromJson(JSONObject indicadorJson) {
        Indicador indicador = new Indicador();

        // Set basic properties
        indicador.setCod(indicadorJson.getString("COD"));
        indicador.setNombre(indicadorJson.getString("Nombre"));
        indicador.setFkUnidad(indicadorJson.getInt("FK_Unidad"));
        indicador.setFkEscala(indicadorJson.getInt("FK_Escala"));

        // Process data points
        JSONArray dataArray = indicadorJson.getJSONArray("Data");
        List<Dato> datos = new ArrayList<>();

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject datoJson = dataArray.getJSONObject(i);
            Dato dato = new Dato();

            // Convert timestamp to Date
            long timestamp = datoJson.getLong("Fecha");
            dato.setFecha((Timestamp) new Date(timestamp));
            dato.setUnidad(datoJson.getDouble("Valor"));

            datos.add(dato);
        }

        indicador.setData(datos);
        return indicador;
    }

    // This method would load the JSON data from a file or other source
    // In a real application, this would likely read from a file or API

    // Additional method for updating indicator values
    public static Command createUpdateIndicadorCommand(Connection connection, String codigo, double nuevoValor) {
        return () -> {
            IndicadorDAO indicadorDAO = DAOFactory.getInstance().getIndicadorDAO();
            indicadorDAO.setConnection(connection);

            Indicador indicador = indicadorDAO.findByCod(codigo);
            if (indicador != null && !indicador.getData().isEmpty()) {
                // Update the most recent data point
                Dato datoMasReciente = indicador.getData().get(0);
                datoMasReciente.setUnidad(nuevoValor);
            }
            return "";
        };
    }
}
