package it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class consegne_vaccini_data implements Serializable {

    @SerializedName("index")
    @Expose
    public Integer index;

    @SerializedName("area")
    @Expose
    public String area;

    @SerializedName("fornitore")
    @Expose
    public String fornitore;

    @SerializedName("numero_dosi")
    @Expose
    public Integer numero_dosi;

    @SerializedName("data_consegna")
    @Expose
    public String data_consegna;

    @SerializedName("codice_NUTS1")
    @Expose
    public String codice_NUTS1;

    @SerializedName("codice_NUTS2")
    @Expose
    public String codice_NUTS2;

    @SerializedName("codice_regione_ISTAT")
    @Expose
    public Integer codice_regione_ISTAT;

    @SerializedName("nome_area")
    @Expose
    public String nome_area;


}