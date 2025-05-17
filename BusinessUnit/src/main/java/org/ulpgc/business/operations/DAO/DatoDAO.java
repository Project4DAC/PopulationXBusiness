package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import org.ulpgc.business.operations.POJO.DatoIndicador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatoDAO {
    private final Gson gson;

    public DatoDAO(Gson gson) {
        this.gson = gson;
    }

    public List<DatoIndicador> findByIndicadorCodigo(Connection conn, String codigo) throws SQLException {
        List<DatoIndicador> datos = new ArrayList<>();
        String query = "SELECT * FROM dato_indicador WHERE codigo_indicador = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DatoIndicador dato = new DatoIndicador(
                    rs.getInt("anio"),
                    rs.getDouble("valor"),
                    rs.getString("unidad")
                );
                datos.add(dato);
            }
        }
        return datos;
    }

    public List<DatoIndicador> findByIndicadorAndYearRange(Connection conn, String codigo, int startYear, int endYear) throws SQLException {
        List<DatoIndicador> datos = new ArrayList<>();
        String query = "SELECT * FROM dato_indicador WHERE codigo_indicador = ? AND anio BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigo);
            stmt.setInt(2, startYear);
            stmt.setInt(3, endYear);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DatoIndicador dato = new DatoIndicador(
                    rs.getInt("anio"),
                    rs.getDouble("valor"),
                    rs.getString("unidad")
                );
                datos.add(dato);
            }
        }
        return datos;
    }

    public boolean fetchAndSaveByTablaId(Connection conn, int tablaId) {
        // Esta función debe implementarse en base al diseño del API del INE
        // o fuente de datos correspondiente. Aquí va un mock:
        System.out.println("Simulación de fetch y save de datos para tabla: " + tablaId);
        return true;
    }
}
