package com.example.luis.smartcharging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.luis.smartcharging.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class currentTourListAdapter extends ArrayAdapter<String> implements View.OnClickListener{

    public currentTourListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getItemViewType(int position) {
        int res = 0;
        if(position==0){
            return 1;
        }
        return res;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.destino_descricao, parent, false);
        }
            Log.e("merda","merda");
            if(getItemViewType(position)==1){
                TextView destino = (TextView) convertView.findViewById(R.id.destino);
                TextView viagem = (TextView) convertView.findViewById(R.id.viagem);
                TextView distancia = (TextView) convertView.findViewById(R.id.distancia);
                ImageButton btn_seguinte = (ImageButton) convertView.findViewById(R.id.btn_seguinte);

                String informacao = getItem(position);
                String[] info=informacao.split(",");
                destino.setText(info[0]);
                viagem.setText(info[1]);
                distancia.setText(info[2]);
                btn_seguinte.setOnClickListener(this);

            }else{
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.current_tour, parent, false);
                TextView info = (TextView) convertView.findViewById(R.id.title);
                TextView kms=(TextView) convertView.findViewById(R.id.kmsTour);
                TextView descricao=(TextView) convertView.findViewById(R.id.descricao);

                String informacao = getItem(position);
                String[] infoEkms=informacao.split(",");

                info.setText(infoEkms[0]);
                descricao.setText(infoEkms[1]);
                kms.setText(infoEkms[2]);
            }



        return convertView;
    }

    @Override
    public void onClick(View view) {
        Intent newItent = new Intent(view.getContext(),ListaViagensActivity.class);
        view.getContext().startActivity(newItent);
    }
}
