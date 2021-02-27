package it.stefanocasagrande.vaccini_stats.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import it.stefanocasagrande.vaccini_stats.Common.Common;
import it.stefanocasagrande.vaccini_stats.R;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;

import static it.stefanocasagrande.vaccini_stats.Common.Common.AddDotToInteger;

public class Consegne_Adapter extends ArrayAdapter<consegne_vaccini_data> {

    private List<consegne_vaccini_data> list;
    private Context context;

    public Consegne_Adapter(Context v_context, int resource, List<consegne_vaccini_data> objects) {
        super(v_context, resource, objects);

        context = v_context;
        list = objects;
    }

    private static class ViewHolder {

        TextView mainText;
        TextView secondText;
        TextView tv_dosi;
    }

    public consegne_vaccini_data getItemList(int position) {
        return list.get(position);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder mViewHolder;

        if (convertView == null) {

            mViewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (inflater!=null) {
                convertView = inflater.inflate(R.layout.single_item_consegne, parent, false);
                convertView.setTag(mViewHolder);
                mViewHolder.mainText = convertView.findViewById(R.id.tv_main_text);
                mViewHolder.secondText = convertView.findViewById(R.id.tv_subtext);
                mViewHolder.tv_dosi = convertView.findViewById(R.id.tv_dosi);
            }
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.mainText.setText(list.get(position).nome_area);
        mViewHolder.secondText.setText(String.format("%s: %s", context.getString(R.string.Last_delivery), Common.get_dd_MM_yyyy(list.get(position).data_consegna)));
        mViewHolder.tv_dosi.setText(AddDotToInteger(list.get(position).numero_dosi));

        return convertView;
    }

}
