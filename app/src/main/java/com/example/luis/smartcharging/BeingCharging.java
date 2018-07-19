package com.example.luis.smartcharging;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class BeingCharging extends MyTukxis {

    private IntentIntegrator qrScan;
    private static int resultadoCodigo;
    private static String stringCodigo;
    private int codigo;
    private static int tucId,tomadaId;
    private Toolbar toolbar;
    private String title;
    private static final int disconect = 2;
    private static final int connect = 1;
    private static final int codigoTomda = 0;
    private static final int codigoTuc = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_being_charging);

        qrScan = new IntentIntegrator(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("Being charging");
        navigationClick(toolbar);
        int opcaoCarregamento = getIntent().getIntExtra("opcaoCarregamento",0);
        if(opcaoCarregamento == disconect)
        {
            title = "You disconect";
        }
        else if(opcaoCarregamento == connect)
        {
            title = "You connect";
        }

       info();
    }

    //Este método é chamado automaticamente quando o objeto qrCode é inicializado no método qrCode().
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.i("asd","result");
        if (result != null)
        {
            //if qrcode has nothing in it
            if (result.getContents() == null)
            {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            }
            else
            {
                //if qr contains data
                try
                {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //Guarda o valor que é lido do QrCode
                    resultadoCodigo=Integer.parseInt(obj.getString(stringCodigo));//Fazer método para verificar se existe carro com qrcode
                    Toast.makeText(this,stringCodigo+": "+resultadoCodigo,Toast.LENGTH_LONG).show();
                    //Caso seja o Tuc-Tuc para identificar
                    if(codigo == codigoTomda)
                    {
                        tomadaId=resultadoCodigo;
                        if(DBManager.existePulg(tomadaId)){
                            confirmacao("plug",tomadaId);
                        }else{
                            Toast.makeText(this,"Não existe tomada com esse ID",Toast.LENGTH_LONG).show();
                        }

                    }
                    //Caso seja a tomada para identificar
                    else if(codigo == codigoTuc)
                    {
                        tucId = resultadoCodigo;
                        if(DBManager.existeCarro(tucId)) {
                            MyTukxis.setTucId(tucId);//Mudei aqui
                            confirmacao("car", tucId);
                        } else{
                            Toast toast = Toast.makeText(getContext(), "Não existe Tuc com esse id", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void confirmacao(String tipo,int id)
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())
                    {
                        new AlertDialog.Builder(BeingCharging.this)
                                .setTitle(title+" "+tipo+" "+id)
                                .setMessage("Please, check and confirm")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if(tipo.equals("plug"))
                                        {
                                            stringCodigo="IdCarro";
                                            codigo=1;
                                            qrScan.initiateScan();
                                        }
                                        else if(tipo.equals("car"))
                                        {
                                            iniciarOuTerminarCarregar();
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if(tipo.equals("plug"))
                                        {
                                            stringCodigo="idTomada";
                                            codigo=0;
                                            qrScan.initiateScan();
                                        }
                                        else if(tipo.equals("car"))
                                        {
                                            stringCodigo="IdCarro";
                                            codigo=1;
                                            qrScan.initiateScan();
                                        }
                                    }
                                })
                                .show();
                    }
                }
        );
    }

    public void info()
    {
        runOnUiThread(()->
                      {
                          if (!isFinishing())
                    {
                        new AlertDialog.Builder(BeingCharging.this)
                                .setTitle("Informação")
                                .setMessage("Please read plug code. After read and confirm the plug id, please read"+
                                        " car code.")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        stringCodigo = "idTomada";
                                        codigo=0;
                                        qrScan.initiateScan();
                                    }
                                })
                                .show();
                    }
                }
        );
    }

    public void iniciarOuTerminarCarregar()
    {
        Intent intentPerBateria = new Intent(BeingCharging.this, IntroduzirPerBat.class);
        //Verifica se é para iniciar carregamento
        if(getIntent().getIntExtra("opcaoCarregamento",0)==1)
        {
            if(getIntent().getIntExtra("iniciarViagem",0)==1)
            {
                intentPerBateria.putExtra("viagem",1);
            }
            intentPerBateria.putExtra("opcao", 2); //Para indicar que é para inicar carregamento
            startActivity(intentPerBateria);
        }
        //Verifica se é para terminar carregamento
        else if(getIntent().getIntExtra("opcaoCarregamento",0)==2)
        {
            if(getIntent().getIntExtra("iniciarViagem",0)==2)
            {
                intentPerBateria.putExtra("viagem",2);
            }
            intentPerBateria.putExtra("opcao", 3); //Para indicar que é para terminar carregamento
            startActivity(intentPerBateria);
        }
    }

    //public static int getTucId(){return tucId;}
    public static int getTomadaId(){return tomadaId;}


}
