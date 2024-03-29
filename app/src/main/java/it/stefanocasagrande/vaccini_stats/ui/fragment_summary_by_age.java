package it.stefanocasagrande.vaccini_stats.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.GlobalVariables;
import it.stefanocasagrande.vaccini_stats.Interface;
import it.stefanocasagrande.vaccini_stats.MainActivity;
import it.stefanocasagrande.vaccini_stats.R;

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

        if (!Common.Data_Already_Loaded) {

            if (GlobalVariables.isNetworkConnected)
                ((MainActivity) getActivity()).getLastUpdate(this);
            else {

                if (!Common.Database.Get_Configurazione("ultimo_aggiornamento").equals(""))
                    Load_Data();
                else
                    Toast.makeText(getContext(), getString(R.string.Internet_Missing), Toast.LENGTH_LONG).show();
            }

            if (!Common.Database.Get_Configurazione("HIDE_INSTRUCTION").equals("1"))
                ((MainActivity) requireActivity()).Show_Help();
            else if (!Common.Database.Get_Configurazione("HIDE_NEWS_2021_06").equals("1"))
                ((MainActivity) requireActivity()).Show_News();
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getContext(), getChildFragmentManager());
        ViewPager viewPager = v.findViewById(R.id.view_pager);
        viewPager.setHorizontalScrollBarEnabled(true);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = v.findViewById(R.id.tabs);
        tabs.setHorizontalScrollBarEnabled(true);
        tabs.setupWithViewPager(viewPager);


        Common.Back_Action = Common.Back_To_Nowhere;

        return v;
    }

    public void Load_Data()
    {
        Common.Data_Already_Loaded = true;

        if (getFragmentManager() != null) {

            getFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    @Override
    public void newDataAvailable() {
        Load_Data();
    }
}