package it.stefanocasagrande.vaccini_stats.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.MainActivity;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;
import it.stefanocasagrande.vaccini_stats.json_classes.somministrazioni_data;
import it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary.vaccini_summary_data;

import static it.stefanocasagrande.vaccini_stats.Common.Common.get_int_from_Date;
import static it.stefanocasagrande.vaccini_stats.Common.Common.hideKeyboard;

public class fragment_delivery_details extends Fragment {

    PieChart pieChart;

    static String area_name;
    TextView tv_location;

    TextView tv_doses_administered;
    TextView tv_total_delivered;
    TextView tv_total_delivered_desc;
    TextView tv_population;
    TextView tv_percent;

    public fragment_delivery_details() {
        // Required empty public constructor
    }

    public static fragment_delivery_details newInstance(String p_area_name) {
        fragment_delivery_details fragment = new fragment_delivery_details();
        Bundle args = new Bundle();
        area_name = p_area_name;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v = inflater.inflate(R.layout.fragment_delivery_details, container, false);

        tv_location = v.findViewById(R.id.tv_location);
        tv_population = v.findViewById(R.id.tv_population);
        tv_percent = v.findViewById(R.id.tv_percent);

        tv_doses_administered = v.findViewById(R.id.tv_doses_administered);
        tv_total_delivered = v.findViewById(R.id.tv_total_delivered);
        tv_total_delivered_desc = v.findViewById(R.id.tv_total_delivered_desc);

        pieChart = v.findViewById(R.id.pieChart_view);
        initPieChart();

        pieChart.setOnChartValueSelectedListener(new pieChartOnChartValueSelectedListener());

        TextView tv_last_update = v.findViewById(R.id.tv_last_update);
        tv_last_update.setText(String.format("%s: %s", getString(R.string.Last_Update), Common.get_dd_MM_yyyy(Common.Database.Get_Configurazione("ultimo_aggiornamento"))));

        final ProgressDialog waiting_bar = ((MainActivity)getActivity()).getprogressDialog();
        waiting_bar.show();

        if (Load_Data(area_name))
            waiting_bar.dismiss();

        hideKeyboard(getActivity());

        Button btn_doses_administered_detail = v.findViewById(R.id.btn_doses_administered_detail);
        btn_doses_administered_detail.setOnClickListener(v2 -> {
            ((MainActivity)getActivity()).Show_Administered_Doses_per_Area(area_name);
        });

        Button btn_category_graph = v.findViewById(R.id.btn_category_graph);
        btn_category_graph.setOnClickListener(v2 -> {

            List<somministrazioni_data> lista = Common.Database.get_Somministrazioni(0,99990101, area_name);

            int categoria_operatori_sanitari_sociosanitari=0;
            int categoria_personale_non_sanitario=0;
            int categoria_ospiti_rsa=0;
            int categoria_forze_armate=0;
            int categoria_personale_scolastico = 0;
            int categoria_altro = 0;
            int fragili=0;

            for (somministrazioni_data var : lista)
            {
                categoria_operatori_sanitari_sociosanitari += var.categoria_operatori_sanitari_sociosanitari;
                categoria_personale_non_sanitario += var.categoria_personale_non_sanitario;
                categoria_ospiti_rsa += var.categoria_ospiti_rsa;
                categoria_forze_armate += var.categoria_forze_armate;
                categoria_personale_scolastico += var.categoria_personale_scolastico;
                fragili += var.categoria_fragili;
                categoria_altro += var.categoria_over80+var.categoria_over75+var.categoria_over70+var.categoria_altro+var.categoria_60_69+var.categoria_70_79;
            }
                ((MainActivity)getActivity()).Show_Graph(
                        categoria_operatori_sanitari_sociosanitari,
                        categoria_personale_non_sanitario,
                        categoria_ospiti_rsa,
                        categoria_forze_armate,
                        categoria_personale_scolastico,
                        categoria_altro,
                        fragili
                );
        });

        return v;
    }

    public boolean Load_Data(String area_name)
    {
        List<consegne_vaccini_data> list_to_load = Common.Database.Get_Deliveries(area_name);
        List<vaccini_summary_data> list_administered = Common.Database.Get_vaccini_summary(area_name);

        int popolazione=Common.Database.Get_Popolazione("", list_to_load.get(0).nome_area);
        int somministrate_prima_dose = list_administered.get(0).prima_dose;
        int somministrate = list_administered.get(0).dosi_somministrate;

        tv_location.setText(list_to_load.get(0).nome_area);
        tv_population.setText(Common.AddDotToInteger(popolazione));
        tv_doses_administered.setText(Common.AddDotToInteger(somministrate));
        tv_total_delivered.setText(Common.AddDotToInteger(list_administered.get(0).dosi_consegnate));

        if (popolazione>0)
        {
            double percent = (((somministrate_prima_dose)*100)/ (double)popolazione);
            tv_percent.setText(String.format(getString(R.string.Percent_Population), String.format("%.2f", percent)));
        }

        int doses_type1=0;
        int doses_type2=0;
        int doses_type3=0;
        int doses_type4=0;
        int doses_type5=0;

        String last_consegna="";

        for (consegne_vaccini_data var : list_to_load)
        {
            if (last_consegna.equals("") || (get_int_from_Date(var.data_consegna)>get_int_from_Date(last_consegna)))
                last_consegna=var.data_consegna;

            if (var.fornitore.toUpperCase().equals(getString(R.string.Pfizer_BioNTech).toUpperCase()))
                doses_type1 += var.numero_dosi;
            else if (var.fornitore.toUpperCase().equals(getString(R.string.AstraZeneca).toUpperCase()) || var.fornitore.toUpperCase().equals(getString(R.string.AstraZeneca_2).toUpperCase()))
                doses_type2 += var.numero_dosi;
            else if (var.fornitore.toUpperCase().equals(getString(R.string.Moderna).toUpperCase()))
                doses_type3 += var.numero_dosi;
            else if (var.fornitore.toUpperCase().equals(getString(R.string.janssen).toUpperCase()))
                doses_type4 += var.numero_dosi;
            else
                doses_type5 += var.numero_dosi;
        }

        tv_total_delivered_desc.setText(String.format("%s%s%s - %s", getString(R.string.Doses_Delivered), System.lineSeparator(), getString(R.string.Last_delivery), Common.get_dd_MM_yyyy(last_consegna)));

        showPieChart(doses_type1, doses_type2, doses_type3, doses_type4, doses_type5);

        return true;
    }

    private class pieChartOnChartValueSelectedListener implements OnChartValueSelectedListener {

        @Override
        public void onValueSelected(Entry e, Highlight h) {


            final Dialog custom_dialog = new Dialog(getActivity());
            custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            custom_dialog.setContentView(R.layout.alertdialog_detail);
            custom_dialog.setCancelable(true);

            TextView txt_title= custom_dialog.findViewById(R.id.textView1);
            txt_title.setText(((PieEntry) e).getLabel());

            TextView txt_detail = custom_dialog.findViewById(R.id.textView2);
            txt_detail.setText(String.format("%s: %s", getString(R.string.Doses_Delivered), Common.AddDotToInteger((int)e.getY())));
            custom_dialog.show();
        }

        @Override
        public void onNothingSelected() {

        }
    }

    private void initPieChart(){


        //remove the description label on the lower left corner, default true if not set
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(R.color.Dividing_Bar);

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

    private void showPieChart(int doses_type1, int doses_type2, int doses_type3, int doses_type4, int  doses_type5){

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        //initializing data
        Map<String, Integer> typeAmountMap = new HashMap<>();
        typeAmountMap.put(getString(R.string.Pfizer_BioNTech_Short),doses_type1);
        typeAmountMap.put(getString(R.string.AstraZeneca),doses_type2);
        typeAmountMap.put(getString(R.string.Moderna),doses_type3);

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#fb8500"));
        colors.add(Color.parseColor("#219ebc"));
        colors.add(Color.parseColor("#d62828"));

        if (doses_type4>0) {
            colors.add(Color.parseColor("#7400b8"));
            typeAmountMap.put(getString(R.string.janssen), doses_type4);
        }

        if (doses_type5>0) {
            colors.add(Color.parseColor("#00b874"));
            typeAmountMap.put(getString(R.string.Other_Vaccines), doses_type5);
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

        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}