package org.ulpgc.inefeeder.servicios.general.helpers;

import javax.sql.DataSource;

public class RunDailyINEFetcherCommand implements Runnable {

    private final DataSource dataSource;

    public RunDailyINEFetcherCommand(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run() {
        new Thread(new DailyINEFetcher(dataSource)).start();
    }
}
