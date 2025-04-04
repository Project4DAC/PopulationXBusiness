package org.ulpgc.proyecto;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.*;
import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.SimpleInput;
import org.ulpgc.proyecto.servicios.SimpleOutput;
import org.ulpgc.proyecto.servicios.inefeeder.commands.query.FetchDataCommand;

import java.util.HashMap;
import java.util.Map;

public class Main {

    enum FuncionesINE {
        DATOS_TABLA, DATOS_SERIE, DATOS_METADATAOPERACION, OPERACIONES_DISPONIBLES, OPERACION, VARIABLES,
        VARIABLES_OPERACION, VALORES_VARIABLE, VALORES_VARIABLEOPERACION, TABLAS_OPERACION, GRUPOS_TABLA,
        VALORES_GRUPOSTABLA, SERIE, SERIES_OPERACION, VALORES_SERIE, SERIES_TABLA, SERIE_METADATAOPERACION,
        PERIODICIDADES, PUBLICACIONES, PUBLICACIONES_OPERACION, PUBLICACIONFECHA_PUBLICACION
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        Gson gson = new Gson();
        app.get("/", ctx -> ctx.html(generarHtmlConSelector()));

        app.post("/fetchData", ctx -> {
            try {
                String functionName = ctx.formParam("function");
                String language = ctx.formParam("language");

                Input input = crearInput(ctx);
                Output output = new SimpleOutput();

                new FetchDataCommand(input, output).execute();

                String dynamicPath = "/result/" + functionName;
                if (language != null && !language.isEmpty()) {
                    dynamicPath += "/" + language;
                }

                ctx.sessionAttribute("generatedUrl", output.getValue("url"));
                ctx.sessionAttribute("jsonResponse", output.getValue("jsonResponse"));

                ctx.redirect(dynamicPath);
            } catch (Exception e) {
                ctx.html("Error: " + e.getMessage());
            }
        });

        app.get("/result/{function}", ctx -> {
            String function = ctx.pathParam("function");
            String url = ctx.sessionAttribute("generatedUrl");
            String jsonResponse = ctx.sessionAttribute("jsonResponse");
            StringBuilder html = new StringBuilder(getHtmlHeader());
            html.append("<h1>Resultado de la consulta: ").append(function).append("</h1>");

            if (url != null) {
                html.append("<h2>URL Generada:</h2><p>").append(url).append("</p>");
                ctx.sessionAttribute("generatedUrl", null);

                if (jsonResponse != null) {
                    html.append("<h2>Datos obtenidos:</h2>");
                    html.append("<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow: auto; max-height: 500px;'>");
                    html.append(jsonResponse);
                    html.append("</pre>");
                    ctx.sessionAttribute("jsonResponse", null);
                }
            } else {
                html.append("<p>No se ha generado ninguna consulta.</p>");
            }

            html.append("<p><a href='/'>Volver al inicio</a></p>");
            html.append(getHtmlFooter());

            ctx.html(html.toString());
        });

        app.get("/result/{function}/{language}", ctx -> {
            String function = ctx.pathParam("function");
            String language = ctx.pathParam("language");
            String url = ctx.sessionAttribute("generatedUrl");
            String jsonResponse = ctx.sessionAttribute("jsonResponse");

            StringBuilder html = new StringBuilder(getHtmlHeader());
            html.append("<h1>Resultado de la consulta: ").append(function).append("</h1>");
            html.append("<p>Idioma: ").append(language).append("</p>");

            if (url != null) {
                html.append("<h2>URL Generada:</h2><p>").append(url).append("</p>");
                ctx.sessionAttribute("generatedUrl", null);

                if (jsonResponse != null) {
                    html.append("<h2>Datos obtenidos:</h2>");
                    html.append("<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow: auto; max-height: 500px;'>");
                    html.append(jsonResponse);
                    html.append("</pre>");
                    ctx.sessionAttribute("jsonResponse", null);
                }
            } else {
                html.append("<p>No se ha generado ninguna consulta.</p>");
            }

            html.append("<p><a href='/'>Volver al inicio</a></p>");
            html.append(getHtmlFooter());

            ctx.html(html.toString());
        });
    }

    private static String generarHtmlConSelector() {
        StringBuilder html = new StringBuilder(getHtmlHeader());
        html.append("<h1>Consulta de datos del INE</h1>");
        html.append("<form method='post' action='/fetchData'>");
        html.append("<div><label for='function'>Función:</label>");
        html.append("<select name='function' id='function'>");
        for (FuncionesINE funcion : FuncionesINE.values()) {
            html.append("<option value='").append(funcion.name()).append("'>").append(funcion.name()).append("</option>");
        }
        html.append("</select></div>");

        html.append("<div style='margin-top: 10px;'><label for='language'>Idioma:</label>");
        html.append("<input type='text' id='language' name='language' value='es' placeholder='es, en, etc.'></div>");

        // Puedes añadir campos adicionales según la función seleccionada
        html.append("<div id='additionalParams'></div>");

        html.append("<div style='margin-top: 15px;'><button type='submit'>Consultar datos</button></div>");
        html.append("</form>");
        html.append(getHtmlFooter());
        return html.toString();
    }

    private static Input crearInput(Context ctx) {
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

    private static String getHtmlHeader() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Consulta de datos INE</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; }
                        h1 { color: #333; }
                        label { display: block; margin-bottom: 5px; }
                        select, input { padding: 8px; width: 300px; }
                        button { padding: 8px 16px; background-color: #4CAF50; color: white; border: none; cursor: pointer; }
                        button:hover { background-color: #45a049; }
                        pre { white-space: pre-wrap; }
                    </style>
                </head>
                <body>
                """;
    }

    private static String getHtmlFooter() {
        return "</body></html>";
    }
}