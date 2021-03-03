package it.stefanocasagrande.vaccini_stats.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import it.stefanocasagrande.vaccini_stats.Adapters.Delivery_Details_Adapter;
import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;

public class fragment_delivery_details extends Fragment {

    static String area_name;
    TextView tv_location;

    TextView tv_vaccine_type1;
    TextView tv_vaccine_type1_doses;

    TextView tv_vaccine_type2;
    TextView tv_vaccine_type2_doses;

    TextView tv_vaccine_type3;
    TextView tv_vaccine_type3_doses;

    TextView tv_vaccine_type4;
    TextView tv_vaccine_type4_doses;

    ListView list;

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

        tv_vaccine_type1 = v.findViewById(R.id.tv_vaccine_type1);
        tv_vaccine_type1_doses = v.findViewById(R.id.tv_vaccine_type1_doses);

        tv_vaccine_type2 = v.findViewById(R.id.tv_vaccine_type2);
        tv_vaccine_type2_doses = v.findViewById(R.id.tv_vaccine_type2_doses);

        tv_vaccine_type3 = v.findViewById(R.id.tv_vaccine_type3);
        tv_vaccine_type3_doses = v.findViewById(R.id.tv_vaccine_type3_doses);

        tv_vaccine_type4 = v.findViewById(R.id.tv_vaccine_type4);
        tv_vaccine_type4_doses = v.findViewById(R.id.tv_vaccine_type4_doses);

        list = v.findViewById(R.id.listView);

        TextView tv_last_update = v.findViewById(R.id.tv_last_update);
        tv_last_update.setText(String.format("%s: %s", getString(R.string.Last_Update), Common.get_dd_MM_yyyy(Common.Database.Get_Configurazione("ultimo_aggiornamento"))));

        Load_Data(area_name);

        return v;
    }

    public void Load_Data(String area_name)
    {
        List<consegne_vaccini_data> list_to_load = Common.Database.Get_Deliveries(area_name);

        tv_location.setText(list_to_load.get(0).nome_area);

        tv_vaccine_type1.setText(getString(R.string.Pfizer_BioNTech));
        tv_vaccine_type2.setText(getString(R.string.AstraZeneca));
        tv_vaccine_type3.setText(getString(R.string.Moderna));
        tv_vaccine_type4.setText(getString(R.string.Other_Vaccines));

        int doses_type1=0;
        int doses_type2=0;
        int doses_type3=0;
        int doses_type4=0;

        for (consegne_vaccini_data var : list_to_load)
        {
            if (var.fornitore.toUpperCase().equals(getString(R.string.Pfizer_BioNTech).toUpperCase()))
                doses_type1 += var.numero_dosi;
            else if (var.fornitore.toUpperCase().equals(getString(R.string.AstraZeneca).toUpperCase()))
                doses_type2 += var.numero_dosi;
            else if (var.fornitore.toUpperCase().equals(getString(R.string.Moderna).toUpperCase()))
                doses_type3 += var.numero_dosi;
            else
                doses_type4 += var.numero_dosi;
        }

        tv_vaccine_type1_doses.setText(String.format("%s: %s", getString(R.string.Doses_Delivered), Common.AddDotToInteger(doses_type1)));
        tv_vaccine_type2_doses.setText(String.format("%s: %s", getString(R.string.Doses_Delivered), Common.AddDotToInteger(doses_type2)));
        tv_vaccine_type3_doses.setText(String.format("%s: %s", getString(R.string.Doses_Delivered), Common.AddDotToInteger(doses_type3)));
        tv_vaccine_type4_doses.setText(String.format("%s: %s", getString(R.string.Doses_Delivered), Common.AddDotToInteger(doses_type4)));

        Delivery_Details_Adapter adapter = new Delivery_Details_Adapter(getActivity(), R.layout.single_item_delivery,list_to_load);
        list.setAdapter(adapter);
    }
}