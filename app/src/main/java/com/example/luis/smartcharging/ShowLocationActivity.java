package com.example.luis.smartcharging;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ShowLocationActivity extends AppCompatActivity {

    private int idViagem=0;
    private double distanciaKm=0;
    private TextView autonomiaText,idViagemText,percentagemBatText,distanciaKmText;
    private Button bCarregarBat;
    DecimalFormat numberFormat=new DecimalFormat("#.0");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);

        distanciaKmText=findViewById(R.id.distanciaText);
        idViagemText=findViewById(R.id.idViagem);
        autonomiaText=findViewById(R.id.autonomiaText);
        percentagemBatText=findViewById(R.id.percentagemBat);
        bCarregarBat=findViewById(R.id.bIniciarCarregamento);

        autonomiaText.setText("Autonomia restante: X%");
        percentagemBatText.setText("Percentagem de bateria atual: X%");

        //Buscamos os valores que vem no intent
        idViagem=getIntent().getIntExtra("idViagem",0);
        idViagemText.setText("Id da última viagem: "+Integer.toString(idViagem));

        distanciaKm = getIntent().getDoubleExtra("distanciaKm", 0);
        distanciaKmText.setText("Distância percorrida na última viagem: " + distanciaKm + " Km");
    }
}
