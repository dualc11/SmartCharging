package com.example.luis.smartcharging;


import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.luis.dataclass.LogInfo;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import static com.example.luis.smartcharging.DBManager.createCsvFileGps;
import static com.example.luis.smartcharging.DBManager.getAllDeslocacao;
import static com.example.luis.smartcharging.DBManager.getAllLogViagem;
import static com.example.luis.smartcharging.DBManager.getAllViagem;
import static com.example.luis.smartcharging.DBManager.getDBManager;
import static com.example.luis.smartcharging.DBManager.getLogDeslocacao;
import static com.example.luis.smartcharging.DBManager.getLogViagem;
import static com.example.luis.smartcharging.DBManager.getViagem;
import static com.example.luis.smartcharging.DBManager.isToUpdateDB;
import static com.example.luis.smartcharging.DBManager.updateDB;
import static com.example.luis.smartcharging.MyTukxis.refreshCarsAndPlug;

public class Login extends AccountAuthenticatorActivity {
    private static final int PERMISSOES = 1;
    private static final String TAG = "autherror";
    private static final String LASTUPDATETIME = "lastUpdateDate";
    private static final int Unauthorized = 401;
    private static Context context;
    private String user;
    private String password;
    private static String token = "";
    private static final int SIGN_IN_CODE = 0;
    private static final String MIME_TYPE_DATABASE = "application/x-sqlite-3";
    private static DBManager db;

    private static DriveClient mDriveClient;
    private static DriveResourceClient mDriveResourceClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            pedePermissoes();

        }
        SharedPreferences sPref = getSharedPreferences("loginInfo",MODE_PRIVATE);
        if(sPref.contains("token")){//Se já existir a token
            token = sPref.getString("token",null);
            VolleyRequest.setToken(token);//Atualiza o valor token no VolleyRequest
          //  checkUploadDrive(sPref);
            db = getDBManager();
            signIn();

        }else{
            setContentView(R.layout.activity_login);
        }


    }
    //Método responsável por criar a atividade que permite ao user escolher a conta da drive
    private void signIn() {
        Log.i(TAG, "Start sign in");
        GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
        GoogleSignInClient.silentSignIn();
        startActivityForResult(GoogleSignInClient.getSignInIntent(), SIGN_IN_CODE);
    }
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }
    //Função evocado quando é clicado no botão de login
    public void doLogin(View v){
        EditText userED = (EditText) findViewById(R.id.user);
        user = userED.getText().toString();

        EditText passwordED = (EditText) findViewById(R.id.password);
        password = passwordED.getText().toString();
        ProgressBar progressBar = findViewById(R.id.pBar);
        LinearLayout linearLayout = findViewById(R.id.linearLogin);
        linearLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if (progressBar.isShown()){
         Log.e("coisa","coisa");
        };
            VolleyRequest.getToken(user, password, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray result) {

                }

                @Override
                public void onSucess(JSONObject result) {
                    saveLoginInfo(result);
                    progressBar.setVisibility(View.GONE);
                    changeToMyTuxis();
                }
                @Override
                public void onFail(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(v.getContext(),R.string.login_error,Toast.LENGTH_LONG);


                }
            });
    }
    //Guarda a informação do user(id, nome,token)
    public void saveLoginInfo(JSONObject loginInfo){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sPref = getContext().getSharedPreferences("loginInfo",MODE_PRIVATE);
                try {
                    JSONObject info = loginInfo;
                    SharedPreferences.Editor editor = sPref.edit();
                    editor.putString("token",info.getString("access_token"));
                    JSONObject driver = info.getJSONObject("driver");
                    editor.putInt("driverId",driver.getInt("id"));
                    editor.putString("driverFirstName",driver.getString("first_name"));
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void changeToMyTuxis(){
        Intent myTukxisIntent =  new Intent(Login.getContext(),MyTukxis.class);
        startActivity(myTukxisIntent);
    }
    public static Context getContext(){return context;}
    public static String getToken(){return token;}
    //Método evocado quando o user acaba de escolher a conta da google drive
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGN_IN_CODE:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    // Use the last signed in account here since it already have a Drive scope.

                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Build a drive resource client.
                    mDriveResourceClient =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    createAllCSVFiles();

                  /* createCsvFileGps(Environment.getExternalStorageDirectory()+DBManager.getFolderDatabase(),
                                    "logViagem.csv",getLogViagem(1));
                    saveAnyFileToDrive(this,mDriveResourceClient,
                                        new File(Environment.getExternalStorageDirectory(),
                                                DBManager.getFolderDatabase() + DBManager.getDatabaseName()),
                                        DBManager.getDatabaseName(),
                                        MIME_TYPE_DATABASE);
                    saveAnyFileToDrive(this,mDriveResourceClient,
                            new File(Environment.getExternalStorageDirectory(),
                                    DBManager.getDatabaseName()),
                            DBManager.getDatabaseName(),
                            MIME_TYPE_DATABASE);
                    saveAnyFileToDrive(this,mDriveResourceClient,
                            new File(Environment.getExternalStorageDirectory(),
                                    "logViagem.csv"),
                            "logViagem.csv",
                            MIME_TYPE_DATABASE);
                    File file = new File(Environment.getExternalStorageDirectory(),
                            "topicos.txt");*/
                    changeToMyTuxis();
                }else{
                    changeToMyTuxis();
                }
                break;
            default:
                changeToMyTuxis();
                break;
        }
    }
    //Método que recebe um ficheiro do telemovel e cria um ficheiro na drive
    public static void saveAnyFileToDrive(Activity activity,DriveResourceClient mDriveResourceClient, File file,
                                          String driveFileName, String driveFileMimeType) {
        final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(rootFolderTask,createContentsTask).continueWithTask(task -> {
            InputStream is = new FileInputStream(file);
            DriveContents contents = createContentsTask.getResult();
            DriveFolder parent = rootFolderTask.getResult();

            OutputStream outputStream = contents.getOutputStream();

            byte[] buffer = new byte[1024];
            int n;
            while ((n = is.read(buffer,0,buffer.length)) > 0) {
                outputStream.write(buffer, 0, n);
                outputStream.flush();
            }
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(driveFileName)
                    .setMimeType(driveFileMimeType)
                    .setStarred(true)
                    .build();
            return mDriveResourceClient.createFile(parent,changeSet, contents);
        }).addOnSuccessListener(activity,
                driveFile -> {
                    Toast.makeText(activity,"CreateFile ANY FILE CREATED",Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(activity, e -> {
                    Log.e(TAG, "Unable to create file", e);
                });
    }
    //Método que verifica se existe o campo "last update time" no shredPref
    public boolean existUpdateDate(SharedPreferences sharedPreferences){
        return sharedPreferences.contains(LASTUPDATETIME);
    }
    //Método que verifica se foi feito upload no último dia
    public static boolean  isToUpdateDrive(String lastUpdateDate){
        Date lastUpdateDate_ = null,currentDate = null;String _currentDate = null;
        try {
            lastUpdateDate_  = new SimpleDateFormat("dd/MM/yyyy").parse(lastUpdateDate);
            _currentDate = new SimpleDateFormat("dd/MM/yyyy").
                    format(Calendar.getInstance().getTime());
            currentDate = new SimpleDateFormat("dd/MM/yyyy").parse(_currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return lastUpdateDate_.before(currentDate);
    }

    public static DriveClient getmDriveClient() {
        return mDriveClient;
    }

    public static DriveResourceClient getmDriveResourceClient() {
        return mDriveResourceClient;
    }
    //Verifica se é fazer upload do base de dados para a fazer
    //Vê se existe o campo no sharedPref com a data do ultimo upload se não existir então
    //cria esse campo e faz o login e upload. Se existir o campo então verifica se já foi feito
    //upload no ultimo dia.
    public void checkUploadDrive(SharedPreferences sharedPreferences){
        new Thread(()->{
            if(existUpdateDate(sharedPreferences)){
                if(isToUpdateDrive(sharedPreferences.getString(LASTUPDATETIME,null))){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(LASTUPDATETIME,new SimpleDateFormat("dd/MM/yyyy").
                            format(Calendar.getInstance().getTime()));// Atualiza o valor do campo "last update time" com a dia de hoje
                    editor.commit();
                    signIn();//Obtem a informação sobre a conta da drive
                }else{
                    changeToMyTuxis();
                }
            }else{
           /*     SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(LASTUPDATETIME,new SimpleDateFormat("dd/MM/yyyy").
                        format(Calendar.getInstance().getTime()));
                editor.commit();*/
                signIn();
            }

        }).start();
    }
    public void pedePermissoes()
    {
        ActivityCompat.requestPermissions(this,new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
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
                        final int finalIndex = Integer.parseInt(value);
                        if (!isFinishing())
                        {
                            new AlertDialog.Builder(Login.this)
                                    .setTitle("Alerta")
                                    .setMessage(getResources().getString(R.string.permission_denied))
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
        }else{
            if (!DBManager.databaseExists()) {
                DBManager.initDatabase();
            }

            db = DBManager.getDBManager();

            if(isToUpdateDB()){
                updateDB();
            }


     /*       SharedPreferences sPref = getSharedPreferences("loginInfo",MODE_PRIVATE);
            if(isToUpdateDrive(sPref.getString("lastUpdateDate",null))){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveAnyFileToDrive(getParent(),getmDriveResourceClient(),
                                new File(Environment.getExternalStorageDirectory()
                                        ,DBManager.getFolderDatabase()+DBManager.getDatabaseName()),
                                DBManager.getDatabaseName(),
                                "application/x-sqlite-3");
                        SharedPreferences.Editor editor = sPref.edit();
                        editor.putString("lastUpdateDate",new SimpleDateFormat("dd/MM/yyyy").
                                format(Calendar.getInstance().getTime()));
                        editor.commit();
                    }
                }).start();
            }else{
                Toast.makeText(this,"Shit",Toast.LENGTH_LONG);
            }*/
           // refreshCarsAndPlug();
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
                .setMessage(getResources().getString(R.string.permission_comfirm))
                .setCancelable(false)
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        pedePermissoes();
                    }
                }).show();
    }
    public void createAllCSVFiles(){
        ArrayList<LogInfo> viagens = getAllViagem();
        File file = new File(Environment.getExternalStorageDirectory()+DBManager.getFolderDatabase()+"/logViagem.csv");
            Log.e("create",file.getAbsolutePath());
            for (LogInfo viagem: viagens){
                ArrayList<?> log = getLogViagem(viagem.getId());
                if(log.size()>0 ){
                    createCsvFileGps(this,Environment.getExternalStorageDirectory()+DBManager.getFolderDatabase(),
                            file,log,"," + viagem.getBatInicial()+" , "+viagem.getBatFinal());
                }
            }
            ArrayList<LogInfo> deslocacoes = getAllDeslocacao();
          for (LogInfo deslocacao: deslocacoes){
                ArrayList<?> log = getLogDeslocacao(deslocacao.getId());
                if(log.size()>0 ){
                    createCsvFileGps(this,Environment.getExternalStorageDirectory()+DBManager.getFolderDatabase(),
                            file,getLogDeslocacao(deslocacao.getId()),"," + deslocacao.getBatInicial()+" , "+deslocacao.getBatFinal());
                }
            }

    }

}
