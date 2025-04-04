package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class FetchTablesCommand implements Command {
    private final Input input;
    private final Output output;
    private final HttpClient httpClient;
    private String jsonResponse;

    public FetchTablesCommand(Input input, Output output, HttpClient httpClient) {
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
            throw new RuntimeException("Error executing FetchTables command", e);
        }
    }

    private void input() {
        String baseUrl = input.getValue("baseUrl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("La URL base no puede ser nula o vacía.");
        }

        Integer operationId = input.getValue("operationId");
        if (operationId == null) {
            throw new IllegalArgumentException("El ID de operación no puede ser nulo.");
        }
    }

    private void process() throws IOException, InterruptedException {
        String baseUrl = input.getValue("baseUrl");
        String language = input.getValue("language");
        if (language == null) language = "es";

        Integer operationId = input.getValue("operationId");

        String url = baseUrl + "/tablas?lang=" + language.toLowerCase() + "&operacionId=" + operationId;

        Map<String, String> params = input.getValue("params");
        if (params != null && !params.isEmpty()) {
            StringBuilder queryParams = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                queryParams.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            url += queryParams.toString();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error fetching tables from API: " + response.statusCode());
        }

        jsonResponse = response.body();
    }

    private void output() {
        output.setValue("tablesJson", jsonResponse);
        output.setValue("operationId", input.getValue("operationId"));
    }
}