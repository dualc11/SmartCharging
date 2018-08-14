package com.example.luis.smartcharging;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import static com.example.luis.smartcharging.BeingCharging.getTomadaId;
import static com.example.luis.smartcharging.DBManager.calculaKmDeslocacao;
import static com.example.luis.smartcharging.DBManager.calculaKmViagem;
import static com.example.luis.smartcharging.DBManager.getIdViagemAnterior;
import static com.example.luis.smartcharging.DBManager.getUltimoDeslocacaoId;
import static com.example.luis.smartcharging.DBManager.getUltimoUlizacaoId;
import static com.example.luis.smartcharging.DBManager.updateKmBateriaFinalDeslocacao;
import static com.example.luis.smartcharging.DBManager.updateKmBateriaFinalUtilizacao;
import static com.example.luis.smartcharging.DBManager.updateKmBateriaFinalViagem;
import static com.example.luis.smartcharging.GpsService.beginTour;
import static com.example.luis.smartcharging.GpsService.endTour;
import static com.example.luis.smartcharging.GpsService.getEmViagem;
import static com.example.luis.smartcharging.VolleyRequest.postViagem;
import static com.example.luis.smartcharging.VolleyRequest.sendChargeInfo;
import static com.example.luis.smartcharging.VolleyRequest.sendPickUp;

public class IntroduzirPerBat extends MyTukxis {

    private SeekBar seekBar;
    private TextView percentagem;
    private static int percentagemBat;
    private static Intent intentPrincipal;
    private static boolean foiParaWhatsapp;
    private Toolbar toolbar;
    private static final int OPCAO_VIAGEM_DECORRER = 1;
    private static boolean comecouViagem = false;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek_bar);
        view = LayoutInflater.from(IntroduzirPerBat.this).inflate(R.layout.seekbar,null);
        seekBarTouch();
        intentPrincipal = new Intent(IntroduzirPerBat.this,MapsActivity.class);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("CarsCharging");
        navigationClick(toolbar);
        percentagemBateria();
    }

    /*Método para controlar a percentagem de bateria que o user põe na IntroduzirPerBat*/
    public void seekBarTouch()
    {
        seekBar = view.findViewById(R.id.seek_bar1);
        percentagem = view.findViewById(R.id.percentagem);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b)
                    {
                        percentagemBat = i;
                        percentagem.setText(""+percentagemBat);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)
                    {
                        //percentagem.setText("Percentagem: "+percentagemBat);
                    }
                }
        );
    }

    /*Método para iniciar o serviço de começar a registar as coordenadas da viagem*/
    public void iniciarGps(){
        if(percentagemBat!=0)
        {
            if (!GpsService.getServicoIniciado())
            {
                startService(MyTukxis.getIntentGps());

                //int idCarro=getIntent().getIntExtra("idCarro",0);
                int idCarro=MyTukxis.getIdCarro();
               // enviaInfoWhatsapp(idCarro,"está a ser usado pelo");
                startActivity(intentPrincipal);
                            }
            else
            {
                Toast.makeText(this, getResources().getString(R.string.using_car),Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this,getResources().getString(R.string.please_insert_bat),Toast.LENGTH_LONG).show();
        }
    }
    /*Método para parar finalizar a viagem*/
    public void terminarUtilizacao() throws JSONException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(percentagemBat!=0)
                {
                    if (GpsService.getServicoIniciado())
                    {
                        stopService(MyTukxis.getIntentGps());
                        int idCarro=MyTukxis.getIdCarro();
                        //enviaInfoWhatsapp(idCarro,"deixou de ser usado pelo");
                        int viagemId = getIdViagemAnterior();
                        int deslocacaoId =  getUltimoDeslocacaoId();
                        if(getEmViagem()){
                            double kmViagem = 0;
                            try {
                                kmViagem = calculaKmViagem(viagemId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            updateKmBateriaFinalViagem(kmViagem,IntroduzirPerBat.getPercentagemBat(),viagemId);

                            //End tour (Miss emViagem = false)
                        }else{

                            double kmDeslocacao = 0;
                            try {
                                kmDeslocacao = calculaKmDeslocacao(deslocacaoId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            updateKmBateriaFinalDeslocacao(kmDeslocacao,IntroduzirPerBat.getPercentagemBat(),deslocacaoId);
                            //End deslocação(MIss emViagem = false)
                        }
                        double kmUtilizacao = -1f;
                        int ultimoUtilizacaoId = getUltimoUlizacaoId();
                        try {
                            //kmViagem = DBManager.calculaKmViagem(viagemId);
                            kmUtilizacao = DBManager.calculaKmUtilizacao(getUltimoUlizacaoId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        updateKmBateriaFinalUtilizacao(kmUtilizacao,IntroduzirPerBat.getPercentagemBat(),ultimoUtilizacaoId);
                        postViagem(viagemId,deslocacaoId,getPercentagemBat(),getTomadaId(),getIdCarro());
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.not_in_tour_),Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.bat_error),Toast.LENGTH_LONG).show();
                }
            }
        }).start();

    }

    public void confirmar() throws JSONException {
        if (getIntent().getIntExtra("beginTour", 0) == 1) {//Caso seja para cpmeçar uma viagem
            beginTour();
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("endTour",1);
            startActivity(intent);
            }else{
            if (getIntent().getIntExtra("endTour", 0) == 1) {//Caso seja para acabar uma viagem
                endTour();
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("startTour",1);
                startActivity(intent);
            } else {
                if (getIntent().getIntExtra("opcao", 0) == 0) {
                    sendPickUp(getIdCarro(), getPercentagemBat(), getTomadaId());
                    iniciarGps();
                    // iniciarViagem();
                }
                //Caso seja para terminar utilização
                else if (getIntent().getIntExtra("opcao", 0) == OPCAO_VIAGEM_DECORRER) {
                    terminarUtilizacao();
                }
                //Caso seja para inicar carregamento e terminar utilização
                else if (getIntent().getIntExtra("opcao", 0) == 2) {
                    if (getIntent().getIntExtra("viagem", 0) == 1) {
                        terminarUtilizacao();
                    }
                    MyTukxis.getDb().colocaTucCarregar(percentagemBat, MyTukxis.getIdCarro(),
                            getTomadaId(), String.valueOf(MyTukxis.getUserId()));
                    sendChargeInfo(VolleyRequest.getActionBeginCharge(), percentagemBat, MyTukxis.getIdCarro(), getTomadaId());
                    Toast.makeText(this, "BeingCharging iniciado", Toast.LENGTH_LONG).show();

                    if (getIntent().getIntExtra("viagem", 0) != 1) {
                        Intent intentPrincipal = new Intent(IntroduzirPerBat.this, MyTukxis.class);
                        startActivity(intentPrincipal);
                        //this.finish();
                    }
                }
                //Caso seja para terminar carregamento
                else if (getIntent().getIntExtra("opcao", 0) == 3) {
                    if (getIntent().getIntExtra("viagem", 0) == 2) {
                        sendPickUp(getIdCarro(), getPercentagemBat(), getTomadaId());
                        iniciarGps();
                        //iniciarViagem();
                    }
                    boolean estado = MyTukxis.getDb().atualizaInfoCarregamento(percentagemBat, MyTukxis.getIdCarro(),
                            getTomadaId());//Verifica se existe algum carregamento desse tipo

                    if (estado) {
                        sendChargeInfo(VolleyRequest.getActionStopCharge(), percentagemBat, getIdCarro(), getTomadaId());
                        Toast.makeText(this, "BeingCharging terminado", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(this, "Não existia nenhum carregamento correspondente!", Toast.LENGTH_LONG).show();
                    }
                    if (getIntent().getIntExtra("viagem", 0) != 2) {
                        Intent intentPrincipal = new Intent(IntroduzirPerBat.this, MyTukxis.class);
                        startActivity(intentPrincipal);
                        //this.finish();
                    }
                }
            }
        }
    }

    public void percentagemBateria()
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())
                    {
                        new AlertDialog.Builder(IntroduzirPerBat.this)
                                .setTitle("The app needs to know the current battery level")
                                .setMessage("Please, use the slider to select the current battery (in bars) of your Tukxi.")
                                .setView(view)
                                .setCancelable(false)
                                .setPositiveButton("Done", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        try {
                                            confirmar();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .show();
                    }
                }
        );
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(foiParaWhatsapp)
        {
            startActivity(intentPrincipal);
            foiParaWhatsapp=false;
        }
    }

    public static int getPercentagemBat(){return percentagemBat;}
}
