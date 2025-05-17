package org.ulpgc.business.operations.POJO;

import java.util.List;

public class Indicador {
    private String cod;
    private String nombre;
    private int fkUnidad;
    private int fkEscala;
    private List<Dato> data;

    // Getters y Setters
    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getFkUnidad() { return fkUnidad; }
    public void setFkUnidad(int fkUnidad) { this.fkUnidad = fkUnidad; }

    public int getFkEscala() { return fkEscala; }
    public void setFkEscala(int fkEscala) { this.fkEscala = fkEscala; }

    public List<Dato> getData() { return data; }
    public void setData(List<Dato> data) { this.data = data; }
}
