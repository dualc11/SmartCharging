package com.example.luis.smartcharging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

public class seekBar extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView percentagem;
    private static int percentagemBat;
    private static Intent intentPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek_bar);
        seekBarTouch();
        intentPrincipal=new Intent(seekBar.this,StartAndStopService.class);
    }

    /*Método para controlar a percentagem de bateria que o user põe na seekBar*/
    public void seekBarTouch()
    {
        seekBar=findViewById(R.id.seek_bar);
        percentagem=findViewById(R.id.percentagem);

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b)
                    {
                        percentagemBat=i;
                        percentagem.setText("Percentagem: "+percentagemBat);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)
                    {
                        percentagem.setText("Percentagem: "+percentagemBat);
                    }
                }
        );
    }

    /*Método para iniciar o serviço de começar a registar as coordenadas da viagem*/
    public void iniciarViagem()
    {
        if(percentagemBat!=0)
        {
            if (!GpsService.getServicoIniciado()) {
                startService(StartAndStopService.getIntentGps());
                startActivity(intentPrincipal);
            }
            else
            {
                Toast.makeText(this,"Está uma viagem em curso!",Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this,"Por favor insira uma percentagem de bateria válida!",Toast.LENGTH_LONG).show();
        }
    }

    /*Método para parar finalizar a viagem*/
    public void terminarViagem() throws JSONException {
        if(percentagemBat!=0)
        {
            if (GpsService.getServicoIniciado())
            {
                stopService(StartAndStopService.getIntentGps());

                //So para debug
                double kmViagem=0;
                int idViagem=GpsService.getIdViagem();
                kmViagem = DBManager.calculaKmViagem(idViagem);

                //Começamos a nova actividade que irá mostrar os kms, bateria, carregar etc
                Intent intentPrincipal = new Intent(seekBar.this, ShowLocationActivity.class);
                intentPrincipal.putExtra("distanciaKm", kmViagem);
                intentPrincipal.putExtra("idViagem", idViagem);
                startActivity(intentPrincipal);
            }
            else
            {
                Toast.makeText(this,"Não está nenhuma viagem em curso!",Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this,"Por favor insira uma percentagem de bateria válida!",Toast.LENGTH_LONG).show();
        }
    }

    public void confirmar(View v) throws JSONException {
        if(getIntent().getIntExtra("opcao",0)==0)
        {
            iniciarViagem();
        }
        else if(getIntent().getIntExtra("opcao",0)==1)
        {
            terminarViagem();
        }
    }

    public static int getPercentagemBat(){return percentagemBat;}
}
