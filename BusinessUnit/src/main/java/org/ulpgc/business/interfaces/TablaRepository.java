package org.ulpgc.business.interfaces;

public interface TablaRepository extends GenericRepository<Tabla, Integer> {
    List<Tabla> findByOperacionId(int operacionId);
}