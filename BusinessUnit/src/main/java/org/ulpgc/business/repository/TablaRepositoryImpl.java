package org.ulpgc.business.repository;

import org.ulpgc.business.Exceptions.DatabaseException;
import org.ulpgc.business.interfaces.TablaRepository;
import org.ulpgc.business.operations.DAO.DatabaseConnectionManager;
import org.ulpgc.business.operations.POJO.TablasOperacion;
import org.ulpgc.business.service.EntityValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TablaRepositoryImpl implements TablaRepository {
    private final EntityValidator validator;

    public TablaRepositoryImpl() {
        this.validator = new EntityValidator();
    }

    @Override
    public void save(TablasOperacion entity) {
        validator.validate(entity);
        
        String sql = "INSERT OR REPLACE INTO tablas " +
                "(id, nombre, codigo, periodicidad, publicacion, periodo_ini, anyo_periodo_ini, fecha_ref_fin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, entity.getId());
            stmt.setString(2, entity.getNombre());
            stmt.setString(3, entity.getCodigo());
            stmt.setInt(4, entity.getPeriodicidad());
            stmt.setInt(5, entity.getPublicacion());
            stmt.setInt(6, entity.getPeriodoIni());
            stmt.setString(7, entity.getAnyoPeriodoIni());
            stmt.setString(8, entity.getFechaRefFin());
            
            stmt.executeUpdate();
            DatabaseConnectionManager.commitTransaction();
        } catch (SQLException e) {
            DatabaseConnectionManager.rollbackTransaction();
            throw new DatabaseException("Error saving tabla", e);
        }
    }

    @Override
    public Optional<TablasOperacion> findById(Integer id) {
        String sql = "SELECT * FROM tablas WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TablasOperacion tabla = new TablasOperacion();
                    tabla.setId(rs.getInt("id"));
                    tabla.setNombre(rs.getString("nombre"));
                    tabla.setCodigo(rs.getString("codigo"));
                    tabla.setPeriodicidad(rs.getInt("periodicidad"));
                    tabla.setPublicacion(rs.getInt("publicacion"));
                    tabla.setPeriodoIni(rs.getInt("periodo_ini"));
                    tabla.setAnyoPeriodoIni(rs.getString("anyo_periodo_ini"));
                    tabla.setFechaRefFin(rs.getString("fecha_ref_fin"));
                    
                    return Optional.of(tabla);
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding tabla by ID", e);
        }
    }

    @Override
    public List<TablasOperacion> findAll() {
        List<TablasOperacion> tablas = new ArrayList<>();
        String sql = "SELECT * FROM tablas";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                TablasOperacion tabla = new TablasOperacion();
                tabla.setId(rs.getInt("id"));
                tabla.setNombre(rs.getString("nombre"));
                tabla.setCodigo(rs.getString("codigo"));
                tabla.setPeriodicidad(rs.getInt("periodicidad"));
                tabla.setPublicacion(rs.getInt("publicacion"));
                tabla.setPeriodoIni(rs.getInt("periodo_ini"));
                tabla.setAnyoPeriodoIni(rs.getString("anyo_periodo_ini"));
                tabla.setFechaRefFin(rs.getString("fecha_ref_fin"));
                
                tablas.add(tabla);
            }
            
            return tablas;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all tablas", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM tablas WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            DatabaseConnectionManager.commitTransaction();
        } catch (SQLException e) {
            DatabaseConnectionManager.rollbackTransaction();
            throw new DatabaseException("Error deleting tabla", e);
        }
    }

    @Override
    public void update(TablasOperacion entity) {
        validator.validate(entity);
        
        // Reuse save method for update, as it uses INSERT OR REPLACE
        save(entity);
    }

    @Override
    public List<TablasOperacion> findByOperacionId(int operacionId) {
        List<TablasOperacion> tablas = new ArrayList<>();
        String sql = "SELECT t.* FROM tablas t " +
                     "JOIN tabla_operaciones to ON t.id = to.tabla_id " +
                     "WHERE to.operacion_id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, operacionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TablasOperacion tabla = new TablasOperacion();
                    tabla.setId(rs.getInt("id"));
                    tabla.setNombre(rs.getString("nombre"));
                    tabla.setCodigo(rs.getString("codigo"));
                    tabla.setPeriodicidad(rs.getInt("periodicidad"));
                    tabla.setPublicacion(rs.getInt("publicacion"));
                    tabla.setPeriodoIni(rs.getInt("periodo_ini"));
                    tabla.setAnyoPeriodoIni(rs.getString("anyo_periodo_ini"));
                    tabla.setFechaRefFin(rs.getString("fecha_ref_fin"));
                    
                    tablas.add(tabla);
                }
            }
            
            return tablas;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding tablas by operacion ID", e);
        }
    }
}