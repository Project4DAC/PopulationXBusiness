import io.javalin.Javalin;
import io.javalin.http.Context;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ulpgc.inefeeder.commands.update.INEFetchAndSaveDataCommand;
import org.ulpgc.inefeeder.servicios.general.helpers.INETableCommandFactory;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;
import main.java.org.ulpgc.inefeeder.servicios.general.commands.RenderResultCommand;
import main.java.org.ulpgc.inefeeder.servicios.general.helpers.SimpleInput;
import main.java.org.ulpgc.inefeeder.servicios.general.helpers.SimpleOutput;
import org.ulpgc.inefeeder.servicios.general.helpers.WebCommandFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static DataSource ineDataSource;

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
        ineDataSource = DatabaseUtil.createDataSource("./INE.db", "INEPool");
        INETableCommandFactory.createInitializeDatabaseCommand(ineDataSource).execute();

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
    }

    private static void handleINE(Context ctx) {
        try {
            Input input = createINEInput(ctx);
            Output output = new SimpleOutput();

            new INEFetchAndSaveDataCommand(input, output, ineDataSource).execute();

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
