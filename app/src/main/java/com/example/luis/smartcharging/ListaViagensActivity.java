package com.example.luis.smartcharging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import static com.example.luis.smartcharging.DBManager.*;


public class ListaViagensActivity extends MyTukxis {
    Toolbar toolbar;
    private static String distanciaViagem;
    private ArrayList<Percurso> listaViagem = new ArrayList<>();
    private AdpaterViagens adpaterViagens;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_viagens);
        setUpToolBar("Tours menu");

        RecyclerView recyclerView =  (RecyclerView)findViewById(R.id.recylerView);
        recyclerView.setHasFixedSize(true);//Every item of listRace has a fixed size
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Percurso percurso = filtroPercurso(adpaterViagens.getIdPercursoCheck(),listaViagem);
                Intent newItent = new Intent(getApplicationContext(),MapsActivity.class);
                newItent.putExtra("percurso",percurso);
                startActivity(newItent);
            }
        });


        listaViagem.add( new Percurso(1,"Funcal","Monte",30f));
        listaViagem.add( new Percurso(2,"Funcal","Garajau",30f));
        listaViagem.add(new Percurso(3,"Funcal","Câmara de Lobos",30f));
        listaViagem.add( new Percurso(4,"Funcal","Câmara de Lobos",30f));
        listaViagem.add( new Percurso(5,"Funcal","Câmara de Lobos",30f));

        adpaterViagens = new AdpaterViagens(listaViagem,getApplicationContext());

        recyclerView.setAdapter(adpaterViagens);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent newItent = new Intent(this,MapsActivity.class);
        startActivity(newItent);
    }
    public Percurso filtroPercurso(int id,ArrayList<Percurso> listaViagem){
        Percurso res = null;
        for (Percurso percurso: listaViagem){
            if(percurso.getId()==id){
             res = percurso;
             return res;
            }
        }
        return res;
    }
    public static String getDistanciaViagem() {
        return distanciaViagem;
    }

    public static void setDistanciaViagem(String distanciaViagem) {
        ListaViagensActivity.distanciaViagem = distanciaViagem;
    }
}
