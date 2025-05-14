package org.ulpgc.inefeeder.servicios.general.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ulpgc.inefeeder.servicios.POJO.DatosTabla;
import org.ulpgc.inefeeder.servicios.POJO.Operacion;
import org.ulpgc.inefeeder.servicios.POJO.TablasOperacion;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;
import org.ulpgc.inefeeder.servicios.general.Interfaces.ResponseParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IneApiDataFetcher {

    private final String apiEndpoint;
    private final String queueName;
    private final Publisher publisher;
    private final HttpClient httpClient;
    private final ResponseParser<?> responseParser;
    private final Gson gson;
    private ScheduledExecutorService scheduler;

    /**
     * Constructor con inyección completa de dependencias
     *
     * @param apiEndpoint URL de la API del INE a consultar
     * @param queueName Nombre de la cola donde publicar
     * @param publisher Implementación de Publisher a utilizar
     * @param httpClient Cliente HTTP para realizar las peticiones
     * @param responseParser Parser para procesar la respuesta JSON
     */
    public IneApiDataFetcher(
            String apiEndpoint,
            String queueName,
            Publisher publisher,
            HttpClient httpClient,
            ResponseParser<?> responseParser) {
        this.apiEndpoint = apiEndpoint;
        this.queueName = queueName;
        this.publisher = publisher;
        this.httpClient = httpClient;
        this.responseParser = responseParser;

        // Configurar Gson para ser flexible con los nombres de campos
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }

    /**
     * Inicia la tarea programada para hacer fetch de datos en intervalos regulares
     * @param initialDelayHours Horas a esperar antes de la primera ejecución
     * @param periodHours Período entre ejecuciones en horas
     */
    public void startScheduledFetch(int initialDelayHours, int periodHours) {
        scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(
                this::fetchAndPublish,
                initialDelayHours,
                periodHours,
                TimeUnit.HOURS
        );
    }

    /**
     * Inicia la tarea programada para hacer fetch diario
     */
    public void startDailyFetch() {
        startScheduledFetch(0, 24);
    }

    /**
     * Detiene la tarea programada
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Realiza el fetch de datos de la API y los publica en la cola
     */
    public void fetchAndPublish() {
        try {
            // Verificar que el publicador esté conectado
            if (!publisher.isConnected()) {
                throw new RuntimeException("Publisher is not connected");
            }

            String jsonData = fetchDataFromApi();
            Object parsedData = responseParser.parse(jsonData);

            // Publicar en la cola utilizando la interfaz Publisher
            publisher.publish(queueName, jsonData);

            if (parsedData instanceof List<?>) {
                System.out.println("Fetched and published " + ((List<?>) parsedData).size() + " items to queue: " + queueName);
            } else {
                System.out.println("Fetched and published data to queue: " + queueName);
            }
        } catch (Exception e) {
            System.err.println("Error in fetch and publish process: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Realiza la petición HTTP a la API
     */
    private String fetchDataFromApi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API returned status code: " + response.statusCode());
        }

        return response.body();
    }
}