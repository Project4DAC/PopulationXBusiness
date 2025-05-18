package org.ulpgc.inefeeder.servicios.general.commands;


import org.ulpgc.inefeeder.servicios.general.Interfaces.*;

import static org.ulpgc.inefeeder.servicios.general.helpers.HtmlUtil.getHtmlFooter;
import static org.ulpgc.inefeeder.servicios.general.helpers.HtmlUtil.getHtmlHeader;

public class RenderResultCommand implements Command {
    private final Input input;
    private final Output output;

    public RenderResultCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        String function = input.getValue("function");
        String language = input.getValue("language");
        String url = input.getValue("generatedUrl");
        String jsonResponse = input.getValue("jsonResponse");

        StringBuilder html = new StringBuilder(getHtmlHeader());
        html.append("<h1>Resultado de la consulta: ").append(function).append("</h1>");

        if (language != null && !language.isEmpty()) {
            html.append("<p>Idioma: ").append(language).append("</p>");
        }

        if (url != null) {
            html.append("<h2>URL Generada:</h2><p>").append(url).append("</p>");

            if (jsonResponse != null) {
                html.append("<h2>Datos obtenidos:</h2>");
                html.append("<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow: auto; max-height: 500px;'>");
                html.append(jsonResponse);
                html.append("</pre>");
            }
        } else {
            html.append("<p>No query has been generated.</p>");
        }

        html.append("<p><a href='/'>Return</a></p>");
        html.append(getHtmlFooter());

        output.setValue("html", html.toString());
        return function;
    }
}
