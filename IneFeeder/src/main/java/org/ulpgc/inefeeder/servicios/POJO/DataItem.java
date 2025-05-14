package org.ulpgc.inefeeder.servicios.POJO;

import com.google.gson.annotations.SerializedName;

public class DataItem {
    @SerializedName(value = "key", alternate = {"Key"})
    private String key;

    @SerializedName(value = "value", alternate = {"Value"})
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}