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

    @SerializedName("categoria_operatori_sanitari_sociosanitari")
    @Expose
    public Integer categoria_operatori_sanitari_sociosanitari;

    @SerializedName("categoria_personale_non_sanitario")
    @Expose
    public Integer categoria_personale_non_sanitario;

    @SerializedName("categoria_ospiti_rsa")
    @Expose
    public Integer categoria_ospiti_rsa;

    @SerializedName("categoria_over80")
    @Expose
    public Integer categoria_over80;

    @SerializedName("categoria_over75")
    @Expose
    public Integer categoria_over75;

    @SerializedName("categoria_altro")
    @Expose
    public Integer categoria_altro;

    @SerializedName("categoria_over70")
    @Expose
    public Integer categoria_over70;

    @SerializedName("categoria_forze_armate")
    @Expose
    public Integer categoria_forze_armate;

    @SerializedName("categoria_personale_scolastico")
    @Expose
    public Integer categoria_personale_scolastico;

    @SerializedName("prima_dose")
    @Expose
    public Integer prima_dose;

    @SerializedName("seconda_dose")
    @Expose
    public Integer seconda_dose;
}
