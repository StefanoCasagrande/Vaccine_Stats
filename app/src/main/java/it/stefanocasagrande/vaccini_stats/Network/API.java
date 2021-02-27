package it.stefanocasagrande.vaccini_stats.Network;

import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.last_update_dataset;
import retrofit2.Call;
import retrofit2.http.GET;

public interface API {

    @GET("last-update-dataset.json")
    Call<last_update_dataset> getLastUpdate();

    @GET("consegne-vaccini-latest.json")
    Call<consegne_vaccini_dataset> getConsegneVaccini();
}
