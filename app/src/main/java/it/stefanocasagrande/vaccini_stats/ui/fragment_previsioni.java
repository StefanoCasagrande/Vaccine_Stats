package it.stefanocasagrande.vaccini_stats.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.somministrazioni_data;

import static it.stefanocasagrande.vaccini_stats.Common.Common.get_int_from_DDMMYYYY;

public class fragment_previsioni extends Fragment {


    public fragment_previsioni() {
        // Required empty public constructor
    }

    public static fragment_previsioni newInstance() {
        fragment_previsioni fragment = new fragment_previsioni();
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
        View v = inflater.inflate(R.layout.fragment_previsioni, container, false);

        int soglia_immunita = 42171247;
        int fascia_rischio = 30240429;
        int media_vaccinazioni_7_giorni;
        int vaccinati = Common.Database.Get_Totale_Vaccini_Somministrazioni();

        TextView tv_herd_immunity_text = v.findViewById(R.id.tv_herd_immunity_text);
        TextView tv_vaccinazione_massa_text = v.findViewById(R.id.tv_vaccinazione_massa_text);
        TextView tv_formule = v.findViewById(R.id.tv_formule);

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -2);

        String data_2 = (sdf.format(c.getTime()));

        c.setTime(new Date());
        c.add(Calendar.DATE, -8);

        String data_1 =  (sdf.format(c.getTime()));

        int totale = 0;

        List<somministrazioni_data> lista = Common.Database.get_Somministrazioni(get_int_from_DDMMYYYY(data_1),get_int_from_DDMMYYYY(data_2), "");

        if (lista.size()!=0)
        {
            for(somministrazioni_data var : lista)
                totale += var.totale;

            media_vaccinazioni_7_giorni = totale/lista.size();

            float giorni = (float)((soglia_immunita*2)-vaccinati)/media_vaccinazioni_7_giorni;

            c.setTime(new Date());
            c.add(Calendar.DATE, (int)giorni);

            tv_herd_immunity_text.setText(Html.fromHtml(String.format(getString(R.string.Herd_Immunity_Text), Common.AddDotToInteger(media_vaccinazioni_7_giorni), "<b>" + (((int) giorni)) +"</b>", "<b>" + sdf.format(c.getTime()) +"</b>")));

            float giorni_massa = (float)((fascia_rischio*2)-vaccinati)/media_vaccinazioni_7_giorni;

            c.setTime(new Date());
            c.add(Calendar.DATE, (int)giorni_massa);

            tv_vaccinazione_massa_text.setText(Html.fromHtml(String.format(getString(R.string.Vaccinazione_Massa_Text), "<b>" + (((int) giorni_massa)) +"</b>", "<b>" + sdf.format(c.getTime()) +"</b>")));

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);

            tv_formule.setText(String.format(getString(R.string.Math_Formula), Common.AddDotToInteger(soglia_immunita), Common.AddDotToInteger(vaccinati), Common.AddDotToInteger(media_vaccinazioni_7_giorni), (df.format(giorni)), Common.AddDotToInteger(fascia_rischio), (df.format(giorni_massa))));
        }

        return v;
    }
}