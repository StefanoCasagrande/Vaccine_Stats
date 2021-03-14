package it.stefanocasagrande.vaccini_stats.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.somministrazioni_data;

import static it.stefanocasagrande.vaccini_stats.Common.Common.get_Date_from_DDMMYYYY;
import static it.stefanocasagrande.vaccini_stats.Common.Common.get_int_from_DDMMYYYY;

public class fragment_storico_andamento extends Fragment {

    LineChart chart;
    EditText et_data_1;
    EditText et_data_2;
    int id_editext;
    TextView tv_media;

    public fragment_storico_andamento() {
        // Required empty public constructor
    }

    public static fragment_storico_andamento newInstance() {
        fragment_storico_andamento fragment = new fragment_storico_andamento();
        Bundle args = new Bundle();

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
        View v = inflater.inflate(R.layout.fragment_storico_andamento, container, false);

        et_data_1 = v.findViewById(R.id.et_data_1);
        et_data_2 = v.findViewById(R.id.et_data_2);

        tv_media = v.findViewById(R.id.tv_media);

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -7);  // number of days to add

        et_data_1.setText(sdf.format(c.getTime()));
        et_data_2.setText(sdf.format(new Date()));

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if (id_editext==R.id.et_data_1)
                updateText(0);
            else
                updateText(1);
        };

        et_data_1.setOnClickListener((View v2)-> {
            id_editext = et_data_1.getId();

            new DatePickerDialog(getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        et_data_2.setOnClickListener((View v2)-> {
                    id_editext = et_data_2.getId();

                    new DatePickerDialog(getContext(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        //region Grafico
        chart = v.findViewById(R.id.chart1);
        chart.setViewPortOffsets(0, 0, 0, 0);
        chart.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.my_gradient_drawable));

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                final Dialog custom_dialog = new Dialog(getActivity());
                custom_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                custom_dialog.setContentView(R.layout.alertdialog_detail);
                custom_dialog.setCancelable(true);

                TextView txt_title= custom_dialog.findViewById(R.id.textView1);

                Date start_date = get_Date_from_DDMMYYYY(et_data_1.getText().toString());

                Calendar c = Calendar.getInstance();
                c.setTime(start_date);
                c.add(Calendar.DATE, (int) e.getX());

                txt_title.setText(sdf.format(c.getTime()));

                TextView txt_detail = custom_dialog.findViewById(R.id.textView2);
                txt_detail.setText(String.format("%s: %s", getString(R.string.doses_administered), Common.AddDotToInteger((int)e.getY())));
                custom_dialog.show();
            }

            @Override
            public void onNothingSelected()
            {

            }
        });

        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);

        XAxis x = chart.getXAxis();
        x.setEnabled(false);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(true);
        y.setAxisLineColor(Color.WHITE);

        chart.getAxisRight().setEnabled(false);

        chart.getLegend().setEnabled(false);

        chart.animateXY(2000, 2000);

        Load_Data(true);

        // don't forget to refresh the drawing
        chart.invalidate();
        //endregion

        return v;
    }

    Calendar myCalendar = Calendar.getInstance();

    private void updateText(int i) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        if (i==0) {
                if (myCalendar.getTime().compareTo(get_Date_from_DDMMYYYY(et_data_2.getText().toString()))<0 && myCalendar.getTime().compareTo(new Date())<0)
                {
                    et_data_1.setText(sdf.format(myCalendar.getTime()));
                    Load_Data(false);
                } else
                    Toast.makeText(getActivity(),getString(R.string.Invalid_Date), Toast.LENGTH_SHORT).show();
        }
        else {
                if (myCalendar.getTime().compareTo(get_Date_from_DDMMYYYY(et_data_1.getText().toString()))>0 && myCalendar.getTime().compareTo(new Date())<0)
                {
                    et_data_2.setText(sdf.format(myCalendar.getTime()));
                    Load_Data(false);
                } else
                    Toast.makeText(getActivity(),getString(R.string.Invalid_Date), Toast.LENGTH_SHORT).show();
        }
    }


    public void Load_Data(Boolean update_media)
    {
        setData(update_media);

        for (IDataSet set : chart.getData().getDataSets())
            set.setDrawValues(chart.getData().getDataSets().get(0).getEntryCount()<=15);

        chart.getAxisLeft().setDrawLabels(chart.getData().getDataSets().get(0).getEntryCount()>15);

        // redraw
        chart.invalidate();
    }

    private void setData(Boolean update_media) {

        List<somministrazioni_data> lista = Common.Database.get_Somministrazioni(get_int_from_DDMMYYYY(et_data_1.getText().toString()),get_int_from_DDMMYYYY(et_data_2.getText().toString()));

        if (update_media)
        {
            int totale = 0;

            for(somministrazioni_data var : lista)
                totale += var.totale;

            tv_media.setText(String.format(getString(R.string.Media_Desc), Common.AddDotToInteger(totale/7)));
        }
        
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            values.add(new Entry(i, lista.get(i).totale));
        }

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(Color.WHITE);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setValueTextColor(Color.WHITE);
            set1.setColor(Color.BLACK);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());

            // create a data object with the data sets
            LineData data = new LineData(set1);
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            chart.setData(data);
        }
    }

}

