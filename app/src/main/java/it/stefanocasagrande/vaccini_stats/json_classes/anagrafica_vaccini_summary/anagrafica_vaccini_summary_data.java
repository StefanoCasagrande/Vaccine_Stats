package it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class anagrafica_vaccini_summary_data implements Serializable {

    @SerializedName("index")
    @Expose
    public Integer index;

    @SerializedName("fascia_anagrafica")
    @Expose
    public String fascia_anagrafica;

    @SerializedName("ultimo_aggiornamento")
    @Expose
    public String ultimo_aggiornamento;

    @SerializedName("totale")
    @Expose
    public Integer totale;

    @SerializedName("sesso_maschile")
    @Expose
    public Integer sesso_maschile;

    @SerializedName("sesso_femminile")
    @Expose
    public Integer sesso_femminile;

    @SerializedName("prima_dose")
    @Expose
    public Integer prima_dose;

    @SerializedName("seconda_dose")
    @Expose
    public Integer seconda_dose;

    @SerializedName("dose_addizionale_booster")
    @Expose
    public Integer dose_addizionale_booster;
}
