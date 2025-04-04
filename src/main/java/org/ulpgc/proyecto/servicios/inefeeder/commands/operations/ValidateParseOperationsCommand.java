package org.ulpgc.proyecto.servicios.inefeeder.commands.operations;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneOperacionesResponse;

public class ValidateParseOperationsCommand implements Command {
    private final Input input;
    private final Output output;
    private final Object jsonParser;

    public ValidateParseOperationsCommand(Input input, Output output, Object jsonParser) {
        this.input = input;
        this.output = output;
        this.jsonParser = jsonParser;
    }

    @Override
    public String execute() {
        String jsonOperaciones = input.getValue("jsonOperaciones");

        if (jsonOperaciones == null || jsonOperaciones.trim().isEmpty()) {
            output.setValue("operaciones", new IneOperacionesResponse[0]);
            return "Empty operations JSON received";
        }

        try {
            java.lang.reflect.Method fromJsonMethod = jsonParser.getClass().getMethod("fromJson", String.class, Class.class);
            IneOperacionesResponse[] operaciones = (IneOperacionesResponse[]) fromJsonMethod.invoke(jsonParser, jsonOperaciones, IneOperacionesResponse[].class);

            output.setValue("operaciones", operaciones);
            return "Successfully parsed operations JSON";
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse operations JSON", e);
        }
    }
}