package com.example.luis.smartcharging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class ChargingListAdapter extends ArrayAdapter<DadosCharging> {


    public ChargingListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DadosCharging> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.charging_item, parent, false);
        }

        TextView startTime= (TextView) convertView.findViewById(R.id.startTime);
        TextView carId= (TextView) convertView.findViewById(R.id.carId);
        TextView plugId= (TextView) convertView.findViewById(R.id.plugId);
        TextView tempoEstimado= (TextView) convertView.findViewById(R.id.tempoEstimado);
        TextView bateriaEstimada= (TextView) convertView.findViewById(R.id.bateriaEstimada);
        SeekBar seekBar=(SeekBar) convertView.findViewById(R.id.seek_bar);
        bloqueaSeekBar(seekBar);
        TextView percentagem=(TextView) convertView.findViewById(R.id.percentagem);

        DadosCharging dados=getItem(position);

        startTime.setText("Time Started"+"\n"+dados.getStartTime());
        carId.setText("Car nº"+"\n"+dados.getCarId());
        plugId.setText("Plug nº"+"\n"+dados.getPlugId());
        tempoEstimado.setText("Time to full charge (estimated): "+dados.getTempoEstimado());
        bateriaEstimada.setText("Current batery level (estimated)");
        seekBar.setProgress(dados.getBateriaEstimada());
        percentagem.setText(dados.getBateriaEstimada()+"0%");

        return convertView;
    }

    public void bloqueaSeekBar(SeekBar seekBar)
    {
        seekBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
}
