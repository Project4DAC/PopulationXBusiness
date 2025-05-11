package org.ulpgc.inefeeder.commands.publisher;

import org.ulpgc.inefeeder.servicios.*;
import org.ulpgc.inefeeder.servicios.PublisherFactory;
import org.ulpgc.inefeeder.servicios.general.helpers.DailyINEFetcher;

import javax.sql.DataSource;

public class RunDailyINEFetcherCommand implements Command {
    private final Input input;
    private final Output output;
    private final DataSource dataSource;
    private final boolean publishToActiveMQ;

    public RunDailyINEFetcherCommand(Input input, Output output, DataSource dataSource) {
        this(input, output, dataSource, false);
    }
    
    public RunDailyINEFetcherCommand(Input input, Output output, DataSource dataSource, boolean publishToActiveMQ) {
        this.input = input;
        this.output = output;
        this.dataSource = dataSource;
        this.publishToActiveMQ = publishToActiveMQ;
    }

    @Override
    public String execute() {
        if (publishToActiveMQ) {
            // Create a publisher and start fetcher with publishing capabilities
            Publisher publisher = PublisherFactory.createActiveMQPublisher();
            new Thread(new DailyINEFetcherWithPublisher(dataSource, publisher)).start();
            output.setValue("html", "<html><body><h2>Proceso de fetch diario iniciado con publicación a ActiveMQ.</h2><a href='/'>Volver a inicio</a></body></html>");
        } else {
            // Use original DailyINEFetcher without publishing
            new Thread(new DailyINEFetcher(dataSource)).start();
            output.setValue("html", "<html><body><h2>Proceso de fetch diario iniciado.</h2><a href='/'>Volver a inicio</a></body></html>");
        }
        return null;
    }
}