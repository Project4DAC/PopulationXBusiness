package org.ulpgc.proyecto.servicios;

public class IneOperacionesResponse {
    private int Id;
    private String Cod_IOE;
    private String Nombre;
    private String Codigo;
    private String Url;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getCod_IOE() {
        return Cod_IOE;
    }

    public void setCod_IOE(String cod_IOE) {
        Cod_IOE = cod_IOE;
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

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }


}
