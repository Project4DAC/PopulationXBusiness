package org.ulpgc.proyecto.servicios;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IneApiClient {
    private static final String BASE_URL = "https://servicios.ine.es/wstempus/js/";
    private final HttpClient httpClient;
    private final Gson gson;

    public enum IneLanguage {
        ES, EN;
    }

    public enum IneFunction {
        DATOS_TABLA, DATOS_SERIE, DATOS_METADATAOPERACION, OPERACIONES_DISPONIBLES, OPERACION,
        VARIABLES, VARIABLES_OPERACION, VALORES_VARIABLE, VALORES_VARIABLEOPERACION,
        TABLAS_OPERACION, GRUPOS_TABLA, VALORES_GRUPOSTABLA, SERIE, SERIES_OPERACION,
        VALORES_SERIE, SERIES_TABLA, SERIE_METADATAOPERACION, PERIODICIDADES,
        PUBLICACIONES, PUBLICACIONES_OPERACION, PUBLICACIONFECHA_PUBLICACION;
    }

    public IneApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public String buildUrl(IneLanguage language, IneFunction function, String input, Map<String, String> params) {
        if (language == null || function == null) {
            throw new IllegalArgumentException("Los parámetros language y function no pueden ser nulos.");
        }

        StringBuilder url = new StringBuilder(BASE_URL);
        url.append(language).append("/");
        url.append(function);

        if (input != null && !input.isEmpty()) {
            url.append("/").append(input);
        }

        if (params != null && !params.isEmpty()) {
            String queryString = buildQueryString(params);
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    private String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return params.entrySet().stream()
                .map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al codificar parámetros de la URL", e);
                    }
                })
                .collect(Collectors.joining("&"));
    }

    public String fetchJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error fetching data from INE API: " + response.statusCode());
        }

        return response.body();
    }

    public String getOperacionesDisponibles(IneLanguage language, Map<String, String> params) throws IOException, InterruptedException {
        String url = buildUrl(language, IneFunction.OPERACIONES_DISPONIBLES, null, params);
        return fetchJson(url);
    }

    public String getTablasOperacion(IneLanguage language, String operacionId, Map<String, String> params) throws IOException, InterruptedException {
        String url = buildUrl(language, IneFunction.TABLAS_OPERACION, operacionId, params);
        return fetchJson(url);
    }
}