package org.ulpgc.inefeeder.servicios.general.helpers;

import org.ulpgc.inefeeder.servicios.Command;
import org.ulpgc.inefeeder.servicios.Input;
import org.ulpgc.inefeeder.servicios.Output;
import org.ulpgc.inefeeder.servicios.general.helpers.DailyINEFetcher;

import javax.sql.DataSource;

public class RunDailyINEFetcherCommand implements Command {
    private final Input input;
    private final Output output;
    private final DataSource dataSource;

    public RunDailyINEFetcherCommand(Input input, Output output, DataSource dataSource) {
        this.input = input;
        this.output = output;
        this.dataSource = dataSource;
    }

    @Override
    public String execute() {
        new Thread(new DailyINEFetcher(dataSource)).start();
        output.setValue("html", "<html><body><h2>Proceso de fetch diario iniciado.</h2><a href='/'>Volver a inicio</a></body></html>");
        return null;
    }
}
