import io.javalin.Javalin;
import io.javalin.http.Context;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.ulpgc.inefeeder.commands.update.INEFetchDataCommand;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;
import org.ulpgc.inefeeder.servicios.general.commands.RenderResultCommand;
import org.ulpgc.inefeeder.servicios.general.helpers.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable dailyTask = new RunDailyINEFetcherCommand(ineDataSource);

        long initialDelay = computeInitialDelay();
        scheduler.scheduleAtFixedRate(dailyTask, initialDelay, 24 * 60, TimeUnit.MINUTES);
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
        app.post("/runDailyFetcher", ctx -> {
            new Thread(new DailyINEFetcher(ineDataSource)).start();
            ctx.html("<html><body><h2>Consulta diaria del INE iniciada.</h2><a href='/'>Volver al inicio</a></body></html>");
        });
        app.post("/runDailyFetcherWithPublish", ctx -> {
            Publisher publisher = new ActiveMQPublisher("tcp://localhost:61616");
            new Thread(new DailyINEFetcherWithPublisher(ineDataSource, publisher)).start();
            ctx.html("<html><body><h2>Consulta diaria del INE iniciada.</h2><a href='/'>Volver al inicio</a></body></html>");
        });
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
    private static long computeInitialDelay() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0).plusDays(1);
        return java.time.Duration.between(now, nextRun).toMinutes();
    }
}
