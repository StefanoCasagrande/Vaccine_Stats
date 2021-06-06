package it.stefanocasagrande.vaccini_stats.json_classes.popolazione;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;

public class platea_dataset implements Serializable {

    @SerializedName("data")
    @Expose
    private List<platea_data> data;

    public List<platea_data> getData() {
        return data;
    }
}
