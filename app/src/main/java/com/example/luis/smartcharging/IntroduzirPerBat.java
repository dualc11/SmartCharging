package com.example.luis.smartcharging;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

public class IntroduzirPerBat extends MyTukxis {

    private SeekBar seekBar;
    private TextView percentagem;
    private static int percentagemBat;
    private static Intent intentPrincipal;
    private static boolean foiParaWhatsapp;
    private Toolbar toolbar;
    private static final int OPCAO_VIAGEM_DECORRER = 1;


    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek_bar);
        view = LayoutInflater.from(IntroduzirPerBat.this).inflate(R.layout.seekbar,null);
        seekBarTouch();
        intentPrincipal=new Intent(IntroduzirPerBat.this,MapsActivity.class);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("CarsCharging");
        navigationClick(toolbar);
        percentagemBateria();
    }

    /*Método para controlar a percentagem de bateria que o user põe na IntroduzirPerBat*/
    public void seekBarTouch()
    {
        seekBar=view.findViewById(R.id.seek_bar1);
        percentagem=view.findViewById(R.id.percentagem);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b)
                    {
                        percentagemBat=i;
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
    public void iniciarViagem()
    {
        if(percentagemBat!=0)
        {
            if (!GpsService.getServicoIniciado())
            {
                startService(MyTukxis.getIntentGps());

                //int idCarro=getIntent().getIntExtra("idCarro",0);
                int idCarro=MyTukxis.getIdCarro();
                enviaInfoWhatsapp(idCarro,"está a ser usado pelo");
                foiParaWhatsapp=true;
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
                stopService(MyTukxis.getIntentGps());
                //int idCarro=getIntent().getIntExtra("idCarro",0);
                int idCarro=MyTukxis.getIdCarro();
                enviaInfoWhatsapp(idCarro,"deixou de ser usado pelo");
                foiParaWhatsapp=true;
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

    public void confirmar() throws JSONException
    {
        //Caso seja para iniciar viagem
        if(getIntent().getIntExtra("opcao",0)==0)
        {
            iniciarViagem();
        }
        //Caso seja para terminar viagem
        else if(getIntent().getIntExtra("opcao",0)==OPCAO_VIAGEM_DECORRER)
        {
            terminarViagem();
        }
        //Caso seja para inicar carregamento
        else if(getIntent().getIntExtra("opcao",0)==2)
        {
            if(getIntent().getIntExtra("viagem",0)==1)
            {
                terminarViagem();
            }
            MyTukxis.getDb().colocaTucCarregar(percentagemBat, /*BeingCharging.getTucId()*/MyTukxis.getIdCarro(),
                    BeingCharging.getTomadaId(), MyTukxis.getUserId());
            Toast.makeText(this,"BeingCharging iniciado",Toast.LENGTH_LONG).show();

            if(getIntent().getIntExtra("viagem",0)!=1)
            {
                Intent intentPrincipal = new Intent(IntroduzirPerBat.this, MyTukxis.class);
                startActivity(intentPrincipal);
                //this.finish();
            }
        }
        //Caso seja para terminar carregamento
        else if(getIntent().getIntExtra("opcao",0)==3)
        {
            if(getIntent().getIntExtra("viagem",0)==2)
            {
                iniciarViagem();
            }
            boolean estado= MyTukxis.getDb().atualizaInfoCarregamento(percentagemBat, /*BeingCharging.getTucId()*/MyTukxis.getIdCarro(),
                    BeingCharging.getTomadaId());
            if(estado)
            {
                Toast.makeText(this,"BeingCharging terminado",Toast.LENGTH_LONG).show();

            }
            else {
                Toast.makeText(this, "Não existia nenhum carregamento correspondente!", Toast.LENGTH_LONG).show();
            }
            if(getIntent().getIntExtra("viagem",0)!=2)
            {
                Intent intentPrincipal = new Intent(IntroduzirPerBat.this, MyTukxis.class);
                startActivity(intentPrincipal);
                //this.finish();
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

    public void enviaInfoWhatsapp(int idCarro,String mensagem)
    {
        Intent sendIntent = new Intent();
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "O tuc-tuc número "+idCarro+" "+mensagem +
                " utilizador "+ MyTukxis.getUserId());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "ola"));
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
