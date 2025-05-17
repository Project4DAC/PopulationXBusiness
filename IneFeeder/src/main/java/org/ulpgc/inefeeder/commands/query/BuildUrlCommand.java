package org.ulpgc.inefeeder.commands.query;

import org.ulpgc.inefeeder.servicios.general.Interfaces.Command;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;

public class BuildUrlCommand implements Command {
    private static final String BaseUrl = "https://servicios.ine.es/wstempus/js/";
    private final Input input;
    private final Output output;
    private String url;

    public BuildUrlCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        try {
            input();    // Validar los parámetros de entrada
            process();  // Construir la URL
            output();   // Establecer la URL en el output
            return output.result();
        } catch (Exception e) {
            output.setResponse(422, "Unprocessable Entity");
            return output.errorCode();
        }
    }

    private void input() {
        String language = input.getValue("language");
        String function = input.getValue("function");

        if (language == null || function == null) {
            throw new IllegalArgumentException("Los parámetros language y function no pueden ser nulos.");
        }
    }

    private void process() {
        StringBuilder urlBuilder = new StringBuilder(BaseUrl);

        // Agregar el idioma y la función a la ruta
        urlBuilder.append((String) input.getValue("language")).append("/");
        urlBuilder.append((String) input.getValue("function"));

        // Si hay un valor de entrada, agregarlo a la URL como parte de la ruta
        String inputValue = input.getValue("inputValue");
        if (inputValue != null && !inputValue.isEmpty()) {
            // Este valor se usa en la ruta directamente
            urlBuilder.append("/").append(inputValue);
        }

        // Agregar los parámetros a la query string si es necesario
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

        // Construir los parámetros de la query string
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
