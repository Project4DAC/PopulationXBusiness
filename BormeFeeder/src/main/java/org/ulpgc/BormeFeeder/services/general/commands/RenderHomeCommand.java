package org.ulpgc.BormeFeeder.services.general.commands;


import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;

import static org.ulpgc.BormeFeeder.services.general.helpers.HtmlUtil.getHtmlFooter;
import static org.ulpgc.BormeFeeder.services.general.helpers.HtmlUtil.getHtmlHeader;

public class RenderHomeCommand implements Command {
    private final Input input;
    private final Output output;

    public RenderHomeCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }


    @Override
    public String execute() {
        StringBuilder html = new StringBuilder(getHtmlHeader());
        html.append("<h1>Consulta de datos del BORME</h1>\n");
        html.append("<form id='bormeForm' method='post' action='/fetchBormeData'>\n");
        html.append("<div style='margin-top: 10px;'><label for='date'>Fecha:</label>\n");
        html.append("<input type='date' id='date' name='date'></div>\n");
        html.append("<div style='margin-top: 15px;'><button type='submit'>Consultar datos</button></div>\n");
        html.append("</form>\n");
        html.append(getHtmlFooter());

        output.setValue("html", html.toString());
        return null;
    }
}
