package it.stefanocasagrande.vaccini_stats.Network;

import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.last_update_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.popolazione.platea_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary.vaccini_summary_dataset;
import retrofit2.Call;
import retrofit2.http.GET;

public interface API {

    @GET("last-update-dataset.json")
    Call<last_update_dataset> getLastUpdate();

    @GET("consegne-vaccini-latest.json")
    Call<consegne_vaccini_dataset> getVaccinesDeliveries();

    @GET("anagrafica-vaccini-summary-latest.json")
    Call<anagrafica_vaccini_summary_dataset> getSummary_by_Age();

    @GET("vaccini-summary-latest.json")
    Call<vaccini_summary_dataset> getSummary_by_Location();

    @GET("platea.json")
    Call<platea_dataset> getPopolazione();

}
