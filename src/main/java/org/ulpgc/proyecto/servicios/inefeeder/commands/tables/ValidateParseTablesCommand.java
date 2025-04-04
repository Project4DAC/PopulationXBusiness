package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneTablaResponse;


public class ValidateParseTablesCommand implements Command {
    private final Input input;
    private final Output output;
    private final Object jsonParser;

    public ValidateParseTablesCommand(Input input, Output output, Object jsonParser) {
        this.input = input;
        this.output = output;
        this.jsonParser = jsonParser;
    }

    @Override
    public String execute() {
        String tablesJson = input.getValue("tablesJson");

        if (tablesJson == null || tablesJson.trim().isEmpty()) {
            output.setValue("tablas", new IneTablaResponse[0]);
            return "Empty tables JSON received";
        }

        try {
            java.lang.reflect.Method fromJsonMethod = jsonParser.getClass().getMethod("fromJson", String.class, Class.class);
            IneTablaResponse[] tablas = (IneTablaResponse[]) fromJsonMethod.invoke(jsonParser, tablesJson, IneTablaResponse[].class);

            output.setValue("tablas", tablas);
            return "Successfully parsed tables JSON";
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse tables JSON", e);
        }
    }
}