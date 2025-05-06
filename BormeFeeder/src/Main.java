
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import org.ulpgc.BormeFeeder.commands.update.BormeFetchAndSaveDataCommand;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;
import org.ulpgc.BormeFeeder.services.general.commands.RenderResultCommand;
import org.ulpgc.BormeFeeder.services.general.helpers.BormeTableCommandFactory;
import org.ulpgc.BormeFeeder.services.general.helpers.DailyBormeFetcher;
import org.ulpgc.BormeFeeder.services.general.helpers.SimpleInput;
import org.ulpgc.BormeFeeder.services.general.helpers.SimpleOutput;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.ulpgc.BormeFeeder.services.general.helpers.WebCommandFactory.createRenderHomeCommand;

public class Main {
    private static DataSource bormeDataSource;
    public class DatabaseUtil {
        public static HikariDataSource createDataSource(String dbPath, String poolName) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setPoolName(poolName);
            return new HikariDataSource(config);
        }
    }
    public static void main(String[] args) {
        bormeDataSource = DatabaseUtil.createDataSource("BORME.db", "BORMEPool");
        BormeTableCommandFactory.createInitializeDatabaseCommand(bormeDataSource).execute();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable dailyTask = new DailyBormeFetcher(bormeDataSource);

        long initialDelay = computeInitialDelay();
        scheduler.scheduleAtFixedRate(dailyTask, initialDelay, 24 * 60, TimeUnit.MINUTES);

        Javalin app = Javalin.create().start(7002);
        app.get("/", ctx -> {
               Input input = new SimpleInput();
                Output output = new SimpleOutput();
        createRenderHomeCommand(input, output).execute();
        ctx.html(output.getValue("html"));}
        );
        app.post("/runDailyFetcher", ctx -> {
            new Thread(new DailyBormeFetcher(bormeDataSource)).start();
            ctx.html("<html><body><h2>Consulta diaria del Borme iniciada.</h2><a href='/'>Volver al inicio</a></body></html>");
        });

        app.post("/fetchBormeData",Main::handleBorme);

        app.get("/borme/{date}", ctx -> {
            Input input = new SimpleInput();
            Output output = new SimpleOutput();
            input.setValue("date", ctx.pathParam("date"));
            input.setValue("url", ctx.sessionAttribute("url"));
            input.setValue("jsonResponse", ctx.sessionAttribute("jsonResponse"));
            new RenderResultCommand(input, output).execute();
            ctx.html(output.getValue("html"));
        });
    }
    //TODO Localizar el español y cambiarlo a ingles.
    private static void handleBorme(Context ctx) {
        try {
            Input input = createBormeInput(ctx);
            Output output = new SimpleOutput();

            new BormeFetchAndSaveDataCommand(input, output, bormeDataSource).execute();

            String jsonResponse = output.getValue("jsonResponse");
            String url = output.getValue("url");
            String date = input.getValue("date");

            ctx.sessionAttribute("jsonResponse", jsonResponse);
            ctx.sessionAttribute("url", url);

            ctx.redirect("/borme/" + date);
        } catch (Exception e) {
            ctx.html("Error en BORME: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Input createBormeInput(Context ctx) {
        SimpleInput input = new SimpleInput();
        input.setValue("date", ctx.formParam("date"));
        input.setValue("url", ctx.formParam("url"));

        Map<String, String> params = new HashMap<>();
        ctx.formParamMap().forEach((key, values) -> {
            if (!key.equals("date") && !key.equals("url") && !values.isEmpty()){
                params.put(key, values.getFirst());
            }
        });

        input.setValue("params", params);
        return input;
    }
    private static long computeInitialDelay() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0).plusDays(1);
        return java.time.Duration.between(now, nextRun).toMinutes();
    }
}
