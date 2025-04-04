package org.ulpgc.proyecto.servicios.inefeeder.commands.operations;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneOperacionesResponse;

public class ParseOperationCommand implements Command {
    private final Input input;
    private final Output output;
    private final Object jsonParser;
    private IneOperacionesResponse[] operaciones;

    public ParseOperationCommand(Input input, Output output, Object jsonParser) {
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
            throw new RuntimeException("Error executing ParseOperations command", e);
        }
    }

    private void input() {
        String jsonOperaciones = input.getValue("operationsJson");
        if (jsonOperaciones == null || jsonOperaciones.trim().isEmpty()) {
            throw new IllegalArgumentException("El JSON de operaciones no puede ser nulo o vacío.");
        }
    }

    private void process() {
        String jsonOperaciones = input.getValue("operationsJson");
        try {
            java.lang.reflect.Method fromJsonMethod = jsonParser.getClass().getMethod("fromJson", String.class, Class.class);
            operaciones = (IneOperacionesResponse[]) fromJsonMethod.invoke(jsonParser, jsonOperaciones, IneOperacionesResponse[].class);

            if (operaciones == null) {
                operaciones = new IneOperacionesResponse[0];
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    private void output() {
        output.setValue("operaciones", operaciones);
        output.setValue("operationCount", operaciones.length);
    }
}