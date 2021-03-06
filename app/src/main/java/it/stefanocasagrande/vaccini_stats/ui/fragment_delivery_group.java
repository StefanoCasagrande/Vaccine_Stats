package it.stefanocasagrande.vaccini_stats.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.Adapters.Delivery_Adapter;
import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.MainActivity;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;
import it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary.vaccini_summary_data;

import static it.stefanocasagrande.vaccini_stats.Common.Common.hideKeyboard;

public class fragment_delivery_group extends Fragment {

    ListView list;
    EditText textFilter;
    private Delivery_Adapter adapter;
    private List<consegne_vaccini_data> full_list;

    public fragment_delivery_group() {
        // Required empty public constructor
    }

    public static fragment_delivery_group newInstance() {
        fragment_delivery_group fragment = new fragment_delivery_group();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_delivery_group, container, false);

        TextView tv_location = v.findViewById(R.id.tv_location);

        TextView tv_doses_administered = v.findViewById(R.id.tv_doses_administered);
        TextView tv_total_delivered = v.findViewById(R.id.tv_total_delivered);
        TextView tv_total = v.findViewById(R.id.tv_total);

        tv_total.setText(String.format("%s: %s", getString(R.string.Last_Update), Common.get_dd_MM_yyyy(Common.Database.Get_Configurazione("ultimo_aggiornamento"))));

        list = v.findViewById(R.id.listView);
        list.setEmptyView(v.findViewById(R.id.empty));
        textFilter = v.findViewById(R.id.SearchEditText);

        textFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String filter = s.toString();
                Load_Data(filter);
            }
        });

        tv_location.setText(getString(R.string.Italian_Situation));

        int doses_type1=0;
        int doses_type2=0;
        int doses_type3=0;
        int doses_type4=0;

        for (consegne_vaccini_data var : Common.Database.Get_Deliveries(""))
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

        tv_total_delivered.setText(Common.AddDotToInteger(doses_type1+doses_type2+doses_type3+doses_type4));

        List<vaccini_summary_data> list_administered = Common.Database.Get_vaccini_summary("");
        int administered=0;

        for (vaccini_summary_data var : list_administered)
        {
            administered+=var.dosi_somministrate;
        }

        tv_doses_administered.setText(Common.AddDotToInteger(administered));

        full_list = Common.Database.Get_Deliveries_GroupBy_Area();

        Load_Data("");

        list.requestFocus();
        hideKeyboard(getActivity());

        return v;
    }

    public void Load_Data(String filter)
    {
        List<consegne_vaccini_data> list_to_load = new ArrayList<>();

        if (filter==null || filter.equals(""))
            list_to_load = full_list;
        else
        {
            for(consegne_vaccini_data var : full_list)
            {
                if (!filter.equals(""))
                {
                    if (var.nome_area.toLowerCase().contains(filter.toLowerCase()))
                        list_to_load.add(var);
                }
            }
        }

        adapter = new Delivery_Adapter(getActivity(), R.layout.single_item_delivery,list_to_load);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id)->{
            Common.Back_Action = Common.Back_To_Delivery_Group;
            ((MainActivity)getActivity()).goToDelivery_Details(adapter.getItemList(position).nome_area);
        });
    }
}
