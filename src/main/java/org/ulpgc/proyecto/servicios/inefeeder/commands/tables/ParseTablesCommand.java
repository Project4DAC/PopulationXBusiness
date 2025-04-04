package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneTablaResponse;

public class ParseTablesCommand implements Command {
    private final Input input;
    private final Output output;
    private final Object jsonParser;
    private IneTablaResponse[] tablas;

    public ParseTablesCommand(Input input, Output output, Object jsonParser) {
        this.input = input;
        this.output = output;
        this.jsonParser = jsonParser;
    }

    @Override
    public String execute() {
        try {
            input();
            process();
            output();
            return output.result();
        } catch (Exception e) {
            throw new RuntimeException("Error executing ParseTables command", e);
        }
    }

    private void input() {
        String jsonTablas = input.getValue("tablesJson");
        if (jsonTablas == null || jsonTablas.trim().isEmpty()) {
            throw new IllegalArgumentException("El JSON de tablas no puede ser nulo o vacío.");
        }

        Integer operationId = input.getValue("operationId");
        if (operationId == null) {
            throw new IllegalArgumentException("El ID de operación no puede ser nulo.");
        }
    }

    private void process() {
        String jsonTablas = input.getValue("tablesJson");

        try {
            java.lang.reflect.Method fromJsonMethod = jsonParser.getClass().getMethod("fromJson", String.class, Class.class);
            tablas = (IneTablaResponse[]) fromJsonMethod.invoke(jsonParser, jsonTablas, IneTablaResponse[].class);

            if (tablas == null) {
                tablas = new IneTablaResponse[0];
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing tables JSON", e);
        }
    }

    private void output() {
        output.setValue("tablas", tablas);
        output.setValue("tablasCount", tablas.length);
        output.setValue("operationId", input.getValue("operationId"));
    }
}