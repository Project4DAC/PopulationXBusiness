package org.ulpgc.business.repository;

import org.ulpgc.business.Exceptions.DatabaseException;
import org.ulpgc.business.interfaces.OperacionRepository;
import org.ulpgc.business.operations.DAO.DatabaseConnectionManager;
import org.ulpgc.business.operations.POJO.Operacion;
import org.ulpgc.business.service.EntityValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperacionRepositoryImpl implements OperacionRepository {
    private final EntityValidator validator;

    public OperacionRepositoryImpl() {
        this.validator = new EntityValidator();
    }

    @Override
    public void save(Operacion entity) {
        validator.validate(entity);
        
        String sql = "INSERT OR REPLACE INTO operaciones " +
                "(id, COD_IOE, nombre, codigo, url_operacion) " +
                "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, entity.getId());
            stmt.setString(2, entity.getCodIoE());
            stmt.setString(3, entity.getNombre());
            stmt.setString(4, entity.getCodigo());
            stmt.setString(5, entity.getUrlOperacion());
            
            stmt.executeUpdate();
            DatabaseConnectionManager.commitTransaction();
        } catch (SQLException e) {
            DatabaseConnectionManager.rollbackTransaction();
            throw new DatabaseException("Error saving operacion", e);
        }
    }

    @Override
    public Optional<Operacion> findById(Integer id) {
        String sql = "SELECT * FROM operaciones WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Operacion operacion = new Operacion();
                    operacion.setId(rs.getInt("id"));
                    operacion.setCodIoE(rs.getString("COD_IOE"));
                    operacion.setNombre(rs.getString("nombre"));
                    operacion.setCodigo(rs.getString("codigo"));
                    operacion.setUrlOperacion(rs.getString("url_operacion"));
                    
                    return Optional.of(operacion);
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding operacion by ID", e);
        }
    }

    @Override
    public List<Operacion> findAll() {
        List<Operacion> operaciones = new ArrayList<>();
        String sql = "SELECT * FROM operaciones";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Operacion operacion = new Operacion();
                operacion.setId(rs.getInt("id"));
                operacion.setCodIoE(rs.getString("COD_IOE"));
                operacion.setNombre(rs.getString("nombre"));
                operacion.setCodigo(rs.getString("codigo"));
                operacion.setUrlOperacion(rs.getString("url_operacion"));
                
                operaciones.add(operacion);
            }
            
            return operaciones;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all operaciones", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM operaciones WHERE id = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            DatabaseConnectionManager.commitTransaction();
        } catch (SQLException e) {
            DatabaseConnectionManager.rollbackTransaction();
            throw new DatabaseException("Error deleting operacion", e);
        }
    }

    @Override
    public void update(Operacion entity) {
        validator.validate(entity);
        
        // Reuse save method for update, as it uses INSERT OR REPLACE
        save(entity);
    }

    @Override
    public Optional<Operacion> findByCodigo(String codigo) {
        String sql = "SELECT * FROM operaciones WHERE codigo = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codigo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Operacion operacion = new Operacion();
                    operacion.setId(rs.getInt("id"));
                    operacion.setCodIoE(rs.getString("COD_IOE"));
                    operacion.setNombre(rs.getString("nombre"));
                    operacion.setCodigo(rs.getString("codigo"));
                    operacion.setUrlOperacion(rs.getString("url_operacion"));
                    
                    return Optional.of(operacion);
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding operacion by codigo", e);
        }
    }
}