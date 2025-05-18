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
        html.append("<h1>BORME data query</h1>\n");
        html.append("<form id='bormeForm' method='post' action='/fetchBormeData'>\n");
        html.append("<div style='margin-top: 10px;'><label for='date'>Fecha:</label>\n");
        html.append("<input type='date' id='date' name='date'></div>\n");
        html.append("<div style='margin-top: 15px;'><button type='submit'>Consult data</button></div>\n");
        html.append("</form>\n");
        html.append("<form method='post' action='/runDailyFetcher' style='margin-top:30px;'>\n");
        html.append("<button type='submit' style='background-color:#007bff; color:white; padding:10px 20px; border:none; border-radius:5px;'>Run Fetch Borme Diary</button>\n");
        html.append("</form>\n");
        html.append(getHtmlFooter());

        output.setValue("html", html.toString());
        return null;
    }
}
