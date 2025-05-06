package org.ulpgc.BormeFeeder.services.general.helpers;

import org.ulpgc.BormeFeeder.commands.query.BuildURLCommand;
import org.ulpgc.BormeFeeder.commands.update.BormeFetchAndSaveDataCommand;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;
import org.ulpgc.BormeFeeder.publisher.BormePublisher;

import java.time.LocalDate;
import javax.sql.DataSource;

public class DailyBormeFetcher implements Runnable {
    private final DataSource dataSource;
    private final BormePublisher publisher;

    public DailyBormeFetcher(DataSource dataSource) {
        this.dataSource = dataSource;
        this.publisher = new BormePublisher();
    }

    @Override
    public void run() {
        runSingle();
    }

    private void runSingle() {
        for (int daysBack = 0; daysBack <= 5; daysBack++) {
            try {
                Input input = new SimpleInput();
                Output output = new SimpleOutput();
                LocalDate targetDate = LocalDate.now().minusDays(daysBack);

                if (targetDate.getDayOfWeek().getValue() > 5) {
                    continue;
                }

                input.setValue("date", targetDate.toString());
                BuildURLCommand buildURLCommand = new BuildURLCommand(input, output);
                buildURLCommand.execute();
                String url = output.getValue("url");

                if (url != null && !url.isEmpty()) {
                    input.setValue("url", url);
                    new BormeFetchAndSaveDataCommand(input, output, dataSource).execute();
                    System.out.println("Successfully fetched BORME data for: " + targetDate);

                    try {
                        publisher.publishMessage(targetDate, url);
                        System.out.println("Successfully published message for BORME data: " + targetDate);
                    } catch (Exception e) {
                        System.err.println("Failed to publish message for " + targetDate + ": " + e.getMessage());
                        e.printStackTrace();
                    }

                    return;
                } else {
                    System.out.println("No valid URL found for date: " + targetDate);
                }
            } catch (Exception e) {
                System.out.println("Failed to fetch data for " + LocalDate.now().minusDays(daysBack) + ": " + e.getMessage());
            }
        }
        System.err.println("Failed to fetch BORME data for any recent dates");
    }
}
//TODO Aplicar Gitflow
