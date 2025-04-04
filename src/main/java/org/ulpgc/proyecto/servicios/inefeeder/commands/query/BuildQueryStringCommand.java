package org.ulpgc.proyecto.servicios.inefeeder.commands.query;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildQueryStringCommand implements Command {
    private final Input input;
    private final Output output;
    private String queryString;

    public BuildQueryStringCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        process();
        output();
        return output.result();
    }


    private void process() {
        Map<String, String> params = input.getValue("params");

        if (params == null || params.isEmpty()) {
            queryString = "";
            return;
        }

        queryString = params.entrySet().stream()
                .map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al codificar parámetros de la URL", e);
                    }
                })
                .collect(Collectors.joining("&"));
    }

    private void output() {
        output.setValue("queryString", queryString);
    }
}