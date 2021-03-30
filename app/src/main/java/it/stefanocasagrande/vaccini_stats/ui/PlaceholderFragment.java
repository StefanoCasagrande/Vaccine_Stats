package it.stefanocasagrande.vaccini_stats.ui;

import android.app.AppOpsManager;
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

        final TextView tv_operatori_sanitari = root.findViewById(R.id.tv_operatori_sanitari);
        final TextView tv_ospiti_rsa = root.findViewById(R.id.tv_ospiti_rsa);
        final TextView tv_others = root.findViewById(R.id.tv_others);
        final TextView tv_law_enforcement = root.findViewById(R.id.tv_law_enforcement);
        final TextView tv_non_healtcare = root.findViewById(R.id.tv_non_healtcare);
        final TextView tv_school_staff = root.findViewById(R.id.tv_school_staff);

        final TextView tv_age_click_for_graph = root.findViewById(R.id.tv_age_click_for_graph);

        final LinearLayout ll_administered_doses_graph = root.findViewById(R.id.ll_administered_doses_graph);
        ll_administered_doses_graph.setOnClickListener(v -> {
            if (!tv_operatori_sanitari.getText().toString().toUpperCase().equals(getString(R.string.Number_Placeholder).toUpperCase()))
            {
                ((MainActivity)getActivity()).Show_Graph(
                        Integer.parseInt(tv_operatori_sanitari.getText().toString().replace(".","")),
                        Integer.parseInt(tv_non_healtcare.getText().toString().replace(".","")),
                        Integer.parseInt(tv_ospiti_rsa.getText().toString().replace(".","")),
                        Integer.parseInt(tv_law_enforcement.getText().toString().replace(".","")),
                        Integer.parseInt(tv_school_staff.getText().toString().replace(".","")),
                        Integer.parseInt(tv_others.getText().toString().replace(".",""))
                );
            }
            else
                Toast.makeText(getContext(), getString(R.string.Internet_Missing), Toast.LENGTH_SHORT).show();
        });

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
            int popolazione=0;

            if (s.equals("Totale"))
            {
                popolazione = Common.Database.Get_Popolazione("","");

                tv_population.setText(Common.AddDotToInteger(popolazione));
                tv_age.setText(getString(R.string.General_Data));
                int male=0;
                int female=0;
                int categoria_operatori_sanitari_sociosanitari=0;
                int categoria_ospiti_rsa=0;
                int categoria_altro=0;
                int categoria_forze_armate=0;
                int categoria_personale_scolastico=0;
                int categoria_personale_non_sanitario=0;
                int first_dose=0;
                int second_dose=0;

                tv_age_click_for_graph.setVisibility(View.VISIBLE);


                if (lista.size()>0)
                    tv_last_update.setText(String.format("%s: %s", getString(R.string.Data), Common.get_dd_MM_yyyy(lista.get(0).ultimo_aggiornamento)));

                for(anagrafica_vaccini_summary_data var : lista) {
                    male+=var.sesso_maschile;
                    female+=var.sesso_femminile;
                    categoria_operatori_sanitari_sociosanitari+=var.categoria_operatori_sanitari_sociosanitari;
                    categoria_ospiti_rsa+=var.categoria_ospiti_rsa;
                    categoria_altro+=var.categoria_over80+var.categoria_over75+var.categoria_over70+var.categoria_altro;
                    categoria_forze_armate+=var.categoria_forze_armate;
                    categoria_personale_scolastico+=var.categoria_personale_scolastico;
                    categoria_personale_non_sanitario+=var.categoria_personale_non_sanitario;
                    first_dose+=var.prima_dose;
                    second_dose+=var.seconda_dose;
                }

                tv_first_dose.setText(Common.AddDotToInteger(female+male-second_dose));
                tv_second_dose.setText(Common.AddDotToInteger(second_dose));

                tv_operatori_sanitari.setText(Common.AddDotToInteger(categoria_operatori_sanitari_sociosanitari));
                tv_ospiti_rsa.setText(Common.AddDotToInteger(categoria_ospiti_rsa));
                tv_others.setText(Common.AddDotToInteger(categoria_altro));
                tv_law_enforcement.setText(Common.AddDotToInteger(categoria_forze_armate));
                tv_school_staff.setText(Common.AddDotToInteger(categoria_personale_scolastico));
                tv_non_healtcare.setText(Common.AddDotToInteger(categoria_personale_non_sanitario));

                if (popolazione>0)
                {
                    double percent = (((female+male)*100)/ (double)popolazione);
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

                        tv_operatori_sanitari.setText(Common.AddDotToInteger(var.categoria_operatori_sanitari_sociosanitari));
                        tv_ospiti_rsa.setText(Common.AddDotToInteger(var.categoria_ospiti_rsa));
                        tv_others.setText(Common.AddDotToInteger(var.categoria_over80+var.categoria_over75+var.categoria_over70+var.categoria_altro));
                        tv_law_enforcement.setText(Common.AddDotToInteger(var.categoria_forze_armate));
                        tv_school_staff.setText(Common.AddDotToInteger(var.categoria_personale_scolastico));
                        tv_non_healtcare.setText(Common.AddDotToInteger(var.categoria_personale_non_sanitario));

                        if (popolazione>0)
                        {
                            double percent = (((var.sesso_femminile+var.sesso_maschile)*100)/(double) popolazione);
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
