package com.example.luis.smartcharging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class StartAndStopService extends AppCompatActivity {

    private Intent intent;
    private static DBManager db;

    //qr code scanner object
    private IntentIntegrator qrScan;
    private static int idCarro;
    private boolean reconheceuQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_and_stop_service);

        intent = new Intent(this, GpsService.class);

        if(!DBManager.databaseExists())
        {
            DBManager.initDatabase();
        }
        else
        {
            Log.i("sdf", "Database já existe");
        }
        db = DBManager.getDBManager();

        //intializing scan object
        qrScan = new IntentIntegrator(this);
        reconheceuQrCode=false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(intent!=null) //Apenas paramos o serviço se este estiver a ser executado.
        {
            stopService(intent);
        }
    }

    public void iniciarServico(View v)
    {
        if(reconheceuQrCode)
        {
            if (!GpsService.getServicoIniciado())//Para iniciar só o serviço quando este ainda não foi iniciado.
            {
                reconheceuQrCode = false;
                startService(intent);
            }
            else
            {
                Toast.makeText(this, "Serviço já foi iniciado anteriormente", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this,"Ainda não identificou o Tuc-Tuc que vai usar",Toast.LENGTH_SHORT).show();
        }
    }

    public void pararServico(View v)
    {
        if(intent!=null)
        {
            double kmViagem=0;
            //Calcula os kms da última viagem

            //Para não permitir o serviço ser parado antes de ser iniciado.
            if(GpsService.getServicoIniciado())
            {
                int idViagem=GpsService.getIdViagem();
                kmViagem = DBManager.calculaKmViagem(idViagem);
                stopService(intent);

                //Começamos a nova actividade que irá mostrar os kms, bateria, carregar etc
                Intent intentPrincipal = new Intent(StartAndStopService.this, ShowLocationActivity.class);
                intentPrincipal.putExtra("distanciaKm", kmViagem);
                intentPrincipal.putExtra("idViagem", idViagem);
                startActivity(intentPrincipal);
            }
            else
            {
                Toast.makeText(this,"O serviço não estava activo",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void guardaKmsDiários(View v)
    {
        if(db.calculaKmTotaisDiariosCond())
        {
            Toast.makeText(this,"Registo diário inserido com sucesso!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,"O registo diário já foi inserido anteriormente!",Toast.LENGTH_SHORT).show();
        }
    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

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
                    idCarro=Integer.parseInt(obj.getString("IdCarro"));
                    reconheceuQrCode=true;
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void qrCode(View v)
    {
        //initiating the qr code scan
        qrScan.initiateScan();
    }

    public static DBManager getDb()
    {
        return db;
    }
    public static int getIdCarro(){return idCarro;}
}
