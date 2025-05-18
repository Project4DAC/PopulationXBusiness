package org.ulpgc.business.operations.POJO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Indicador {
    private String cod;
    private String nombre;
    private int fkUnidad;
    private int fkEscala;
    private List<Dato> data;

    // Constructores
    public Indicador() {
        this.data = new ArrayList<>();
    }

    public Indicador(String cod, String nombre, int fkUnidad, int fkEscala) {
        this.cod = cod;
        this.nombre = nombre;
        this.fkUnidad = fkUnidad;
        this.fkEscala = fkEscala;
        this.data = new ArrayList<>();
    }

    // Getters y Setters
    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getFkUnidad() {
        return fkUnidad;
    }

    public void setFkUnidad(int fkUnidad) {
        this.fkUnidad = fkUnidad;
    }

    public int getFkEscala() {
        return fkEscala;
    }

    public void setFkEscala(int fkEscala) {
        this.fkEscala = fkEscala;
    }

    public List<Dato> getData() {
        return data;
    }

    public void setData(List<Dato> data) {
        this.data = data;
    }

    // Métodos de utilidad
    public void addDato(Dato dato) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(dato);
    }

    public Dato getLatestDato() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        // Asumiendo que los datos están ordenados por fecha más reciente primero
        return data.get(0);
    }

    @Override
    public String toString() {
        return "Indicador{" +
                "cod='" + cod + '\'' +
                ", nombre='" + nombre + '\'' +
                ", fkUnidad=" + fkUnidad +
                ", fkEscala=" + fkEscala +
                ", data.size=" + (data != null ? data.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indicador indicador = (Indicador) o;
        return Objects.equals(cod, indicador.cod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cod);
    }
}