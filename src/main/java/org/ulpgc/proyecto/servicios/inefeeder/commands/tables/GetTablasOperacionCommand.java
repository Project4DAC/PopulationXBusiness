package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.*;
import org.ulpgc.proyecto.servicios.inefeeder.commands.query.BuildURLCommand;
import org.ulpgc.proyecto.servicios.inefeeder.commands.json.FetchJsonCommand;

import java.net.http.HttpClient;

public class GetTablasOperacionCommand implements Command {
    private final Input input;
    private final Output output;
    private final HttpClient httpClient;

    public GetTablasOperacionCommand(Input input, Output output, HttpClient httpClient) {
        this.input = input;
        this.output = output;
        this.httpClient = httpClient;
    }

    @Override
    public String execute() {
        try {
            input();
            process();
            output();
            return output.result();
        } catch (Exception e) {
            throw new RuntimeException("Error executing GetTablasOperacion command", e);
        }
    }

    private void input() {
        String language = input.getValue("language");
        String operacionId = input.getValue("operacionId");

        if (language == null || language.isEmpty()) {
            throw new IllegalArgumentException("El parámetro language no puede ser nulo o vacío.");
        }

        if (operacionId == null || operacionId.isEmpty()) {
            throw new IllegalArgumentException("El parámetro operacionId no puede ser nulo o vacío.");
        }
    }

    private void process() {
        Input buildUrlInput = new SimpleInput();
        Output buildUrlOutput = new SimpleOutput();

        buildUrlInput.setValue("language", input.getValue("language"));
        buildUrlInput.setValue("function", "TABLAS_OPERACION");
        buildUrlInput.setValue("inputValue", input.getValue("operacionId"));
        buildUrlInput.setValue("params", input.getValue("params"));

        Command buildUrlCommand = new BuildURLCommand(buildUrlInput, buildUrlOutput);
        buildUrlCommand.execute();

        Input fetchJsonInput = new SimpleInput();
        Output fetchJsonOutput = new SimpleOutput();

        fetchJsonInput.setValue("url", buildUrlOutput.getValue("url"));

        Command fetchJsonCommand = new FetchJsonCommand(fetchJsonInput, fetchJsonOutput, httpClient);
        fetchJsonCommand.execute();

        input.setValue("jsonResponse", fetchJsonOutput.getValue("jsonResponse"));
    }

    private void output() {
        output.setValue("jsonResponse", input.getValue("jsonResponse"));
    }
}