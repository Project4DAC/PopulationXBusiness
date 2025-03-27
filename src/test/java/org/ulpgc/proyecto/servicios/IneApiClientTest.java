package org.ulpgc.proyecto.servicios;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IneApiClientTest {

    private IneApiClient ineApiClient;

    @Test
    void buildUrl_validParams_returnsCorrectUrl() {
        ineApiClient = new IneApiClient();
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");

        String url = ineApiClient.buildUrl(IneApiClient.IneLanguage.ES, IneApiClient.IneFunction.DATOS_TABLA, "123", params);

        assertEquals("https://servicios.ine.es/wstempus/js/ES/DATOS_TABLA/123?param1=value1&param2=value2", url);
    }

    @Test
    void buildUrl_noParams_returnsCorrectUrl() {
        ineApiClient = new IneApiClient();
        String url = ineApiClient.buildUrl(IneApiClient.IneLanguage.ES, IneApiClient.IneFunction.DATOS_SERIE, "456", null);
        assertEquals("https://servicios.ine.es/wstempus/js/ES/DATOS_SERIE/456", url);
    }

    @Test
    void buildUrl_noInput_returnsCorrectUrl() {
        ineApiClient = new IneApiClient();
        String url = ineApiClient.buildUrl(IneApiClient.IneLanguage.ES, IneApiClient.IneFunction.OPERACIONES_DISPONIBLES, null, null);
        assertEquals("https://servicios.ine.es/wstempus/js/ES/OPERACIONES_DISPONIBLES", url);
    }

}