package com.example.luis.smartcharging;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class CarsCharging extends MyTukxis {

    private Toolbar toolbar;
    private ListView listaTucsAcarregar;
    private ChargingListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_charging);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBaR

        navigationClick(toolbar);
        getSupportActionBar().setTitle("CarsCharging");
        listaTucsAcarregar=findViewById(R.id.listaTucsAcarregar);
        preencheListaCarregamento();
    }

    public void preencheListaCarregamento()
    {
       if(DBManager.tucsEmCarregamento()!=null) {
           listAdapter = new ChargingListAdapter(this, R.layout.charging_item, DBManager.tucsEmCarregamento());
           listaTucsAcarregar.setAdapter(listAdapter);
       }
    }

    public void terminarCarregar(View v)
    {
        Intent intentCarregar=new Intent(getApplicationContext(),BeingCharging.class);
        intentCarregar.putExtra("opcaoCarregamento",2); //Para indicar que é para terminar carregamento
        startActivity(intentCarregar);
    }
}
