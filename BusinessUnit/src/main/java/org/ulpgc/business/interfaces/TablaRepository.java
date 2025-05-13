package org.ulpgc.business.interfaces;

import org.ulpgc.business.operations.POJO.TablasOperacion;

import java.util.List;

public interface TablaRepository extends GenericRepository<TablasOperacion, Integer> {
    List<TablasOperacion> findByOperacionId(int operacionId);
}