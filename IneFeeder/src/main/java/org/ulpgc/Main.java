package org.ulpgc;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.net.http.HttpClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.ulpgc.inefeeder.commands.update.DailyINEFetcher;
import org.ulpgc.inefeeder.commands.update.INEFetchDataCommand;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;
import org.ulpgc.inefeeder.servicios.general.commands.RenderResultCommand;
import org.ulpgc.inefeeder.servicios.general.helpers.*;

import java.util.HashMap;
import java.util.Map;

public class Main {

    private static ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        HttpClient httpClient = HttpClient.newHttpClient();
        Publisher publisher = PublisherFactory.createActiveMQPublisher();

        // Start Javalin server
        Javalin app = Javalin.create().start(7001);
        app.get("/", ctx -> {
            Input input = new SimpleInput();
            Output output = new SimpleOutput();
            WebCommandFactory.createRenderHomeCommand(input, output).execute();
            ctx.html(output.getValue("html"));
        });

        app.post("/fetchINEData", Main::handleINE);
        app.get("/result/{function}", Main::renderResult);
        app.get("/result/{function}/{language}", Main::renderResultWithLanguage);
        app.post("/runDailyFetcherWithPublish", ctx -> {
            new Thread(new DailyINEFetcher()).start();
            ctx.html("<html><body><h2>Daily consultation of the INE started.</h2><a href='/'>Volver al inicio</a></body></html>");
        });

        // 🔁 Schedule daily fetch
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[Scheduler] Running daily INE fetch...");
                new DailyINEFetcher().run();
            } catch (Exception e) {
                System.err.println("Error in scheduled INE fetch: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 24, TimeUnit.HOURS);

        // 🛑 Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    scheduler.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }
        }));
    }

    private static void handleINE(Context ctx) {
        try {
            Input input = createINEInput(ctx);
            Output output = new SimpleOutput();

            new INEFetchDataCommand(input, output).execute();

            String jsonResponse = output.getValue("jsonResponse");
            String generatedUrl = output.getValue("url");
            String functionName = input.getValue("function");
            String language = input.getValue("language");

            String dynamicPath = "/result/" + functionName;
            if (language != null && !language.isEmpty()) {
                dynamicPath += "/" + language;
            }

            ctx.sessionAttribute("generatedUrl", generatedUrl);
            ctx.sessionAttribute("jsonResponse", jsonResponse);
            ctx.redirect(dynamicPath);
        } catch (Exception e) {
            ctx.html("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Input createINEInput(Context ctx) {
        SimpleInput input = new SimpleInput();
        input.setValue("language", ctx.formParam("language"));
        input.setValue("function", ctx.formParam("function"));

        Map<String, String> params = new HashMap<>();
        ctx.formParamMap().forEach((key, values) -> {
            if (!key.equals("language") && !key.equals("function") && !values.isEmpty()) {
                params.put(key, values.getFirst());
            }
        });

        input.setValue("params", params);
        return input;
    }

    private static void renderResult(Context ctx) {
        SimpleInput input = new SimpleInput();
        input.setValue("function", ctx.pathParam("function"));
        input.setValue("generatedUrl", ctx.sessionAttribute("generatedUrl"));
        input.setValue("jsonResponse", ctx.sessionAttribute("jsonResponse"));

        Output output = new SimpleOutput();
        new RenderResultCommand(input, output).execute();

        ctx.html(output.getValue("html"));
    }

    private static void renderResultWithLanguage(Context ctx) {
        SimpleInput input = new SimpleInput();
        input.setValue("function", ctx.pathParam("function"));
        input.setValue("language", ctx.pathParam("language"));
        input.setValue("generatedUrl", ctx.sessionAttribute("generatedUrl"));
        input.setValue("jsonResponse", ctx.sessionAttribute("jsonResponse"));

        Output output = new SimpleOutput();
        WebCommandFactory.createRenderResultCommand(input, output).execute();

        ctx.html(output.getValue("html"));
    }
}
