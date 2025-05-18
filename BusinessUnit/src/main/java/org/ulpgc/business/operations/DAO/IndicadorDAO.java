package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import org.ulpgc.business.operations.POJO.Dato;
import org.ulpgc.business.operations.POJO.Indicador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IndicadorDAO {
    private Connection connection;
    private Gson gson;

    public IndicadorDAO(Gson gson) {
        this.gson = gson;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Método para insertar un nuevo indicador
    public void save(Indicador indicador) throws SQLException {
        insertarIndicador(indicador);
    }

    // Método para insertar un indicador y sus datos
    public void insertarIndicador(Indicador indicador) throws SQLException {
        String sqlIndicador = "INSERT INTO Indicadores (COD, Nombre, FK_Unidad, FK_Escala) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sqlIndicador)) {
            stmt.setString(1, indicador.getCod());
            stmt.setString(2, indicador.getNombre());
            stmt.setInt(3, indicador.getFkUnidad());
            stmt.setInt(4, indicador.getFkEscala());
            stmt.executeUpdate();
        }

        insertarDatos(indicador.getCod(), indicador.getData());
    }

    // Método para insertar los datos de un indicador
    private void insertarDatos(String codIndicador, List<Dato> datos) throws SQLException {
        String sqlDato = "INSERT INTO Datos (COD_Indicador, Fecha, FK_TipoDato, FK_Periodo, Anyo, Valor, Secreto) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sqlDato)) {
            for (Dato dato : datos) {
                stmt.setString(1, codIndicador);
                // Convertir Date a timestamp SQL
                Timestamp fecha = dato.getFecha();
                stmt.setTimestamp(1, fecha);
                stmt.setDouble(2, dato.getUnidad());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Método para buscar un indicador por su código
    public Indicador findByCod(String codigo) throws SQLException {
        String sql = "SELECT * FROM Indicadores WHERE COD = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Indicador indicador = new Indicador();
                    indicador.setCod(rs.getString("COD"));
                    indicador.setNombre(rs.getString("Nombre"));
                    indicador.setFkUnidad(rs.getInt("FK_Unidad"));
                    indicador.setFkEscala(rs.getInt("FK_Escala"));

                    // Cargar los datos asociados al indicador
                    indicador.setData(findDatosByCodIndicador(codigo));

                    return indicador;
                }
            }
        }
        return null;
    }

    // Método para buscar todos los datos de un indicador por su código
    private List<Dato> findDatosByCodIndicador(String codIndicador) throws SQLException {
        List<Dato> datos = new ArrayList<>();
        String sql = "SELECT * FROM Datos WHERE COD_Indicador = ? ORDER BY Fecha DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codIndicador);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Dato dato = new Dato();
                    dato.setFecha(rs.getTimestamp("Fecha"));
                    dato.setUnidad(rs.getDouble("Valor"));
                    datos.add(dato);
                }
            }
        }
        return datos;
    }

    // Método para obtener todos los indicadores
    public List<Indicador> findAll() throws SQLException {
        List<Indicador> indicadores = new ArrayList<>();
        String sql = "SELECT * FROM Indicadores";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String codigo = rs.getString("COD");
                Indicador indicador = new Indicador();
                indicador.setCod(codigo);
                indicador.setNombre(rs.getString("Nombre"));
                indicador.setFkUnidad(rs.getInt("FK_Unidad"));
                indicador.setFkEscala(rs.getInt("FK_Escala"));

                // Cargar los datos asociados al indicador
                indicador.setData(findDatosByCodIndicador(codigo));

                indicadores.add(indicador);
            }
        }
        return indicadores;
    }
}
