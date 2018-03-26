package com.example.luis.smartcharging;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONException;
import org.json.JSONObject;

public class StartAndStopService extends AppCompatActivity {

    private static final int PERMISSOES = 1;
    private static Intent intent;
    private Intent intentSeekBar;
    private static DBManager db;
    private IntentIntegrator qrScan;
    private static int idCarro;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static String userId;
    private static Context context;
    private static Button bTerminarViagem,bIniciarViagem;
    private DrawerLayout dLayout;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_and_stop_service);

        sharedPref=getSharedPreferences("Configuração",Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        userId=sharedPref.getString("UserIdentificador",null);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.menuicon));

        navigationClick(toolbar);
        getSupportActionBar().setTitle("My Tukxi");

        /*bIniciarViagem=findViewById(R.id.bIniciarViagem);
        bTerminarViagem=findViewById(R.id.bPararViagem);
        buttonsVisibility();*/

        intentSeekBar= new Intent(this,IntroduzirPerBat.class);
        intent = new Intent(this, GpsService.class);

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

        if(!userIdRegistado()) {
            registoUserId();
        }

        context=getApplicationContext();
    }

    //Método para saber quando o utilizador carregou na toolbar
    public void navigationClick(Toolbar toolbar)
    {
        // implement setNavigationOnClickListener event
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dLayout.openDrawer(Gravity.LEFT);
            }
        });
        setNavigationDrawer(); // call method
    }

    //Método para ver qual dos menus foi carregado
    public void setNavigationDrawer() {
        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // initiate a DrawerLayout
        NavigationView navView = (NavigationView) findViewById(R.id.navigation); // initiate a Navigation View
        Menu menu = navView.getMenu();
        menu.add(getUserId());
        menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.accounticon));
        // implement setNavigationItemSelectedListener event on NavigationView
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Fragment frag = null; // create a Fragment Object
                int itemId = menuItem.getItemId(); // get selected menu item's id
                // check selected menu item's id and replace a Fragment Accordingly
                if (itemId == R.id.myTukxis)
                {
                    Intent intent =new Intent(getApplicationContext(),StartAndStopService.class);
                    startActivity(intent);
                    dLayout.closeDrawers();
                    return true;
                }
                else if (itemId == R.id.myTrip) {
                    dLayout.closeDrawers();
                    if(GpsService.getServicoIniciado()) {
                        Intent intent = new Intent(getApplicationContext(), MyTrip.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Não está nenhuma viagem em curso",Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                else if (itemId == R.id.carregar) {
                    dLayout.closeDrawers();
                    iniciarCarregamento();
                    return true;
                }
                else if (itemId == R.id.acabarCarregar) {
                    dLayout.closeDrawers();
                    terminarCarregar();
                    return true;
                }
                else if (itemId == R.id.tucsDisponiveis) {
                    dLayout.closeDrawers();
                    verTucsDisp();
                    return true;
                }
                else if (itemId == R.id.registoDiario) {
                    dLayout.closeDrawers();
                    guardaKmsDiários();
                    return true;
                }
                return false;
            }
        });
    }

    public void buttonsVisibility()
    {
        if(GpsService.getServicoIniciado())
        {
            bIniciarViagem.setVisibility(View.GONE);
            bTerminarViagem.setVisibility(View.VISIBLE);
        }
        else
        {
            bIniciarViagem.setVisibility(View.VISIBLE);
            bTerminarViagem.setVisibility(View.GONE);
        }
    }

    public void seekBar(View v)
    {
        Intent intentPrincipal = new Intent(StartAndStopService.this, IntroduzirPerBat.class);
        startActivity(intentPrincipal);
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

    /*Este método é chamado quando o botão back é carregado*/
    @Override
    public void onBackPressed() {
        moveTaskToBack(true); //Para minimizar a aplicação
        //super.onBackPressed();
    }

    //Método para iniciar uma rota
   public void iniciarServico(View v)
    {
        if(!GpsService.getServicoIniciado())
        {
            qrCode();
        }
        else
        {
            Toast.makeText(this, "Está uma viagem em curso!", Toast.LENGTH_SHORT).show();
        }
    }

    //Método para terminar uma rota
    public void pararServico(View v) throws JSONException {
        if(intent!=null)
        {
            //Para não permitir o serviço ser parado antes de ser iniciado.
            if(GpsService.getServicoIniciado())
            {
                intentSeekBar.putExtra("opcao",1); //Para indicar que é para terminar a viagem
                intentSeekBar.putExtra("idCarro",idCarro);
                startActivity(intentSeekBar);
            }
            else
            {
                Toast.makeText(this,"Não estava uma viagem em curso!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Este método é usado para guardar o total de kms diários que um determinado condutor fez
    public void guardaKmsDiários()
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
                    confirmacaoIdTuc();
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
    public void qrCode()
    {
            //initiating the qr code scan
            qrScan.initiateScan();
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
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            {
                                                if(!shouldShowRequestPermissionRationale(permissions[finalIndex]))
                                                {
                                                    alertaDefinicoes();
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

    public void confirmacaoIdTuc()
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())
                    {
                        new AlertDialog.Builder(StartAndStopService.this)
                                .setTitle("You picked up car "+idCarro)
                                .setMessage("Please, check and confirm")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        //Vai para o intent de colocar a percentagem de bateria
                                        intentSeekBar.putExtra("opcao",0); //Para indicar que é para iniciar a viagem
                                        intentSeekBar.putExtra("idCarro",idCarro);
                                        startActivity(intentSeekBar);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        qrCode();
                                    }
                                })
                                .show();
                    }
                }
        );
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

    public void alertaDefinicoes()
    {
        int REQUEST_PERMISSION_SETTING = 1;
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
        new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Confirme que adicionou todas as permissões")
                .setCancelable(false)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                       pedePermissoes();
                    }
                }).show();
    }
    //Alterei aqui
    public void iniciarCarregamento()
    {
        Intent intentCarregar=new Intent(StartAndStopService.this,Carregamento.class);
        intentCarregar.putExtra("opcaoCarregamento",1); //Para indicar que é para inicar carregamento
        startActivity(intentCarregar);
    }

    public void terminarCarregar()
    {
        Intent intentCarregar=new Intent(StartAndStopService.this,Carregamento.class);
        intentCarregar.putExtra("opcaoCarregamento",2); //Para indicar que é para terminar carregamento
        startActivity(intentCarregar);
    }

    public void verTucsDisp()
    {
        Intent intentVerTucsDisp=new Intent(StartAndStopService.this,VerTucsDisponiveis.class);
        startActivity(intentVerTucsDisp);
    }

    //Métodos getters
    public static String getUserId() {return userId;}
    public static DBManager getDb()
    {
        return db;
    }
    public static int getIdCarro(){return idCarro;}
    public static Context getContext(){return context;}
    public static Intent getIntentGps(){return intent;}
}
