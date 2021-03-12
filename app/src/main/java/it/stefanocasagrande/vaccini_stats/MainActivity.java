package it.stefanocasagrande.vaccini_stats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    PieChart pieChart;

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
            case Common.Back_To_CloseApp:
                finishAndRemoveTask();
                break;
            default:
                Common.Back_Action=Common.Back_To_CloseApp;
                Toast.makeText(this,getString(R.string.Last_Back_Alert), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //region CheckLastUpdate

    public void Check_Update(String last_update, fragment_summary_by_age var)
    {
        if ((Common.Database.Get_Configurazione("ultimo_aggiornamento").equals("") || !Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(last_update)))
        {
            Common.Database.Set_Configurazione("ultimo_aggiornamento", last_update);
            getSummary_by_Age(var);
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
            case R.id.reload:

                Common.Data_Already_Loaded=false;
                goToSummary();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //region Graph Category

    public void Show_Graph(int operatori_sanitari_sociosanitari, int personale_non_sanitario, int ospiti_rsa, int forze_armate, int personale_scolastico, int over_80)
    {
        final Dialog custom_dialog = new Dialog(this);
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.alertdialog_category_graph);
        custom_dialog.setCancelable(true);

        pieChart = custom_dialog.findViewById(R.id.pieChart_view);
        initPieChart();

        Button btn_ok = custom_dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                custom_dialog.cancel();
            }
        });

        int totale = operatori_sanitari_sociosanitari+ personale_non_sanitario+ ospiti_rsa+ forze_armate+ personale_scolastico+ over_80;

        showPieChart((double)(operatori_sanitari_sociosanitari*100)/totale, (double)(personale_non_sanitario*100)/totale, (double)(ospiti_rsa*100)/totale, (double)(forze_armate*100)/totale, (double)(personale_scolastico*100)/totale, (double)(over_80*100)/totale);

        custom_dialog.show();
    }

    private void initPieChart(){

        //remove the description label on the lower left corner, default true if not set
        pieChart.getDescription().setEnabled(false);

        //enabling the user to rotate the chart, default true
        pieChart.setRotationEnabled(true);
        //adding friction when rotating the pie chart
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        //setting the first entry start from right hand side, default starting from top
        pieChart.setRotationAngle(0);

        //highlight the entry when it is tapped, default true if not set
        pieChart.setHighlightPerTapEnabled(true);
        //adding animation so the entries pop up from 0 degree
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

    }

    private void showPieChart(double operatori_sanitari_sociosanitari, double personale_non_sanitario, double ospiti_rsa, double forze_armate, double personale_scolastico, double over_80)
    {

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        //initializing data
        Map<String, Double> typeAmountMap = new HashMap<>();
        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();


        if (forze_armate>0)
        {
            typeAmountMap.put(getString(R.string.law_enforcement),forze_armate);
            colors.add(Color.parseColor("#ffb703"));
        }

        if (operatori_sanitari_sociosanitari>0)
        {
            typeAmountMap.put(getString(R.string.Health_Workers),operatori_sanitari_sociosanitari);
            colors.add(Color.parseColor("#8ecae6"));
        }

        if (over_80>0)
        {
            typeAmountMap.put(getString(R.string.Others),over_80);
            colors.add(Color.parseColor("#e07a5f"));
        }

        if (personale_non_sanitario>0)
        {
            typeAmountMap.put(getString(R.string.non_health_care_professional),personale_non_sanitario);
            colors.add(Color.parseColor("#219ebc"));
        }


        if (ospiti_rsa>0)
        {
            typeAmountMap.put(getString(R.string.RSA_Guests),ospiti_rsa);
            colors.add(Color.parseColor("#687175"));
        }

        if (personale_scolastico>0)
        {
            typeAmountMap.put(getString(R.string.School_Staff),personale_scolastico);
            colors.add(Color.parseColor("#fb8500"));
        }



        //input data and fit data into pie chart entry
        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,"");
        //setting text size of the value
        pieDataSet.setValueTextSize(15f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
        pieData.setDrawValues(true);

        pieData.setValueFormatter(new PercentFormatter());

        pieChart.getLegend().setEnabled(false);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    //endregion

    public void Show_Help()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Instruction_Title))
                .setMessage(getString(R.string.Instruction_Text))
                .setPositiveButton(getString(R.string.Dont_Show_Anymore), (dialog2, which) -> Common.Database.Set_Configurazione("HIDE_INSTRUCTION","1"))
                .show();
    }
}