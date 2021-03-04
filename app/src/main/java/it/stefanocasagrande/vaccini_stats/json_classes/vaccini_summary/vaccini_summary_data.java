package it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class vaccini_summary_data implements Serializable {

    @SerializedName("area")
    @Expose
    public String area;

    @SerializedName("dosi_somministrate")
    @Expose
    public Integer dosi_somministrate;

    @SerializedName("dosi_consegnate")
    @Expose
    public Integer dosi_consegnate;

    @SerializedName("ultimo_aggiornamento")
    @Expose
    public String ultimo_aggiornamento;

    @SerializedName("nome_area")
    @Expose
    public String nome_area;

}
