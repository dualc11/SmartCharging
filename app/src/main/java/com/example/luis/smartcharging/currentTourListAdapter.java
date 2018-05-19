package com.example.luis.smartcharging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.luis.smartcharging.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class currentTourListAdapter extends ArrayAdapter<String> {

    public currentTourListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.current_tour, parent, false);
        }

        TextView info = (TextView) convertView.findViewById(R.id.title);
        TextView kms=(TextView) convertView.findViewById(R.id.kmsTour);
        TextView descricao=(TextView) convertView.findViewById(R.id.descricao);

        String informacao=getItem(position);
        String[] infoEkms=informacao.split(",");

        info.setText(infoEkms[0]);
        descricao.setText(infoEkms[1]);
        kms.setText(infoEkms[2]);

        return convertView;
    }
}
