package it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;

public class vaccini_summary_dataset implements Serializable {

    @SerializedName("data")
    @Expose
    private List<vaccini_summary_data> data;

    public List<vaccini_summary_data> getData() {
        return data;
    }
}