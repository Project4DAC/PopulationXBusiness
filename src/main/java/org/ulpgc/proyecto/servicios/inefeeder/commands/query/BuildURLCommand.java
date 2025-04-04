package org.ulpgc.proyecto.servicios.inefeeder.commands.query;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

public class BuildURLCommand implements Command {
    private static final String BASE_URL = "https://servicios.ine.es/wstempus/js/";
    private final Input input;
    private final Output output;
    private String url;

    public BuildURLCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        input();
        process();
        output();
        return output.result();
    }

    private void input() {
        String language = input.getValue("language");
        String function = input.getValue("function");

        if (language == null || function == null) {
            throw new IllegalArgumentException("Los parámetros language y function no pueden ser nulos.");
        }
    }

    private void process() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        urlBuilder.append((String) input.getValue("language")).append("/");
        urlBuilder.append((String) input.getValue("function"));

        String inputValue = input.getValue("inputValue");
        if (inputValue != null && !inputValue.isEmpty()) {
            urlBuilder.append("/").append(inputValue);
        }

        java.util.Map<String, String> params = input.getValue("params");
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?").append(buildQueryString(params));
        }

        this.url = urlBuilder.toString();
    }

    private void output() {
        output.setValue("url", this.url);
    }

    private String buildQueryString(java.util.Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;

        for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        return queryString.toString();
    }
}