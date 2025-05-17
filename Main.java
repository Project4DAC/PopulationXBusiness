import java.util.Scanner;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose a module to run:");
        System.out.println("1. BormeFeeder");
        System.out.println("2. BusinessUnit");
        System.out.println("3. IneFeeder");
        System.out.println("4. StoreBuilder");
        System.out.println("0. Exit");

        int choice = scanner.nextInt();

        try {
            switch (choice) {
                case 0:
                    System.out.println("Exiting program.");
                    break;
                case 1:
                    runModule("BormeFeeder");
                    break;
                case 2:
                    runModule("BusinessUnit");
                    break;
                case 3:
                    runModule("IneFeeder");
                    break;
                case 4:
                    runModule("StoreBuilder");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } finally {
            scanner.close();
        }
    }

    private static void runModule(String moduleName) {
        try {
            System.out.println("Running " + moduleName);
            // Assuming each module has a Main class in a package structure like org.ulpgc.ModuleName
            Class<?> moduleClass = Class.forName("org.ulpgc." + moduleName + ".Main");
            Method mainMethod = moduleClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[]{});
        } catch (Exception e) {
            System.err.println("Failed to run module " + moduleName);
            e.printStackTrace();
        }
    }
}