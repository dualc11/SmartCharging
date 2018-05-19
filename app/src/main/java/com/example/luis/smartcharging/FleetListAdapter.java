package com.example.luis.smartcharging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.luis.smartcharging.DadosFleet;
import com.example.luis.smartcharging.R;

import java.util.ArrayList;

public class FleetListAdapter extends ArrayAdapter<DadosFleet> {

    public FleetListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DadosFleet> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dados_fleet, parent, false);
        }

        TextView carId = (TextView) convertView.findViewById(R.id.carId);
        TextView estado=(TextView) convertView.findViewById(R.id.estado);
        TextView bars=(TextView) convertView.findViewById(R.id.bars);

        DadosFleet dados=getItem(position);

        carId.setText("Car nº "+dados.getIdTuc());
        estado.setText(dados.getEstado());
        bars.setText(dados.getBars());

        return convertView;
    }
}
