package org.ulpgc.BormeFeeder.services.general.commands;


import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;

import static org.ulpgc.BormeFeeder.services.general.helpers.HtmlUtil.getHtmlFooter;
import static org.ulpgc.BormeFeeder.services.general.helpers.HtmlUtil.getHtmlHeader;

public class RenderResultCommand implements Command {
    private final Input input;
    private final Output output;

    public RenderResultCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        String function = input.getValue("date");
        String url = input.getValue("url");
        Object jsonResponse = input.getValue("jsonResponse");
        System.out.println("jsonResponse: " + jsonResponse);
        System.out.println("url: " + url);
        StringBuilder html = new StringBuilder(getHtmlHeader());
        html.append("<h1>Resultado de la consulta: ").append(function).append("</h1>");

        if (url != null) {
            html.append("<h2>URL Generada:</h2><p>").append(url).append("</p>");

            if (jsonResponse != null) {
                html.append("<h2>Datos obtenidos:</h2>");
                html.append("<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow: auto; max-height: 500px;'>");
                html.append(jsonResponse);
                html.append("</pre>");
            }
        } else {
            html.append("<p>No se ha generado ninguna consulta.</p>");
        }

        html.append("<p><a href='/'>Volver al inicio</a></p>");
        html.append(getHtmlFooter());

        output.setValue("html", html.toString());
        return function;
    }
}
