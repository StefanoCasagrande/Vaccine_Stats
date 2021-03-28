package it.stefanocasagrande.vaccini_stats.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import androidx.core.util.Pair;

import java.text.DateFormatSymbols;
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
    TextView tv_media;
    static String area_name;

    public fragment_storico_andamento() {
        // Required empty public constructor
    }

    public static fragment_storico_andamento newInstance(String p_area_name) {
        fragment_storico_andamento fragment = new fragment_storico_andamento();
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
        View v = inflater.inflate(R.layout.fragment_storico_andamento, container, false);

        et_data_1 = v.findViewById(R.id.et_data_1);
        et_data_2 = v.findViewById(R.id.et_data_2);

        tv_media = v.findViewById(R.id.tv_media);

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -6);  // number of days to add

        et_data_1.setText(sdf.format(c.getTime()));
        et_data_2.setText(sdf.format(new Date()));

        et_data_1.setOnClickListener((View v2)-> Select_Data_Range());
        et_data_2.setOnClickListener((View v2)-> Select_Data_Range());

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

                String[] weekdays = new DateFormatSymbols(Locale.ITALIAN).getWeekdays();
                txt_title.setText(String.format("%s %s", weekdays[c.get(Calendar.DAY_OF_WEEK)], sdf.format(c.getTime())));

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

        Load_Data();

        // don't forget to refresh the drawing
        chart.invalidate();
        //endregion

        return v;
    }

    public void Select_Data_Range()
    {
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.dateRangePicker();

        builder.setCalendarConstraints(limitRange().build());
        builder.setTitleText("Select Date Range");
        MaterialDatePicker<Pair<Long, Long>> pickerRange = builder.build();
        pickerRange.show(getChildFragmentManager(), pickerRange.toString());

        pickerRange.addOnPositiveButtonClickListener(selection -> {

            String myFormat = "dd/MM/yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

            et_data_1.setText(sdf.format(selection.first));
            et_data_2.setText(sdf.format(selection.second));

            Load_Data();
        });
    }

    public CalendarConstraints.Builder limitRange()
    {
        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(new Date());

        int startYear = 2020;
        int startMonth = 12;
        int startDate = 16;

        calendarStart.set(startYear, startMonth - 1, startDate - 1);

        long minDate = calendarStart.getTimeInMillis();
        long maxDate = calendarEnd.getTimeInMillis();

        constraintsBuilderRange.setStart(minDate);
        constraintsBuilderRange.setEnd(maxDate);
        constraintsBuilderRange.setValidator(new RangeValidator(minDate, maxDate));

        return constraintsBuilderRange;
    }

    public void Load_Data()
    {
        if (setData()) {

            for (IDataSet set : chart.getData().getDataSets())
                set.setDrawValues(chart.getData().getDataSets().get(0).getEntryCount() <= 15);

            chart.getAxisLeft().setDrawLabels(chart.getData().getDataSets().get(0).getEntryCount() > 15);

            // redraw
            chart.invalidate();
        }
    }

    private boolean setData() {

        List<somministrazioni_data> lista = Common.Database.get_Somministrazioni(get_int_from_DDMMYYYY(et_data_1.getText().toString()),get_int_from_DDMMYYYY(et_data_2.getText().toString()), area_name);

        int totale = 0;

        for(somministrazioni_data var : lista)
                totale += var.totale;

        if (lista.size()==0)
            return false;

        String data_selezionata = String.valueOf(lista.get(0).data_somministrazione);
        String Year=data_selezionata.substring(0,4);
        String Month=data_selezionata.substring(4,6);
        String Day=data_selezionata.substring(6,8);
        et_data_1.setText(String.format("%s/%s/%s", Day, Month, Year));

        data_selezionata = String.valueOf(lista.get(lista.size()-1).data_somministrazione);
        Year=data_selezionata.substring(0,4);
        Month=data_selezionata.substring(4,6);
        Day=data_selezionata.substring(6,8);
        et_data_2.setText(String.format("%s/%s/%s", Day, Month, Year));

        tv_media.setText(String.format(getString(R.string.Media_Desc), String.valueOf(lista.size()), Common.AddDotToInteger(totale/lista.size())));

        if (!TextUtils.isEmpty(area_name))
            tv_media.setText(String.format("%s %s %s", tv_media.getText(), getString(R.string.Area_Of), area_name));

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

        return true;
    }

}

class RangeValidator implements CalendarConstraints.DateValidator {

    long minDate, maxDate;

    RangeValidator(long minDate, long maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    RangeValidator(Parcel parcel) {
        minDate = parcel.readLong();
        maxDate = parcel.readLong();
    }

    @Override
    public boolean isValid(long date) {
        return !(minDate > date || maxDate < date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static final Parcelable.Creator<RangeValidator> CREATOR = new Parcelable.Creator<RangeValidator>() {

        @Override
        public RangeValidator createFromParcel(Parcel parcel) {
            return new RangeValidator(parcel);
        }

        @Override
        public RangeValidator[] newArray(int size) {
            return new RangeValidator[size];
        }
    };

}

