package org.ulpgc.inefeeder.commands.query.POJO;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class DatosTabla {
    private Metadata metadata;
    private JsonArray data;
    private JsonArray variables;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public JsonArray getData() {
        return data;
    }

    public void setData(JsonArray data) {
        this.data = data;
    }

    public JsonArray getVariables() {
        return variables;
    }

    public void setVariables(JsonArray variables) {
        this.variables = variables;
    }

    public static class Metadata {
        @SerializedName(value = "id", alternate = {"Id"})
        private int id;

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("codigo")
        private String codigo;

        @SerializedName("periodicidad")
        private int periodicidad;

        @SerializedName("publicacion")
        private int publicacion;

        @SerializedName(value = "periodoIni", alternate = {"periodo_ini"})
        private int periodoIni;

        @SerializedName(value = "anyoPeriodoIni", alternate = {"anyo_periodo_ini"})
        private String anyoPeriodoIni;

        @SerializedName(value = "fechaRefFin", alternate = {"fecha_ref_fin"})
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
}