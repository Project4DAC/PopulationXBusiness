package org.ulpgc.business.analysis;

import java.sql.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

public class SocioeconomicImpactAnalyzer {

    private static final String INE_DB_PATH = "BusXpop/INE.db";
    private static final String BORME_DB_PATH = "BusXpop/Borme.db";

    public static void main(String[] args) {
        SocioeconomicImpactAnalyzer analyzer = new SocioeconomicImpactAnalyzer();

        Map<String, Integer> ineEmpleoMunicipio = analyzer.fetchINEData();
        Map<String, Integer> bormeEmpleosNuevos = analyzer.fetchBORMEData();

        List<ImpactResult> results = analyzer.analyzeImpact(ineEmpleoMunicipio, bormeEmpleosNuevos);

        analyzer.exportToJson(results, "impact_report.json");
    }

    // 1️⃣ Fetch datos de INE.db: empleo total por municipio
    public Map<String, Integer> fetchINEData() {
        Map<String, Integer> empleoMunicipio = new HashMap<>();

        String query = "SELECT municipio, SUM(empleo) as total_empleo FROM EmpleoMunicipal GROUP BY municipio";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + INE_DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String municipio = rs.getString("municipio");
                int totalEmpleo = rs.getInt("total_empleo");
                empleoMunicipio.put(municipio, totalEmpleo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return empleoMunicipio;
    }

    // 2️⃣ Fetch datos de BORME.db: nuevos empleos creados por municipio
    public Map<String, Integer> fetchBORMEData() {
        Map<String, Integer> nuevosEmpleos = new HashMap<>();

        String query = "SELECT municipio, SUM(empleos_creados) as total_nuevos FROM EmpresasBorme GROUP BY municipio";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + BORME_DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String municipio = rs.getString("municipio");
                int totalNuevos = rs.getInt("total_nuevos");
                nuevosEmpleos.put(municipio, totalNuevos);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nuevosEmpleos;
    }

    // 3️⃣ Análisis de impacto a nivel municipal
    public List<ImpactResult> analyzeImpact(Map<String, Integer> ineData, Map<String, Integer> bormeData) {
        List<ImpactResult> results = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : bormeData.entrySet()) {
            String municipio = entry.getKey();
            int nuevosEmpleos = entry.getValue();

            if (ineData.containsKey(municipio)) {
                int totalEmpleo = ineData.get(municipio);
                double impacto = (nuevosEmpleos / (double) totalEmpleo) * 100.0;

                results.add(new ImpactResult(municipio, nuevosEmpleos, totalEmpleo, impacto));
            }
        }

        return results;
    }

    // 4️⃣ Exportar resultados a JSON
    public void exportToJson(List<ImpactResult> results, String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(results, writer);
            System.out.println("✅ Reporte generado: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // POJO resultado de análisis
    static class ImpactResult {
        String municipio;
        int nuevosEmpleos;
        int empleoTotal;
        double impactoPorcentaje;

        ImpactResult(String municipio, int nuevosEmpleos, int empleoTotal, double impactoPorcentaje) {
            this.municipio = municipio;
            this.nuevosEmpleos = nuevosEmpleos;
            this.empleoTotal = empleoTotal;
            this.impactoPorcentaje = impactoPorcentaje;
        }
    }
}
