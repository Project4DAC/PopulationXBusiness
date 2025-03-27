package org.ulpgc.proyecto;
import io.javalin.Javalin;
import org.ulpgc.proyecto.servicios.IneApiClient;
import org.ulpgc.proyecto.servicios.IneDatabaseService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try (IneDatabaseService databaseService = new IneDatabaseService()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Application shutdown initiated");
            }));

            databaseService.procesarYGuardarOperaciones();
            databaseService.procesarYGuardarTablas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}