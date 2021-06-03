package it.stefanocasagrande.vaccini_stats.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.MainActivity;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_data;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tab, container, false);

        Common.Back_Action = Common.Back_To_Nowhere;

        final TextView tv_age = root.findViewById(R.id.tv_age);
        final TextView tv_last_update = root.findViewById(R.id.tv_last_update);
        final TextView tv_first_dose = root.findViewById(R.id.tv_first_dose);
        final TextView tv_population = root.findViewById(R.id.tv_population);
        final TextView tv_percent = root.findViewById(R.id.tv_percent);

        final TextView tv_second_dose = root.findViewById(R.id.tv_second_dose);

        final TextView tv_age_click_for_graph = root.findViewById(R.id.tv_age_click_for_graph);

        final LinearLayout ll_age = root.findViewById(R.id.ll_age);
        ll_age.setOnClickListener(v -> {
            if (tv_age.getText().toString().toUpperCase().equals(getString(R.string.General_Data).toUpperCase()))
            {
                ((MainActivity)getActivity()).Show_Age_Graph();
            }
        });

        List<anagrafica_vaccini_summary_data> lista = Common.Database.Get_anagrafica_vaccini_summary();
        pageViewModel.getText().observe(getActivity(), s -> {

            final ProgressDialog waiting_bar = ((MainActivity)getActivity()).getprogressDialog();
            waiting_bar.show();
            int popolazione;

            if (s.equals("Totale"))
            {
                popolazione = Common.Database.Get_Popolazione("","");

                tv_population.setText(Common.AddDotToInteger(popolazione));
                tv_age.setText(getString(R.string.General_Data));
                int male=0;
                int female=0;
                int first_dose=0;
                int second_dose=0;

                tv_age_click_for_graph.setVisibility(View.VISIBLE);


                if (lista.size()>0)
                    tv_last_update.setText(String.format("%s: %s", getString(R.string.Data), Common.get_dd_MM_yyyy(lista.get(0).ultimo_aggiornamento)));

                for(anagrafica_vaccini_summary_data var : lista) {
                    male+=var.sesso_maschile;
                    female+=var.sesso_femminile;
                    first_dose+=var.prima_dose;
                    second_dose+=var.seconda_dose;
                }

                tv_first_dose.setText(Common.AddDotToInteger(female+male-second_dose));
                tv_second_dose.setText(Common.AddDotToInteger(second_dose));

                if (popolazione>0)
                {
                    double percent = ((((double)(female+male-second_dose))/(double)popolazione)*100);
                    tv_percent.setText(String.format(getString(R.string.Percent_Population), String.format("%.2f", percent)));
                }
            }
            else
            {
                popolazione = Common.Database.Get_Popolazione(s,"");

                tv_population.setText(Common.AddDotToInteger(popolazione));
                tv_age.setText(String.format("%s: %s",getString(R.string.Age_Group), s));

                for(anagrafica_vaccini_summary_data var : lista)
                {
                    if (var.fascia_anagrafica.equals(s))
                    {
                        tv_last_update.setText(String.format("%s: %s", getString(R.string.Data), Common.get_dd_MM_yyyy(var.ultimo_aggiornamento)));
                        tv_first_dose.setText(Common.AddDotToInteger(var.sesso_femminile+var.sesso_maschile-var.seconda_dose));

                        tv_second_dose.setText(Common.AddDotToInteger(var.seconda_dose));

                        if (popolazione>0)
                        {
                            double percent =  ((((double)(var.sesso_femminile+var.sesso_maschile-var.seconda_dose))/(double)popolazione)*100);
                            tv_percent.setText(String.format(getString(R.string.Percent_Population), String.format("%.2f", percent)));
                        }
                    }
                }
            }

            waiting_bar.dismiss();
        });
        return root;
    }
}
