package com.example.luis.smartcharging;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;

public class CarsCharging extends MyTukxis {

    private Toolbar toolbar;
    private String [] listaTucsCarregamento= new String[16];
    private ArrayAdapter <String>listaAdapter;
    private ListView listaTucsAcarregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_charging);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.menuicon));

        navigationClick(toolbar);
        getSupportActionBar().setTitle("CarsCharging");
        listaTucsAcarregar=findViewById(R.id.listaTucsAcarregar);
        preencheListaCarregamento();
    }

    public void preencheListaCarregamento()
    {
        listaTucsCarregamento[0]="Feedback";
        listaTucsCarregamento[1]="CarsCharging"+"\n"+"Started at 16:40      Car 4     Plug 3";
        listaTucsCarregamento[2]="Time to full charge (estimated)      4h";
        listaTucsCarregamento[3]="Curren battery status (estimated)        10%";

        listaTucsCarregamento[4]="";
        listaTucsCarregamento[5]="CarsCharging"+"\n"+"Started at 10:40      Car 5     Plug 4";
        listaTucsCarregamento[6]="Time to full charge (estimated)      1h";
        listaTucsCarregamento[7]="Curren battery status (estimated)        90%";

        listaTucsCarregamento[8]="";
        listaTucsCarregamento[9]="CarsCharging"+"\n"+"Started at 10:40      Car 1     Plug 2";
        listaTucsCarregamento[10]="Time to full charge (estimated)      1h";
        listaTucsCarregamento[11]="Curren battery status (estimated)        90%";

        listaTucsCarregamento[12]="";
        listaTucsCarregamento[13]="CarsCharging"+"\n"+"Started at 10:40      Car 2     Plug 1";
        listaTucsCarregamento[14]="Time to full charge (estimated)      1h";
        listaTucsCarregamento[15]="Curren battery status (estimated)        90%";

        listaAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listaTucsCarregamento);
        listaTucsAcarregar.setAdapter(listaAdapter);
    }


    public void terminarCarregar(View v)
    {
        Intent intentCarregar=new Intent(getApplicationContext(),BeingCharging.class);
        intentCarregar.putExtra("opcaoCarregamento",2); //Para indicar que é para terminar carregamento
        startActivity(intentCarregar);
    }
}
