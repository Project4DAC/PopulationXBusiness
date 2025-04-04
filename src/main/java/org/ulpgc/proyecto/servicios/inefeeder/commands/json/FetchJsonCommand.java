package org.ulpgc.proyecto.servicios.inefeeder.commands.json;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FetchJsonCommand implements Command {
    private final Input input;
    private final Output output;
    private String jsonResponse;
    private final HttpClient httpClient;

    public FetchJsonCommand(Input input, Output output, HttpClient httpClient) {
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
            throw new RuntimeException("Error executing FetchJson command", e);
        }
    }

    private void input() {
        String url = input.getValue("url");
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("La URL no puede ser nula o vacía.");
        }
    }

    private void process() throws IOException, InterruptedException {
        String url = input.getValue("url");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error fetching data from INE API: " + response.statusCode());
        }

        jsonResponse = response.body();
    }

    private void output() {
        output.setValue("jsonResponse", jsonResponse);
    }
}