package it.stefanocasagrande.vaccini_stats.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.stefanocasagrande.vaccini_stats.R;

public class fragment_delivery_details extends Fragment {


    public fragment_delivery_details() {
        // Required empty public constructor
    }

    public static fragment_delivery_details newInstance(String p_area_name) {
        fragment_delivery_details fragment = new fragment_delivery_details();
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
        View  v = inflater.inflate(R.layout.fragment_delivery_details, container, false);

        return v;
    }
}