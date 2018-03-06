package com.example.luis.smartcharging;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONException;
import org.json.JSONObject;

public class StartAndStopService extends AppCompatActivity {

    private static final int PERMISSOES = 1;
    private Intent intent;
    private static DBManager db;

    //qr code scanner object
    private IntentIntegrator qrScan;
    private static int idCarro;
    private boolean reconheceuQrCode;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static String userId;
    private static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_and_stop_service);

        intent = new Intent(this, GpsService.class);

        sharedPref=getSharedPreferences("Configuração",Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Verifica as permissões - não avança até que todas as permissões forem cedidas
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
              pedePermissoes();
        }

        if (!DBManager.databaseExists()) {
            DBManager.initDatabase();
        } else {
            Log.i("sdf", "Database já existe");
        }
        db = DBManager.getDBManager();

        //intializing scan object
        qrScan = new IntentIntegrator(this);
        reconheceuQrCode = false;

        if(!userIdRegistado()) {
            registoUserId();
        }

        context=getApplicationContext();
    }

    //Terminamos o serviço para este não continuar activo quando a aplicação é fechada
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(intent!=null) //Apenas paramos o serviço se este estiver a ser executado.
        {
            stopService(intent);
        }
    }

    /*public void getVolleyRequest(View v)
    {
        VolleyRequest.postRequest();
    }*/

    //Método para iniciar uma rota
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
                Toast.makeText(this, "Está uma viagem em curso!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this,"Ainda não identificou o Tuc-Tuc!",Toast.LENGTH_SHORT).show();
        }
    }

    //Método para terminar uma rota
    public void pararServico(View v) throws JSONException {
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
                Toast.makeText(this,"Não estava uma viagem em curso!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Este método é usado para guardar o total de kms diários que um determinado condutor fez
    public void guardaKmsDiários(View v)
    {
        if(!GpsService.getServicoIniciado()) {
            if (db.calculaKmTotaisDiariosCond()) {
                Toast.makeText(this, "Registo diário inserido com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "O registo diário já foi inserido anteriormente!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this,"Termine primeiro a viagem",Toast.LENGTH_LONG).show();
        }
    }

    //Este método é chamado automaticamente quando o objeto qrCode é inicializado no método qrCode().
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
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Para inicializar o objeto que vai ser usado para ler o qrCode
    public void qrCode(View v)
    {
        if(!GpsService.getServicoIniciado())
        {
            //initiating the qr code scan
            qrScan.initiateScan();
        }
        else
        {
            Toast.makeText(this,"Está uma viagem a decorrer!",Toast.LENGTH_LONG).show();
        }
    }

    //Métodos para a verificação das permissões, mostra um alert para cada permissão que ainda não foi cedida

    public void pedePermissoes()
    {
        ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_PHONE_STATE},PERMISSOES);
    }

    //Este método é chamado automaticamente quando é feito o "requestPermissions(...) dentro do método pedePermissoes()
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        boolean concedidas = true;
        int index=0;
        for(int i : grantResults)
        {
            if(i == PermissionChecker.PERMISSION_DENIED)
            {
                concedidas = false;
                break;
            }
            index++;
        }
        final String value=Integer.toString(index);
        if(!concedidas)
        {
            runOnUiThread(()->
                    {
                        final int finalIndex =Integer.parseInt(value);
                        if (!isFinishing())
                        {
                           new AlertDialog.Builder(StartAndStopService.this)
                                    .setTitle("Alerta")
                                    .setMessage("Não pode negar estas permissões!!!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                    {
                                        public static final int REQUEST_PERMISSION_SETTING = 1;

                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            {
                                                if(!shouldShowRequestPermissionRationale(permissions[finalIndex]))
                                                {
                                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                                    intent.setData(uri);
                                                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                                    //Delay para o utilizador não ver o segundo alerta antes de ir às definições
                                                    try {
                                                        Thread.sleep(5000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    //Para saber quando é que utilizador voltou das definições
                                                    new AlertDialog.Builder(StartAndStopService.this)
                                                            .setTitle("Confirmação")
                                                            .setMessage("Confirme que adicionou todas as permissões!")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i)
                                                                {
                                                                    pedePermissoes();
                                                                }
                                                            }).show();
                                                }
                                                else
                                                {
                                                    pedePermissoes();
                                                }
                                            }
                                        }
                                    }).show();
                        }
                    }
            );
        }
    }

    //Para poder identificar os utilizador por exemplo no registo de kms que fez num dia
    public void registoUserId()
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())
                    {
                        final EditText input = new EditText(this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

                        new AlertDialog.Builder(StartAndStopService.this)
                                .setTitle("Identificação")
                                .setMessage("Insira o seu nome para poder ser identificado na aplicação")
                                .setCancelable(false)
                                .setView(input)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        editor.putString("UserIdentificador", input.getText().toString());
                                        editor.apply();
                                        userId=sharedPref.getString("UserIdentificador",null);
                                    }
                                }).show();
                    }
                }
        );
    }

    //Método para verificar se o user já se registou
    public boolean userIdRegistado()
    {
        userId=sharedPref.getString("UserIdentificador",null);
        if(userId!=null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    //Métodos getters
    public static String getUserId() {return userId;}
    public static DBManager getDb()
    {
        return db;
    }
    public static int getIdCarro(){return idCarro;}
    public static Context getContext(){return context;}
}
