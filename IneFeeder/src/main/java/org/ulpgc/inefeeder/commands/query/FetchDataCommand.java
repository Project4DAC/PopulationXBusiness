package main.java.org.ulpgc.inefeeder.commands.query;

import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;
import main.java.org.ulpgc.inefeeder.servicios.general.helpers.SimpleOutput;

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
            Output urlOutput = new SimpleOutput();
            String url = new BuildUrlCommand(input, urlOutput).execute();

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
            output.setValue("function", input.getValue("function"));
            output.setValue("language", input.getValue("language"));

            return jsonResponse;
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar la consulta: " + e.getMessage(), e);
        }
    }
}