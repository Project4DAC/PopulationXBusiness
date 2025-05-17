package org.ulpgc.business.operations.DAO;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Custom adapter for JsonArray to handle cases where a single element or null is provided
 * instead of an array
 */
public class JsonArrayAdapter implements JsonDeserializer<JsonArray> {
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