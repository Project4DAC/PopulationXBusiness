import io.javalin.Javalin;
import io.javalin.http.Context;

import java.net.http.HttpClient;
import java.util.List;

import org.ulpgc.inefeeder.commands.update.INEFetchDataCommand;
import org.ulpgc.inefeeder.servicios.POJO.Operacion;
import org.ulpgc.inefeeder.servicios.POJO.TablasOperacion;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;
import org.ulpgc.inefeeder.servicios.general.Interfaces.ResponseParser;
import org.ulpgc.inefeeder.servicios.general.commands.RenderResultCommand;
import org.ulpgc.inefeeder.servicios.general.helpers.*;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        // Configurar el cliente HTTP (común para todas las consultas)
        HttpClient httpClient = HttpClient.newHttpClient();

        // Crear el publicador para ActiveMQ
        Publisher publisher = PublisherFactory.createActiveMQPublisher();


        // Configurar fetcher para Operaciones
        String operacionesEndpoint = "https://servicios.ine.es/wstempus/js/ES/OPERACIONES";
        ResponseParser<List<Operacion>> operacionesParser = IneResponseParsers.createOperacionesListParser();

        IneApiDataFetcher operacionesFetcher = new IneApiDataFetcher(
                operacionesEndpoint,
                "INE_OPERACIONES_QUEUE",
                publisher,
                httpClient,
                operacionesParser
        );

        // Configurar fetcher para Tablas de una Operación específica (ejemplo con ID=1)
        String tablasOperacionEndpoint = "https://servicios.ine.es/wstempus/js/ES/TABLAS_OPERACION/4";
        ResponseParser<List<TablasOperacion>> tablasParser = IneResponseParsers.createTablasOperacionListParser();

        IneApiDataFetcher tablasOperacionFetcher = new IneApiDataFetcher(
                tablasOperacionEndpoint,
                "INE_TABLAS_OPERACION_QUEUE",
                publisher,
                httpClient,
                tablasParser
        );

        // Iniciar los fetchs diarios
        operacionesFetcher.startDailyFetch();
        tablasOperacionFetcher.startDailyFetch();

        // Registrar hooks para cerrar limpiamente al terminar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            operacionesFetcher.stop();
            tablasOperacionFetcher.stop();
        }));

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
}
