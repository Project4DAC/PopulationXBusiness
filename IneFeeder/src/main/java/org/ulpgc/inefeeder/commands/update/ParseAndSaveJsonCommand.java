package org.ulpgc.inefeeder.commands.update;

import com.google.gson.*;
import org.ulpgc.inefeeder.servicios.Command;
import org.ulpgc.inefeeder.servicios.Input;
import org.ulpgc.inefeeder.servicios.Output;

import java.lang.reflect.Type;

public class ParseAndSaveJsonCommand implements Command {
    private final Input input;
    private final Output output;
    private final String functionName;
    private final Gson gson;

    public ParseAndSaveJsonCommand(Input input, Output output, String functionName) {
        this.input = input;
        this.output = output;
        this.functionName = functionName;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(JsonArray.class, new SafeJsonArrayAdapter())
                .create();
    }

    private static class SafeJsonArrayAdapter implements JsonDeserializer<JsonArray> {
        @Override
        public JsonArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonArray()) {
                return json.getAsJsonArray();
            } else {
                JsonArray array = new JsonArray();
                if (!json.isJsonNull()) {
                    array.add(json);
                }
                return array;
            }
        }
    }

    @Override
    public String execute() {
        String jsonResponse = input.getValue("jsonResponse");

        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return "Empty JSON response";
        }

        try {
            // Intentar parsear como JsonElement (tanto JsonObject, JsonArray o JsonPrimitive)
            JsonElement jsonElement = JsonParser.parseString(jsonResponse);

            if (jsonElement.isJsonArray()) {
                // Si es un JsonArray, procesarlo como tal
                JsonArray response = jsonElement.getAsJsonArray();
                for (JsonElement element : response) {
                    if (element.isJsonObject()) {
                        JsonObject objetoIndividual = element.getAsJsonObject();
                        Command saveCommand = new SaveDataCommand(input, output, functionName, objetoIndividual);
                        saveCommand.execute();
                    }
                }
            } else if (jsonElement.isJsonPrimitive()) {
                // Si es un JsonPrimitive, procesarlo como tal (cadena, número, etc.)
                JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                String primitiveValue = jsonPrimitive.getAsString();  // O usa getAsInt(), getAsBoolean(), según corresponda
                return "Received primitive value: " + primitiveValue;
            } else if (jsonElement.isJsonObject()) {
                // Si es un JsonObject, procesarlo como tal
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Command saveCommand = new SaveDataCommand(input, output, functionName, jsonObject);
                saveCommand.execute();
            }

            return jsonResponse;

        } catch (JsonSyntaxException e) {
            // Si hay un error al intentar parsear el JSON, probablemente sea texto plano
            System.err.println("Failed to parse JSON response: " + e.getMessage());
            System.err.println("Response was: " + jsonResponse);

            // Devolver la respuesta como texto plano si no es un JSON válido
            return "Non-JSON response: " + jsonResponse;
        }
    }
}