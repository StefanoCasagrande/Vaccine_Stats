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

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.Adapters.Consegne_Adapter;
import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;

import static it.stefanocasagrande.vaccini_stats.Common.Common.hideKeyboard;

public class fragment_group_consegne extends Fragment {

    ListView list;
    EditText textFilter;
    private Consegne_Adapter adapter;
    private List<consegne_vaccini_data> full_list;

    public fragment_group_consegne() {
        // Required empty public constructor
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
        View v = inflater.inflate(R.layout.fragment_group_consegne, container, false);

        list = v.findViewById(R.id.listView);
        list.setEmptyView(v.findViewById(R.id.empty));
        textFilter = v.findViewById(R.id.cercaEditText);

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

        full_list = Common.Database.Consegne_GroupBy_Area();

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

        adapter = new Consegne_Adapter(getActivity(), R.layout.single_item_consegne,list_to_load);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id)-> {

        });
    }
}
