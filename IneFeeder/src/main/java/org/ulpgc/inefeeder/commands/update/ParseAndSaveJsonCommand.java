package org.ulpgc.inefeeder.commands.update;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;

public class ParseAndSaveJsonCommand implements Command {
    private final Input input;
    private final Output output;
    private final String functionName;

    public ParseAndSaveJsonCommand(Input input, Output output, String functionName) {
        this.input = input;
        this.output = output;
        this.functionName = functionName;
    }

    @Override
    public String execute() {
        String jsonResponse = input.getValue("jsonResponse");
        Gson gson = new Gson();
        JsonArray response = gson.fromJson(jsonResponse, JsonArray.class);

        for (JsonElement element : response) {
            if (element.isJsonObject()) {
                JsonObject objetoIndividual = element.getAsJsonObject();
                Command saveCommand = new SaveDataCommand(input, output, functionName, objetoIndividual);
                saveCommand.execute();
            }
        }
        return jsonResponse;
    }
}