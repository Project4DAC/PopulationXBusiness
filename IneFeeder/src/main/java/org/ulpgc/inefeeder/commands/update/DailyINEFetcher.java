package org.ulpgc.inefeeder.commands.update;

import org.ulpgc.inefeeder.commands.update.INEFetchDataCommand;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.helpers.SimpleInput;
import org.ulpgc.inefeeder.servicios.general.helpers.SimpleOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DailyINEFetcher implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(DailyINEFetcher.class.getName());

    @Override
    public void run() {
        try {
            LOGGER.info("Starting scheduled INE fetch...");

            Input input = new SimpleInput();
            Output output = new SimpleOutput();

            input.setValue("function", "OPERACIONES");  // or another endpoint
            input.setValue("language", "ES");

            Map<String, String> params = new HashMap<>();
            input.setValue("params", params);

            new INEFetchDataCommand(input, output).execute();

            String response = output.getValue("jsonResponse");
            LOGGER.info("INE fetch complete. Length: " + response.length());

        } catch (Exception e) {
            LOGGER.severe("Error during scheduled INE fetch: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
