package org.ulpgc.IneSubscriberDataMart.query.POJO;

import com.google.gson.annotations.SerializedName;

public class Operacion {
    @SerializedName(value = "id", alternate = {"Id"})
    private int id;

    @SerializedName(value = "codIoE", alternate = {"Cod_IOE"})
    private String codIoE;

    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @SerializedName(value = "codigo", alternate = {"Codigo"})
    private String codigo;

    @SerializedName(value = "urlOperacion", alternate = {"Url", "url_operacion"})
    private String urlOperacion;

    public String getCodIoE() {
        return codIoE;
    }

    public void setCodIoE(String codIoE) {
        this.codIoE = codIoE;
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

    public String getUrlOperacion() {
        return urlOperacion;
    }

    public void setUrlOperacion(String urlOperacion) {
        this.urlOperacion = urlOperacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}