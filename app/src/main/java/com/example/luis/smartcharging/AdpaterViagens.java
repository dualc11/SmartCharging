package com.example.luis.smartcharging;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;
import java.util.zip.CheckedOutputStream;

/**
 * Created by claudio on 10-07-2018.
 */

public class AdpaterViagens extends RecyclerView.Adapter<AdpaterViagens.ViewHolderViagens> implements CompoundButton.OnCheckedChangeListener{
    private List<Percurso> viagemLista;
    private Context context;
    private static String distanciaViagem;
    private int idPercursoCheck =  -1;
    private static int id = 0;
    public AdpaterViagens(List<Percurso> viagemLista, Context context){
        this.viagemLista = viagemLista;
        this.context = context;

    }
    public int getIdPercursoCheck(){
        return idPercursoCheck;
    }
    @Override
    public ViewHolderViagens onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.tours_info,parent,false);
        return new ViewHolderViagens(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderViagens holder, int position) {
        Percurso percurso = viagemLista.get(position);
        holder.nomeViagem.setText(percurso.getOrigem()+" - "+ percurso.getDestino());
        holder.distanciaViagem.setText(percurso.getDistancia()+" km");
        holder.checkBox.setOnCheckedChangeListener(this);
        holder.checkBox.setId(percurso.getId());

    }

    @Override
    public int getItemCount() {
        return viagemLista.size();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(idPercursoCheck == -1){
           idPercursoCheck = compoundButton.getId();
        }else{
            if(idPercursoCheck == compoundButton.getId()){
                idPercursoCheck = -1;
            }else{
                compoundButton.setChecked(false);
            }
        }

        Log.e("merda","O estado é "+b+" \n O id é "+compoundButton.getId());
    }


    public class ViewHolderViagens extends RecyclerView.ViewHolder{
        private TextView nomeViagem;
        private TextView distanciaViagem;
        private CheckBox checkBox;

        public ViewHolderViagens(View itemView) {
            super(itemView);
            nomeViagem = (TextView)itemView.findViewById(R.id.nomeViagem);
            distanciaViagem = (TextView)itemView.findViewById(R.id.distanciaViagem);
            checkBox = (CheckBox)itemView.findViewById(R.id.checkbox);

        }
    }
}
