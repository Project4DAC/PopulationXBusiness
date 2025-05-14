package org.ulpgc.IneSubscriberDataMart.query.POJO;

public class TablasOperacion {
    private int id;
    private String nombre;
    private String codigo;
    private int periodicidad;
    private int publicacion;
    private int periodoIni;
    private String anyoPeriodoIni;
    private String fechaRefFin;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(int periodicidad) {
        this.periodicidad = periodicidad;
    }

    public int getPublicacion() {
        return publicacion;
    }

    public void setPublicacion(int publicacion) {
        this.publicacion = publicacion;
    }

    public int getPeriodoIni() {
        return periodoIni;
    }

    public void setPeriodoIni(int periodoIni) {
        this.periodoIni = periodoIni;
    }

    public String getAnyoPeriodoIni() {
        return anyoPeriodoIni;
    }

    public void setAnyoPeriodoIni(String anyoPeriodoIni) {
        this.anyoPeriodoIni = anyoPeriodoIni;
    }

    public String getFechaRefFin() {
        return fechaRefFin;
    }

    public void setFechaRefFin(String fechaRefFin) {
        this.fechaRefFin = fechaRefFin;
    }
}
