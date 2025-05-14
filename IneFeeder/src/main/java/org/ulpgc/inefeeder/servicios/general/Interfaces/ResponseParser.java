package org.ulpgc.inefeeder.servicios.general.Interfaces;

/**
 * Interfaz para parsers que extraen datos estructurados de respuestas de API
 * @param <T> Tipo de dato que se extraerá de la respuesta
 */
public interface ResponseParser<T> {

    /**
     * Parsea una respuesta JSON y la convierte al tipo especificado
     *
     * @param jsonData La respuesta JSON a parsear
     * @return Objeto del tipo especificado o una colección de ellos
     * @throws Exception Si ocurre un error durante el parseo
     */
    T parse(String jsonData) throws Exception;
}