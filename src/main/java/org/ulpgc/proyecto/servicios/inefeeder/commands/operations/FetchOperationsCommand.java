package org.ulpgc.proyecto.servicios.inefeeder.commands.operations;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.Map;

public class FetchOperationsCommand implements Command {
    private final Input input;
    private final Output output;
    private final HttpClient httpClient;
    private String jsonResponse;

    public FetchOperationsCommand(Input input, Output output, HttpClient httpClient) {
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
            throw new RuntimeException("Error executing FetchOperations command", e);
        }
    }

    private void input() {
        String baseUrl = input.getValue("baseUrl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("La URL base no puede ser nula o vacía.");
        }
    }

    private void process() throws IOException, InterruptedException {
        String baseUrl = input.getValue("baseUrl");
        String language = input.getValue("language");
        if (language == null) language = "es";

        String url = baseUrl + "/operaciones?lang=" + language.toLowerCase();

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
            throw new IOException("Error fetching operations from API: " + response.statusCode());
        }

        jsonResponse = response.body();
    }

    private void output() {
        output.setValue("operationsJson", jsonResponse);
    }
}