package org.ulpgc.business.repository;

import org.ulpgc.business.Exceptions.DatabaseException;
import org.ulpgc.business.interfaces.OperacionRepository;
import org.ulpgc.business.interfaces.TablaRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryFactory {
    private static Map<Class<?>, Object> repositories = new ConcurrentHashMap<>();

    public static <T> T getRepository(Class<T> repositoryClass) {
        return (T) repositories.computeIfAbsent(repositoryClass, key -> {
            try {
                // Lógica de creación de repositorios
                if (key == OperacionRepository.class) {
                    return new OperacionRepositoryImpl();
                } else if (key == TablaRepository.class) {
                    return new TablaRepositoryImpl();
                }
                // Otros repositorios...
                throw new IllegalArgumentException("No repository found for " + key.getSimpleName());
            } catch (Exception e) {
                throw new DatabaseException("Error creating repository", e);
            }
        });
    }
}