package com.example.luis.smartcharging;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;

public class Fleet extends MyTukxis {
    private static ListView listaFleet;
    private Toolbar toolbar;
    private ArrayList<DadosFleet> fleets=new ArrayList<>();
    private FleetListAdapter fleetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);
        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("Fleet");

        navigationClick(toolbar);
        listaFleet=findViewById(R.id.listaTucsDisp);
        preencheFleet();

    }

    public void preencheFleet()
    {
        DadosFleet dados=new DadosFleet("1","Available (charging)","1 bar at 13:30");
        fleets.add(dados);
        dados=new DadosFleet("2","Not available","");
        fleets.add(dados);
        dados=new DadosFleet("3","Available (charging)","2 bars at 16:30");
        fleets.add(dados);
        dados=new DadosFleet("4","Not available","");
        fleets.add(dados);

        fleetAdapter=new FleetListAdapter(this,R.layout.dados_fleet,fleets);
        listaFleet.setAdapter(fleetAdapter);
    }
}
