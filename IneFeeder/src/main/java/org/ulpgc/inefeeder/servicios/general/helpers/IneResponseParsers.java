package org.ulpgc.inefeeder.servicios.general.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.ulpgc.inefeeder.servicios.POJO.*;
import org.ulpgc.inefeeder.servicios.general.Interfaces.ResponseParser;

import java.lang.reflect.Type;
import java.util.List;

public class IneResponseParsers {

    /**
     * Crea un parser para una respuesta de tipo Operacion
     * @return Parser para Operacion
     */
    public static ResponseParser<Operacion> createOperacionParser() {
        return new GsonResponseParser<>(Operacion.class);
    }

    /**
     * Crea un parser para una lista de Operaciones
     * @return Parser para List<Operacion>
     */
    public static ResponseParser<List<Operacion>> createOperacionesListParser() {
        Type listType = new TypeToken<List<Operacion>>(){}.getType();
        return new GsonResponseParser<>(listType);
    }

    /**
     * Crea un parser para una respuesta de tipo DatosTabla
     * @return Parser para DatosTabla
     */
    public static ResponseParser<DatosTabla> createDatosTablaParser() {
        return new GsonResponseParser<>(DatosTabla.class);
    }

    /**
     * Crea un parser para una lista de TablasOperacion
     * @return Parser para List<TablasOperacion>
     */
    public static ResponseParser<List<TablasOperacion>> createTablasOperacionListParser() {
        Type listType = new TypeToken<List<TablasOperacion>>(){}.getType();
        return new GsonResponseParser<>(listType);
    }

    /**
     * Crea un parser para una respuesta de tipo MetadatosTabla
     * @return Parser para MetadatosTabla
     */
    public static ResponseParser<MetadatosTabla> createMetadatosTablaParser() {
        return new GsonResponseParser<>(MetadatosTabla.class);
    }

    /**
     * Implementación genérica de ResponseParser utilizando Gson
     * @param <T> Tipo de dato a parsear
     */
    private static class GsonResponseParser<T> implements ResponseParser<T> {
        private final Gson gson;
        private final Type typeOfT;

        public GsonResponseParser(Type typeOfT) {
            this.typeOfT = typeOfT;
            this.gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
        }

        public GsonResponseParser(Class<T> classOfT) {
            this.typeOfT = classOfT;
            this.gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
        }

        @Override
        public T parse(String jsonData) throws Exception {
            try {
                return gson.fromJson(jsonData, typeOfT);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing JSON to " + typeOfT.getTypeName(), e);
            }
        }
    }
}