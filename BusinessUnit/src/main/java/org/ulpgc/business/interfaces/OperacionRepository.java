package org.ulpgc.business.interfaces;

import org.ulpgc.business.operations.POJO.Operacion;

import java.util.Optional;

public interface OperacionRepository extends GenericRepository<Operacion, Integer> {
    Optional<Operacion> findByCodigo(String codigo);
}