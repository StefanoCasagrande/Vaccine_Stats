package it.stefanocasagrande.vaccini_stats.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.List;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.GlobalVariables;
import it.stefanocasagrande.vaccini_stats.Interface;
import it.stefanocasagrande.vaccini_stats.MainActivity;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_data;

public class fragment_summary_by_age extends Fragment implements Interface  {

    public fragment_summary_by_age() {
        // Required empty public constructor
    }

    public static fragment_summary_by_age newInstance() {
        fragment_summary_by_age fragment = new fragment_summary_by_age();
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
        View v = inflater.inflate(R.layout.fragment_summary_by_age, container, false);

        if (GlobalVariables.isNetworkConnected)
            ((MainActivity) getActivity()).getLastUpdate(this);
        else
        {
            if (!Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(""))
            {
                Load_Data();
            }
            else
                Toast.makeText(getContext(),getString(R.string.Internet_Missing), Toast.LENGTH_LONG).show();
        }

        return v;
    }

    public void Load_Data()
    {
        List<anagrafica_vaccini_summary_data> lista = Common.Database.Get_anagrafica_vaccini_summary();
    }

    @Override
    public void newDataAvailable() {
        Load_Data();
    }
}
