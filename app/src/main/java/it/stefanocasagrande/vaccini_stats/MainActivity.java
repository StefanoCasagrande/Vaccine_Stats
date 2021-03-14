package it.stefanocasagrande.vaccini_stats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import it.stefanocasagrande.vaccini_stats.ui.fragment_summary_by_age;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    PieChart pieChart;
    BarChart chart;

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
                R.id.nav_summary_by_age, R.id.nav_group_deliveries, R.id.nav_andamento_storico)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        CheckNetwork network = new CheckNetwork(getApplicationContext());
        network.registerNetworkCallback();

        Common.Database = new DB(this);
        Common.Database.Check_Table();
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

    // Order
    // 1. getLastUpdate ( if same value as already download stop everything )
    // 2. getSummary_by_Age
    // 3. getdeliveries
    // 4. getSummaryVaccini
    // 5. getCSVsomministrazione

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

                    try {
                        getCSVsomministrazione();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    public void getCSVsomministrazione() throws IOException {
        new DownloadFileFromURL().execute("");

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

        int fascia_0=0;
        int fascia_1=0;
        int fascia_2=0;
        int fascia_3=0;
        int fascia_4=0;
        int fascia_5=0;
        int fascia_6=0;
        int fascia_7=0;
        int fascia_8=0;

        for(anagrafica_vaccini_summary_data var : lista)
        {
            if (var.fascia_anagrafica.equals(getString(R.string.tab_text_2)))
                fascia_0=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_3)))
                fascia_1=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_4)))
                fascia_2=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_5)))
                fascia_3=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_6)))
                fascia_4=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_7)))
                fascia_5=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_8)))
                fascia_6=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_9)))
                fascia_7=var.totale;
            else if (var.fascia_anagrafica.equals(getString(R.string.tab_text_10)))
                fascia_8=var.totale;
        }

        ShowBarGraph(fascia_0, fascia_1, fascia_2, fascia_3, fascia_4, fascia_5, fascia_6, fascia_7, fascia_8);

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
                getString(R.string.tab_text_2),
                getString(R.string.tab_text_3),
                getString(R.string.tab_text_4),
                getString(R.string.tab_text_5),
                getString(R.string.tab_text_6),
                getString(R.string.tab_text_7),
                getString(R.string.tab_text_8),
                getString(R.string.tab_text_9),
                getString(R.string.tab_text_10)
        };

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLables));

        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();

        chart.invalidate();
    }

    //endregion

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
        btn_ok.setOnClickListener(v -> custom_dialog.cancel());

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

    public void Show_News()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.News_Title))
                .setMessage(getString(R.string.News_Text))
                .setPositiveButton(getString(R.string.Dont_Show_Anymore), (dialog2, which) -> Common.Database.Set_Configurazione("HIDE_NEWS_2021_03","1"))
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
                            Common.Database.Set_Configurazione("HIDE_NEWS_2021_03","1");
                        })
                .show();
    }

    //region Download

    private List<List<String>> Read_CSV(String FilePath, int numero_campi)
    {
        List<List<String>> list = new ArrayList<>();

        try {
            InputStream inputStream = new FileInputStream(FilePath);
            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(inputreader);

            String myLine;
            int i=0;

            while((myLine=buffreader.readLine())!=null)
            {
                i++;

                if(i==1)
                    continue; //La prima riga contiene l'intestazione

                if (myLine.endsWith(","))
                    myLine=myLine+" ";

                String[] values = myLine.split(",");

                List<String> valori = new ArrayList<>();
                if(values.length != numero_campi )
                    continue;
                else
                    valori.addAll(Arrays.asList(values).subList(0, numero_campi));

                list.add(valori);
            }

        }
        catch (IOException e)
        {
            Log.d("Main", e.toString());
        }

        return list;
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

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
                byte data[] = new byte[1024];

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

            Common.Database.Insert_Somministrazioni(Read_CSV(dir+"somministrazioni_vaccini_summary_latest.csv", 17));
        }

    }

    //endregion
}