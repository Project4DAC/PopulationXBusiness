package org.ulpgc.business.operations.POJO;

import java.sql.Time;
import java.sql.Timestamp;

public class Dato {

    private Timestamp fecha;
    private double valor;

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }


    public double getUnidad() { return valor; }
    public void setUnidad(double valor) { this.valor = valor; }

}
