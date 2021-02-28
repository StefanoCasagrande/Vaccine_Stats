package it.stefanocasagrande.vaccini_stats;

import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.Common.DB;
import it.stefanocasagrande.vaccini_stats.Network.API;
import it.stefanocasagrande.vaccini_stats.Network.CheckNetwork;
import it.stefanocasagrande.vaccini_stats.Network.NetworkClient;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.last_update_dataset;
import it.stefanocasagrande.vaccini_stats.ui.fragment_delivery_details;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_summary_by_age, R.id.nav_group_deliveries, R.id.nav_points)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        CheckNetwork network = new CheckNetwork(getApplicationContext());
        network.registerNetworkCallback();

        Common.Database = new DB(this);

        if (GlobalVariables.isNetworkConnected)
            getLastUpdate();
        else
        {
             if (!Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(""))
            {
                /* ToDo Load data already in the db */
            }
            else
                Toast.makeText(this,getString(R.string.Internet_Missing), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //region CheckLastUpdate

    public void Check_Update(String last_update)
    {
        boolean debug=true;

        if ((debug) || (Common.Database.Get_Configurazione("ultimo_aggiornamento").equals("") || !Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(last_update)))
        {
            Common.Database.Set_Configurazione("ultimo_aggiornamento", last_update);
            getconsegne_vaccini();
        }
        else
        {
            /* ToDo Load data already in the db */
        }
    }

    //endregion

    //region API

    public void getconsegne_vaccini()
    {
        Retrofit retrofit= NetworkClient.getRetrofitClient();

        API VacciniAPIs = retrofit.create(API.class);

        Call call = VacciniAPIs.getConsegneVaccini();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
              /*This is the success callback. Though the response type is JSON, with Retrofit we get
              the response in the form of WResponse POJO class
              */
                if (response.body()!=null) {
                    consegne_vaccini_dataset wResponse = (consegne_vaccini_dataset) response.body();
                    Common.Database.Insert_Consegne(wResponse.getData());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getLastUpdate()
    {
        Retrofit retrofit= NetworkClient.getRetrofitClient();

        API VacciniAPIs = retrofit.create(API.class);

        Call call = VacciniAPIs.getLastUpdate();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
              /*This is the success callback. Though the response type is JSON, with Retrofit we get
              the response in the form of WResponse POJO class
              */
                if (response.body()!=null) {
                    last_update_dataset wResponse = (last_update_dataset) response.body();
                    Check_Update(wResponse.ultimo_aggiornamento);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    //endregion

    //region GoTo

    public void goToDelivery_Details(String area_name)
    {
        Fragment fragment = fragment_delivery_details.newInstance(area_name);
        String tag=getString(R.string.fragment_detail_consegne);
        Show_Fragment(fragment, tag);
    }


    public void Show_Fragment(Fragment fragment, String tag)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, tag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //endregion
}