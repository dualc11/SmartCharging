package com.example.luis.smartcharging;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.Random;

public class VerTucsDisponiveis extends AppCompatActivity {
    private static ListView listaTucsDisp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_tucs_disponiveis);

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
                tucsDisp[i] = "Tuc Id: " + idTucDisp;
                i++;
            }
        }
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tucsDisp);
        listaTucsDisp.setAdapter(adapter);
    }
}
