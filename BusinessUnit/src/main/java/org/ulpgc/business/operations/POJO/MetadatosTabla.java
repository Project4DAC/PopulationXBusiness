package org.ulpgc.business.operations.POJO;

import java.util.Map;

public class MetadatosTabla {
    private int tablaId;
    private Map<String, String> metadata;

    public int getTablaId() {
        return tablaId;
    }

    public void setTablaId(int tablaId) {
        this.tablaId = tablaId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadatos) {
        this.metadata = metadatos;
    }
}
