package it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class consegne_vaccini_dataset implements Serializable {

    @SerializedName("data")
    @Expose
    private List<consegne_vaccini_data> data;

    public List<consegne_vaccini_data> getData() {
        return data;
    }
}
