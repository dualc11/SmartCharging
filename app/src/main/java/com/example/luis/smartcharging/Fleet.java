package com.example.luis.smartcharging;

import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Random;

public class Fleet extends MyTukxis {
    private static ListView listaTucsDisp;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);
        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        //toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.menuicon));
        getSupportActionBar().setTitle("Fleet");

        navigationClick(toolbar);
        listaTucsDisp=findViewById(R.id.listaTucsDisp);
        geraTucsDisp();

    }

    public void geraTucsDisp()
    {
        Random rand= new Random();
        int idTucDisp;
        ArrayAdapter <String> adapter;
        boolean jaExiste;
        int nrTucsDisp=rand.nextInt(7);
        String [] tucsDisp= new String[nrTucsDisp];
        int i=0;
        while(i<tucsDisp.length)
        {
            idTucDisp = rand.nextInt(6) + 1;//Gera nr de 1 a 6
            jaExiste=false;
            for(int indice=0;indice<tucsDisp.length;indice++)
            {
                if(tucsDisp[indice]!=null) {
                    if (tucsDisp[indice].equals("Tuc Id: " + idTucDisp)) {
                        jaExiste = true;
                    }
                }
            }
            if(!jaExiste)
            {
                tucsDisp[i] = "Car nº " + idTucDisp+"\n"+"(Available/Available(CarsCharging)/Not available)";
                i++;
            }
        }
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tucsDisp);
        listaTucsDisp.setAdapter(adapter);
    }
}
