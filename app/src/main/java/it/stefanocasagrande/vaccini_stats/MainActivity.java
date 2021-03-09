package it.stefanocasagrande.vaccini_stats;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.last_update_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary.vaccini_summary_dataset;
import it.stefanocasagrande.vaccini_stats.ui.fragment_delivery_details;
import it.stefanocasagrande.vaccini_stats.ui.fragment_delivery_group;
import it.stefanocasagrande.vaccini_stats.ui.fragment_summary_by_age;
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
                R.id.nav_summary_by_age, R.id.nav_group_deliveries)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        CheckNetwork network = new CheckNetwork(getApplicationContext());
        network.registerNetworkCallback();

        Common.Database = new DB(this);
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

    @Override
    public void onBackPressed() {

        switch (Common.Back_Action)
        {
            case Common.Back_To_Summary:
                Common.Back_Action = Common.Back_To_Nowhere;
                goToSummary();
                break;
            case Common.Back_To_Delivery_Group:
                Common.Back_Action=Common.Back_To_Summary;
                goToDelivery_Group();
                break;
        }
    }

    //region CheckLastUpdate

    public void Check_Update(String last_update, fragment_summary_by_age var)
    {
        boolean debug=true;

        if ((debug) || (Common.Database.Get_Configurazione("ultimo_aggiornamento").equals("") || !Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(last_update)))
        {
            Common.Database.Set_Configurazione("ultimo_aggiornamento", last_update);
            getSummary_by_Age(var);
        }
        else
        {
            /* ToDo Load data already in the db */
        }
    }

    //endregion

    //region API

    public void getSummary_by_Age(fragment_summary_by_age var)
    {
        final ProgressDialog waiting_bar = getprogressDialog();
        waiting_bar.show();

        Retrofit retrofit= NetworkClient.getRetrofitClient();

        API VacciniAPIs = retrofit.create(API.class);

        Call call = VacciniAPIs.getSummary_by_Age();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
              /*This is the success callback. Though the response type is JSON, with Retrofit we get
              the response in the form of WResponse POJO class
              */
                if (response.body()!=null) {
                    anagrafica_vaccini_summary_dataset wResponse = (anagrafica_vaccini_summary_dataset) response.body();
                    Common.Database.Insert_anagrafica_vaccini_summary(wResponse.getData());

                    if (var.isVisible())
                        var.newDataAvailable();

                    waiting_bar.dismiss();
                    getdeliveries();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                waiting_bar.dismiss();
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getdeliveries()
    {
        Retrofit retrofit= NetworkClient.getRetrofitClient();

        API VacciniAPIs = retrofit.create(API.class);

        Call call = VacciniAPIs.getVaccinesDeliveries();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
              /*This is the success callback. Though the response type is JSON, with Retrofit we get
              the response in the form of WResponse POJO class
              */
                if (response.body()!=null) {
                    consegne_vaccini_dataset wResponse = (consegne_vaccini_dataset) response.body();
                    Common.Database.Insert_Deliveries(wResponse.getData());

                    getSummaryVaccini();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getSummaryVaccini()
    {
        Retrofit retrofit= NetworkClient.getRetrofitClient();

        API VacciniAPIs = retrofit.create(API.class);

        Call call = VacciniAPIs.getSummary_by_Location();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
              /*This is the success callback. Though the response type is JSON, with Retrofit we get
              the response in the form of WResponse POJO class
              */
                if (response.body()!=null) {
                    vaccini_summary_dataset wResponse = (vaccini_summary_dataset) response.body();
                    Common.Database.Insert_vaccini_summary(wResponse.getData());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getLastUpdate(fragment_summary_by_age var)
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
                    Check_Update(wResponse.ultimo_aggiornamento, var);
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

    public void goToSummary()
    {
        Fragment fragment = fragment_summary_by_age.newInstance();
        String tag=getString(R.string.fragment_summary_by_age);
        Show_Fragment(fragment, tag);
    }

    public void goToDelivery_Group()
    {
        Fragment fragment = fragment_delivery_group.newInstance();
        String tag=getString(R.string.fragment_delivery_group);
        Show_Fragment(fragment, tag);
    }

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

    //region Wait Dialog

    public ProgressDialog getprogressDialog()
    {
        ProgressDialog var = new ProgressDialog(MainActivity.this);
        var.setMessage(getString(R.string.Progress_Text));
        var.setTitle(getString(R.string.Progress_Title));
        var.setIndeterminate(true);
        var.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        return var;
    }

    //endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.action_info))
                        .setMessage(String.format(getString(R.string.Info_About), "covid19-opendata-vaccini", "Developers Italia", "Github", "stefano.casagrande@gmail.com"))
                        .setPositiveButton("Ok", null)
                        .show();

                return true;
            case R.id.action_help:
                Show_Help();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Show_Help()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Instruction_Title))
                .setMessage(getString(R.string.Instruction_Text))
                .setPositiveButton(getString(R.string.Dont_Show_Anymore), (dialog2, which) -> Common.Database.Set_Configurazione("HIDE_INSTRUCTION","1"))
                .show();
    }
}