package it.stefanocasagrande.vaccini_stats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.Common.DB;
import it.stefanocasagrande.vaccini_stats.Network.API;
import it.stefanocasagrande.vaccini_stats.Network.CheckNetwork;
import it.stefanocasagrande.vaccini_stats.Network.NetworkClient;
import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_data;
import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.last_update_dataset;
import it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary.vaccini_summary_dataset;
import it.stefanocasagrande.vaccini_stats.ui.fragment_delivery_details;
import it.stefanocasagrande.vaccini_stats.ui.fragment_delivery_group;
import it.stefanocasagrande.vaccini_stats.ui.fragment_storico_andamento;
import it.stefanocasagrande.vaccini_stats.ui.fragment_summary_by_age;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    BarChart chart;

    ProgressDialog waiting_bar;

    static String url_github="https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/";
    static String url_sole24="https://lab24.ilsole24ore.com/_json/vaccini/";

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
                R.id.nav_summary_by_age, R.id.nav_group_deliveries, R.id.nav_andamento_storico, R.id.nav_previsioni)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        CheckNetwork network = new CheckNetwork(getApplicationContext());
        network.registerNetworkCallback();

        Common.Database = new DB(this);
        Common.Database.Check_Table();

        try {
            Common.Database.Insert_Popolazione(Read_CSV(getAssets().open("popolazione.csv"), 3));
        }
        catch (Exception ex)
        {

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
        if ((Common.Database.Get_Configurazione("ultimo_aggiornamento").equals("")
                || !Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(last_update)
                || Common.Database.get_Somministrazioni(20210101,20210102,"").size()==0
                || Common.Database.get_Prime_Dosi_Null()==0))
        {
            Common.Database.Set_Configurazione("ultimo_aggiornamento", last_update);
            getSummary_by_Age(var);
        }
        else
            Toast.makeText(this,getString(R.string.Already_Update), Toast.LENGTH_SHORT).show();
    }

    //endregion

    //region API

    // Order
    // 1. getLastUpdate ( if same value as already download stop everything )
    // 2. getSummary_by_Age
    // 3. getdeliveries
    // 4. getSummaryVaccini
    // 5. getAnagraficaRegione
    // 6. getCSVsomministrazione

    public void getSummary_by_Age(fragment_summary_by_age var)
    {
        waiting_bar = getprogressDialog();
        waiting_bar.show();

        Retrofit retrofit= NetworkClient.getRetrofitClient(url_github, true);

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

                    getdeliveries();
                }
                else
                    waiting_bar.dismiss();
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
        Retrofit retrofit= NetworkClient.getRetrofitClient(url_github, false);

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
                }
                else
                    waiting_bar.dismiss();

                getSummaryVaccini();
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
                waiting_bar.dismiss();
            }
        });
    }

    public void getSummaryVaccini()
    {
        Retrofit retrofit= NetworkClient.getRetrofitClient(url_github, false);

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
                else
                    waiting_bar.dismiss();

                getAnagraficaRegione();
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(),String.format(getString(R.string.API_Error), t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getAnagraficaRegione()
    {
        Retrofit retrofit= NetworkClient.getRetrofitClient(url_sole24, true);

        API VacciniAPIs = retrofit.create(API.class);

        Call call = VacciniAPIs.getAnagraficaRegione();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
              /*This is the success callback. Though the response type is JSON, with Retrofit we get
              the response in the form of WResponse POJO class
              */
                if (response.body()!=null) {
                    JsonObject wResponse = (JsonObject) response.body();
                    Common.Database.Insert_Anagrafica_Regione(wResponse);
                }
                else
                    waiting_bar.dismiss();

                try {
                    getCSVsomministrazione();
                } catch (IOException e) {
                    waiting_bar.dismiss();
                    e.printStackTrace();
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
        Retrofit retrofit= NetworkClient.getRetrofitClient(url_github, false);

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

    public void getCSVsomministrazione() throws IOException {
        new DownloadFileSomministrazioni().execute("");
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
                break;
            case R.id.reload:

                if (GlobalVariables.isNetworkConnected)
                {
                    Common.Database.Set_Configurazione("ultimo_aggiornamento","2020-01-01");
                    Common.Data_Already_Loaded=false;
                    goToSummary();
                }
                else
                    Toast.makeText(this, getString(R.string.Internet_Missing), Toast.LENGTH_LONG).show();
                
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    //region Graph Age

    public void Show_Age_Graph()
    {
        final Dialog custom_dialog = new Dialog(this);
        custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_dialog.setContentView(R.layout.alertdialog_age_graph);
        custom_dialog.setCancelable(true);

        chart = custom_dialog.findViewById(R.id.chart1);

        Button btn_ok = custom_dialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> custom_dialog.cancel());

        InitBarGraph();

        List<anagrafica_vaccini_summary_data> lista = Common.Database.Get_anagrafica_vaccini_summary();

        int fascia_1=0;
        int fascia_2=0;
        int fascia_3=0;
        int fascia_4=0;
        int fascia_5=0;
        int fascia_6=0;
        int fascia_7=0;
        int fascia_8=0;
        int fascia_9=0;

        for(anagrafica_vaccini_summary_data var : lista)
        {
            if (var.fascia_anagrafica.equals(getString(R.string.tab_age_1)))
                fascia_1=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_2)))
                fascia_2=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_3)))
                fascia_3=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_4)))
                fascia_4=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_5)))
                fascia_5=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_6)))
                fascia_6=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_7)))
                fascia_7=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_8)))
                fascia_8=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_age_9)))
                fascia_9=var.totale;
        }

        ShowBarGraph(fascia_1, fascia_2, fascia_3, fascia_4, fascia_5, fascia_6, fascia_7, fascia_8, fascia_9);

        custom_dialog.show();
    }

    private void InitBarGraph()
    {
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.animateY(1500);
        chart.getLegend().setEnabled(false);
    }

    private void ShowBarGraph(int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7, int val8)
    {
        ArrayList<BarEntry> values = new ArrayList<>();

        values.add((new BarEntry(0, val0)));
        values.add((new BarEntry(1, val1)));
        values.add((new BarEntry(2, val2)));
        values.add((new BarEntry(3, val3)));
        values.add((new BarEntry(4, val4)));
        values.add((new BarEntry(5, val5)));
        values.add((new BarEntry(6, val6)));
        values.add((new BarEntry(7, val7)));
        values.add((new BarEntry(8, val8)));

        BarDataSet set1 = new BarDataSet(values, "Data Set");
        set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
        set1.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        chart.setData(data);
        chart.setFitBars(true);

        set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
        set1.setValues(values);
        set1.setBarBorderWidth(set1.getBarBorderWidth() == 1.f ? 0.f : 1.f);

        String[] xAxisLables = new String[]{
                getString(R.string.tab_age_1),
                getString(R.string.tab_age_2),
                getString(R.string.tab_age_3),
                getString(R.string.tab_age_4),
                getString(R.string.tab_age_5),
                getString(R.string.tab_age_6),
                getString(R.string.tab_age_7),
                getString(R.string.tab_age_8),
                getString(R.string.tab_age_9)
        };

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLables));

        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();

        chart.invalidate();
    }

    //endregion

    public void Show_Administered_Doses_per_Area(String p_area_name)
    {
        Fragment fragment = fragment_storico_andamento.newInstance(p_area_name);
        String tag=getString(R.string.fragment_storico_andamento);
        Show_Fragment(fragment, tag);
    }

    public void Show_News()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.News_Title))
                .setMessage(getString(R.string.News_Text))
                .setPositiveButton(getString(R.string.Dont_Show_Anymore), (dialog2, which) -> Common.Database.Set_Configurazione("HIDE_NEWS_2021_06","1"))
                .show();
    }

    public void Show_Help()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.Instruction_Title))
                .setMessage(getString(R.string.Instruction_Text))
                .setPositiveButton(getString(R.string.Dont_Show_Anymore), (dialog2, which) ->
                        {
                            Common.Database.Set_Configurazione("HIDE_INSTRUCTION","1");
                            Common.Database.Set_Configurazione("HIDE_NEWS_2021_06","1");
                        })
                .show();
    }

    //region Download

    private List<List<String>> Read_CSV(InputStream inputStream, int numero_campi)
    {
        List<List<String>> list = new ArrayList<>();

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);

        String myLine;
        try {
        while((myLine=buffreader.readLine())!=null)
        {
            if (myLine.endsWith(","))
                myLine=myLine+" ";

            String[] values = myLine.split(",");

            List<String> valori;
            if(values.length < numero_campi )
                continue;
            else
                valori = new ArrayList<>(Arrays.asList(values));

            list.add(valori);
        }
        }
        catch (IOException e)
        {
            Log.d("Main", e.toString());
        }

        return list;
    }

    private List<List<String>> Read_CSV(String FilePath, int numero_campi)
    {
        List<List<String>> list = new ArrayList<>();

        try {
            InputStream inputStream = new FileInputStream(FilePath);
            list = Read_CSV(inputStream, numero_campi);

        }
        catch (IOException e)
        {
            Log.d("Main", e.toString());
        }

        return list;
    }

    class DownloadFileSomministrazioni extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting download");

        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {

                File var = getBaseContext().getExternalFilesDir(null);
                File FilesPath = new File(var.getAbsolutePath());
                String dir = FilesPath.toString() + "/";

                System.out.println("Downloading");
                URL url = new URL("https://raw.githubusercontent.com/italia/covid19-opendata-vaccini/master/dati/somministrazioni-vaccini-summary-latest.csv");

                URLConnection conection = url.openConnection();
                conection.connect();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file

                OutputStream output = new FileOutputStream(dir+"somministrazioni_vaccini_summary_latest.csv");
                byte[] data = new byte[1024];

                while ((count = input.read(data)) != -1)
                    output.write(data, 0, count);

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }



        /**
         * After completing background task
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            System.out.println("Downloaded");

            File var = getBaseContext().getExternalFilesDir(null);
            File FilesPath = new File(var.getAbsolutePath());
            String dir = FilesPath.toString() + "/";

            Common.Database.Insert_Somministrazioni(Read_CSV(dir+"somministrazioni_vaccini_summary_latest.csv", 11));
            waiting_bar.dismiss();

        }

    }

    //endregion
}