package org.ulpgc.BormeFeeder.commands.query;

import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;
import org.ulpgc.BormeFeeder.services.general.helpers.SimpleOutput;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FetchDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final HttpClient httpClient;

    public FetchDataCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String execute() {
        try {
            String url = input.getValue("url");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error al realizar la consulta: Código " + response.statusCode());
            }

            String jsonResponse = response.body();
            output.setValue("url", url);
            output.setValue("jsonResponse", jsonResponse);
            output.setValue("date", input.getValue("date"));
            System.out.println((String) output.getValue("jsonResponse"));
            return jsonResponse;
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar la consulta: " + e.getMessage(), e);
        }
    }
}