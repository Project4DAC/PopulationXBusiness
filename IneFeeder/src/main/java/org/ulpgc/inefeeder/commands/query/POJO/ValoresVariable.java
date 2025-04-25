package org.ulpgc.inefeeder.commands.query.POJO;

import com.google.gson.JsonArray;

public class ValoresVariable {
    private JsonArray valores;

    public JsonArray getValores() {
        return valores;
    }

    public void setValores(JsonArray valores) {
        this.valores = valores;
    }
}
