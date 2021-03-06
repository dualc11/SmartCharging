package com.example.luis.smartcharging;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;

import android.support.design.widget.NavigationView;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;


import static com.example.luis.smartcharging.Login.*;
import static com.example.luis.smartcharging.VolleyRequest.loadCarros;
import static com.example.luis.smartcharging.VolleyRequest.loadPlug;

public class MyTukxis extends AppCompatActivity{

    private static final int PERMISSOES = 1;
    private static final String TAG = "MyTukxis";
    private static Intent intent;
    private static DBManager db;
    private IntentIntegrator qrScan;
    private static int idCarro;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private static int userId;
    private static Context context;
    private DrawerLayout dLayout;
    private Toolbar toolbar;
    private static String userName;
    private static  ImageView imageView = null;
    private static boolean isCarsRefreshed = false;
    private static final int TIPOVIAGEM = 1;
    private static final int TIPOUTILIZACAO = 2;
    private static boolean isToUpdateUserInfo = true;
    private static final int SLEEPTIME = 60000*60*5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tukxis);
        updateUserInfo();
        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        navigationClick(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.my_tukxis));

        intent = new Intent(this, GpsService.class);

        context = getApplicationContext();
        //intializing scan object
        qrScan = new IntentIntegrator(this);
        db = DBManager.getDBManager();
    }
    //Atualiza as varias locais com os valores do nome userId
    public void updateUserInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isToUpdateUserInfo){
                    sharedPref = getSharedPreferences("loginInfo",Context.MODE_PRIVATE);
                    editor = sharedPref.edit();
                    userName = sharedPref.getString("driverFirstName",null);
                    userId = sharedPref.getInt("driverId",0);
                    isToUpdateUserInfo = false;
                }
            }
        }).start();

    }
    public void checkCars(){
        if(!isCarsRefreshed){
            VolleyRequest.loadCarros();
            isCarsRefreshed = true;
        }
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
        menu.getItem(0).setTitle(userName);

        // implement setNavigationItemSelectedListener event on NavigationView
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Fragment frag = null; // create a Fragment Object
                int itemId = menuItem.getItemId(); // get selected menu item's id
                // check selected menu item's id and replace a Fragment Accordingly
                Intent intent;
                switch (itemId){
                    case R.id.home:
                        dLayout.closeDrawers();
                        intent= new Intent(getApplicationContext(),MyTukxis.class);
                        startActivity(intent);
                        return true;
                    case R.id.pickUp:
                        dLayout.closeDrawers();
                        iniciarServico();
                        return true;
                    case R.id.dropOff:
                        dLayout.closeDrawers();
                        try {

                            pararServico(2);//Receber um tipo de indica se é para parar a viagem ou utilizacao carro
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return true;
                    case R.id.myTrip:
                        dLayout.closeDrawers();
                        if(GpsService.getServicoIniciado()) {
                            intent = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.not_in_tour),Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.carregar:
                        dLayout.closeDrawers();
                        iniciarCarregamento();
                        return true;
                    case R.id.aCarregar:
                        dLayout.closeDrawers();
                        intent=new Intent(getApplicationContext(),CarsCharging.class);
                        startActivity(intent);
                        return true;
                    case R.id.stopCharging:
                        dLayout.closeDrawers();
                        terminarCarregar();
                        return true;
                    case R.id.tucsDisponiveis:
                        dLayout.closeDrawers();
                        verTucsDisp();
                        return true;
                    case R.id.registoDiario:
                        dLayout.closeDrawers();
                         intent=new Intent(getApplicationContext(),Register.class);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }

            }
        });
    }

    public void seekBar(View v)
    {
        Intent intentPrincipal = new Intent(MyTukxis.this, IntroduzirPerBat.class);
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
   public void iniciarServico()
    {
        if(!GpsService.getServicoIniciado())
        {
            /*String mensagem= "If the car that you want to pick up is still connected to a socket, please," +
                    "unplug it and use QR Code placed on the plug. Otherwise, please, skip this step";*/
            String mensagem = getResources().getString(R.string.qr_first_message);
            alerta(mensagem,false);
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.tour_already_started), Toast.LENGTH_SHORT).show();
        }
    }

    //Método para terminar uma rota
    public void pararServico(int tipo) throws JSONException {
            if(intent!=null)
            {
                //Para não permitir o serviço ser parado antes de ser iniciado.
                if(GpsService.getServicoIniciado())
                {
                    //String mensagem= "If you want put charging you car use QR Code placed on the plug. Otherwise, please, skip this step";
                    String mensagem = getResources().getString(R.string.qr_second_message);
                    alerta(mensagem,true);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.not_using_car),Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Result Not Found", Toast.LENGTH_LONG).show();
            }
            else
            {
                //if qr contains data
                try
                {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    idCarro = Integer.parseInt(obj.getString("IdCarro"));
                    if(!GpsService.getServicoIniciado()) {
                        VolleyRequest.loadCarros();
                        if(DBManager.existeCarro(idCarro)){//Verifica se existe um carro com esse id
                            confirmacaoIdTuc(getResources().getString(R.string.pick_up_car), idCarro);
                        }else{
                                Toast.makeText(getApplicationContext(),"Não existe carro com esse Id",Toast.LENGTH_LONG).show();

                        }
                    }
                    else
                    {
                        confirmacaoIdTuc(getResources().getString(R.string.drop_off_car), idCarro);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();

                    Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_LONG).show();
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


    public void alerta(String mnsg,boolean carregar)
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())//Enquanto a atviidade ainda está em progresso
                    {
                        new AlertDialog.Builder(MyTukxis.this)
                                .setTitle(getResources().getString(R.string.acess_to_camera))
                                .setMessage(mnsg)
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        //Drop off car
                                        if(carregar)
                                        {
                                            //iniciarCarregamento();
                                            Intent intent = new Intent(getApplicationContext(),BeingCharging.class);
                                            intent.putExtra("opcaoCarregamento",1);
                                            intent.putExtra("iniciarViagem",1);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            //terminarCarregar();
                                            Intent intent = new Intent(getApplicationContext(),BeingCharging.class);
                                            intent.putExtra("opcaoCarregamento",2);
                                            intent.putExtra("iniciarViagem",2);
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        qrCode();
                                    }
                                })
                                .show();
                    }
                }
        );
    }

    public void confirmacaoIdTuc(String title,int id)
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())
                    {
                        new AlertDialog.Builder(MyTukxis.this)
                                .setTitle(title+" "+id)
                                .setMessage(getResources().getString(R.string.check_and_confirm_qr_code))
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        Intent intent=new Intent(getApplicationContext(),IntroduzirPerBat.class);
                                        boolean existeCarro = DBManager.existeCarro(idCarro);
                                        if(existeCarro){
                                            if(!GpsService.getServicoIniciado()) {
                                                intent.putExtra("opcao", 0);
                                            }
                                            else
                                            {
                                                intent.putExtra("opcao",1);
                                            }
                                            intent.putExtra("idCarro",idCarro);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            Log.e("Existe Carro","Id carro não existe");
                                            Toast.makeText(getContext(),getResources().getString(R.string.wrong_qr_car),Toast.LENGTH_LONG);
                                        }

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
/*
    //Para poder identificar os utilizador por exemplo no registo de kms que fez num dia
    public void registoUserId()
    {
        runOnUiThread(()->
                {
                    if (!isFinishing())
                    {
                        final EditText input = new EditText(this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

                        new AlertDialog.Builder(MyTukxis.this)
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
    }*/
    //Alterei aqui
    public void iniciarCarregamento()
    {
        if(!GpsService.getServicoIniciado()) {
            Intent intentCarregar = new Intent(MyTukxis.this, BeingCharging.class);
            intentCarregar.putExtra("opcaoCarregamento", 1); //Para indicar que é para inicar carregamento
            //intentCarregar.putExtra("iniciarTerminar",1);
            startActivity(intentCarregar);
        }
        else
        {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.end_tour_fisrt),Toast.LENGTH_SHORT).show();
        }
    }

    public void terminarCarregar()
    {
        Intent intentCarregar = new Intent(MyTukxis.this,BeingCharging.class);
        intentCarregar.putExtra("opcaoCarregamento",2); //Para indicar que é para terminar carregamento
        //intentCarregar.putExtra("iniciarTerminar",1);
        startActivity(intentCarregar);
    }

    public void verTucsDisp()
    {
        Intent intentVerTucsDisp=new Intent(MyTukxis.this,Fleet.class);
        startActivity(intentVerTucsDisp);
    }

    //Métodos getters
    public static int getUserId() {return userId;}
    public static DBManager getDb()
    {
        return db;
    }
    public static int getIdCarro(){return idCarro;}
    public static Context getContext(){return context;}
    public static Intent getIntentGps(){return intent;}

    public static void setTucId(int tucId){idCarro=tucId;}//Mudei aqui
    public void setUpToolBar(String nameToolBar){//Responsável pela toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("Tours menu");
        navigationClick(toolbar);
    }
    public static void refreshCarsAndPlug(){
        new Thread(() -> {
            while (true){
                loadCarros();
                loadPlug();
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
