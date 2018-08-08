package com.example.luis.smartcharging;


import android.accounts.AccountAuthenticatorActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Login extends AccountAuthenticatorActivity {
    private static final String TAG = "autherror";
    private static final int Unauthorized = 401;
    private static Context context;
    private String user;
    private String password;
    private static String token = "";

    private static final int SIGN_IN_CODE = 0;

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        SharedPreferences sPref = getSharedPreferences("loginInfo",MODE_PRIVATE);
        if(sPref.contains("token")){//Se já existir a token, é mudado para o menu mytuxis
            token = sPref.getString("token",null);
            VolleyRequest.setToken(token);//Atualiza o valor token no VolleyRequest
            signIn();

        }else{
            setContentView(R.layout.activity_login);
        }


    }
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

    public void doLogin(View v){//Função evocado quando é clicado no botão de login
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
                //Funções responsaveis que ocorrem depois da resposta do servidor
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
                    Toast.makeText(v.getContext(),"Wrong password or email! Please try again",Toast.LENGTH_LONG);


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
                    // Start camera.
                    saveFileToDrive();
                    File file = new File(Environment.getExternalStorageDirectory(),
                            "topicos.txt");
                    saveAnyFileToDrive(mDriveResourceClient,file,"MERDA.txt","text/plain");
                    changeToMyTuxis();
                }
                break;
        }
    }
    public void saveFileToDrive() {
        final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(rootFolderTask,createContentsTask).continueWithTask(task -> {
            File db_file = new File(Environment.getExternalStorageDirectory(),
                    DBManager.getFolderDatabase() + DBManager.getDatabaseName());

            InputStream is = new FileInputStream(db_file);
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
                    .setTitle(DBManager.getDatabaseName())
                    .setMimeType("application/x-sqlite-3")
                    .setStarred(true)
                    .build();
            return mDriveResourceClient.createFile(parent,changeSet, contents);
        }).addOnSuccessListener(this,
                driveFile -> {
                    Toast.makeText(this,"CreateFile",Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                });
    }
    public void saveAnyFileToDrive(DriveResourceClient mDriveResourceClient,File file,
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
        }).addOnSuccessListener(this,
                driveFile -> {
                    Toast.makeText(this,"CreateFile ANY FILE CREATED",Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                });
    }

}
