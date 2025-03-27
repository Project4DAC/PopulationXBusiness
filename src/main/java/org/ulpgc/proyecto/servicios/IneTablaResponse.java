package org.ulpgc.proyecto.servicios;

public class IneTablaResponse {
    private Integer Id;
    private String Nombre;
    private String Codigo;
    private Integer FK_Periodicidad;
    private Integer FK_Publicacion;
    private Integer FK_Periodo_ini;
    private String Anyo_Periodo_ini;
    private String FechaRef_fin;
    private Long Ultima_Modificacion;

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getCodigo() {
        return Codigo;
    }

    public void setCodigo(String codigo) {
        Codigo = codigo;
    }

    public Integer getFK_Periodicidad() {
        return FK_Periodicidad;
    }

    public void setFK_Periodicidad(Integer FK_Periodicidad) {
        this.FK_Periodicidad = FK_Periodicidad;
    }

    public Integer getFK_Publicacion() {
        return FK_Publicacion;
    }

    public void setFK_Publicacion(Integer FK_Publicacion) {
        this.FK_Publicacion = FK_Publicacion;
    }

    public Integer getFK_Periodo_ini() {
        return FK_Periodo_ini;
    }

    public void setFK_Periodo_ini(Integer FK_Periodo_ini) {
        this.FK_Periodo_ini = FK_Periodo_ini;
    }

    public String getAnyo_Periodo_ini() {
        return Anyo_Periodo_ini;
    }

    public void setAnyo_Periodo_ini(String anyo_Periodo_ini) {
        Anyo_Periodo_ini = anyo_Periodo_ini;
    }

    public String getFechaRef_fin() {
        return FechaRef_fin;
    }

    public void setFechaRef_fin(String fechaRef_fin) {
        FechaRef_fin = fechaRef_fin;
    }

    public Long getUltima_Modificacion() {
        return Ultima_Modificacion;
    }

    public void setUltima_Modificacion(Long ultima_Modificacion) {
        Ultima_Modificacion = ultima_Modificacion;
    }
}
