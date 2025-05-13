package org.ulpgc;

import org.ulpgc.business.interfaces.OperacionRepository;
import org.ulpgc.business.interfaces.TablaRepository;
import org.ulpgc.business.operations.POJO.Operacion;
import org.ulpgc.business.operations.POJO.TablasOperacion;
import org.ulpgc.business.operations.DAO.DatabaseInitializer;
import org.ulpgc.business.operations.DAO.DatabaseConnectionManager;
import org.ulpgc.business.repository.RepositoryFactory;
import org.ulpgc.business.service.APIService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize database
            System.out.println("Initializing database...");
            DatabaseInitializer.initializeDatabase();

            // Create an instance of the API service
            APIService apiService = new APIService();

            // Get repositories
            OperacionRepository operacionRepository = RepositoryFactory.getRepository(OperacionRepository.class);
            TablaRepository tablaRepository = RepositoryFactory.getRepository(TablaRepository.class);

            // Simple command-line interface
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("\n=== BusXpop - Statistical Data Management ===");
                System.out.println("1. Fetch operation by code");
                System.out.println("2. List all operations");
                System.out.println("3. Fetch tables for an operation");
                System.out.println("4. Fetch data for a table");
                System.out.println("5. Fetch frequencies");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        System.out.print("Enter operation code: ");
                        String operacionCodigo = scanner.nextLine();
                        Optional<Operacion> operacion = apiService.fetchAndSaveOperacion(operacionCodigo);

                        if (operacion.isPresent()) {
                            Operacion op = operacion.get();
                            System.out.println("Operation fetched and saved:");
                            System.out.println("ID: " + op.getId());
                            System.out.println("Code: " + op.getCodigo());
                            System.out.println("Name: " + op.getNombre());
                            System.out.println("IOE Code: " + op.getCodIoE());
                            System.out.println("URL: " + op.getUrlOperacion());
                        } else {
                            System.out.println("Operation not found or error occurred.");
                        }
                        break;

                    case 2:
                        List<Operacion> operacions = operacionRepository.findAll();
                        System.out.println("\nAll operations:");

                        if (operacions.isEmpty()) {
                            System.out.println("No operations found. Try fetching some first.");
                        } else {
                            for (Operacion op : operacions) {
                                System.out.println(op.getId() + ": " + op.getNombre() + " (" + op.getCodigo() + ")");
                            }
                        }
                        break;

                    case 3:
                        System.out.print("Enter operation code: ");
                        String codigo = scanner.nextLine();
                        int tablesCount = apiService.fetchAndSaveTablasOperacion(codigo);
                        System.out.println("Fetched and saved " + tablesCount + " tables.");

                        // Get the operation ID and list the tables
                        Optional<Operacion> op = operacionRepository.findByCodigo(codigo);
                        if (op.isPresent()) {
                            int operacionId = op.get().getId();
                            List<TablasOperacion> tablas = tablaRepository.findByOperacionId(operacionId);

                            System.out.println("\nTables for operation " + codigo + ":");
                            for (TablasOperacion tabla : tablas) {
                                System.out.println(tabla.getId() + ": " + tabla.getNombre());
                            }
                        }
                        break;

                    case 4:
                        System.out.print("Enter table ID: ");
                        int tablaId = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        boolean success = apiService.fetchAndSaveDatosTabla(tablaId);
                        if (success) {
                            System.out.println("Table data fetched and saved successfully.");
                        } else {
                            System.out.println("Error fetching table data or no data available.");
                        }
                        break;

                    case 5:
                        boolean freqSuccess = apiService.fetchAndSaveFrecuencias();
                        if (freqSuccess) {
                            System.out.println("Frequencies fetched and saved successfully.");
                        } else {
                            System.out.println("Error fetching frequencies or no data available.");
                        }
                        break;

                    case 0:
                        running = false;
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close database connection
            DatabaseConnectionManager.closeConnection();
        }
    }
}