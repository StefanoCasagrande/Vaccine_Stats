package it.stefanocasagrande.vaccini_stats.json_classes.popolazione;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class platea_data {

    @SerializedName("fascia_anagrafica")
    @Expose
    public String fascia_anagrafica;

    @SerializedName("nome_area")
    @Expose
    public String nome_area;

    @SerializedName("totale_popolazione")
    @Expose
    public int totale_popolazione;
}
