package org.ulpgc.BormeFeeder.commands.query;

import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;

;

public class BuildURLCommand implements Command {
    private static final String BASE_URL = "https://www.boe.es/datosabiertos/api/borme/sumario/";
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
        String date = input.getValue("date");

        if (date == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
    }

    private void process() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append(((String) input.getValue("date")).replaceAll("-", ""));

        String inputDate = input.getValue("inputDate");
        if (inputDate != null && !inputDate.isEmpty()) {
            urlBuilder.append("/").append(inputDate);
        }

        this.url = urlBuilder.toString();
    }

    private void output() {
        output.setValue("url", this.url);
    }

}