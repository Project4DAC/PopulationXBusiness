package org.ulpgc.business.operations.POJO;

public class PropiedadesVariable {
    private int variableId;
    private int orden;
    private String tipo;
    private boolean esClave;

    public int getVariableId() {
        return variableId;
    }

    public void setVariableId(int variableId) {
        this.variableId = variableId;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isEsClave() {
        return esClave;
    }

    public void setEsClave(boolean esClave) {
        this.esClave = esClave;
    }
}
