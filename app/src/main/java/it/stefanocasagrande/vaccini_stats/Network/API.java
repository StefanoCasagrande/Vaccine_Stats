package it.stefanocasagrande.vaccini_stats.Network;

import it.stefanocasagrande.vaccini_stats.json_classes.last_update_dataset;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface API {

    @GET("last-update-dataset.json")
    Call<last_update_dataset> getLastUpdate();
}
