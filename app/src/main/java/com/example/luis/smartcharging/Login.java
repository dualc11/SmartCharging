package com.example.luis.smartcharging;


import android.accounts.AccountAuthenticatorActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AccountAuthenticatorActivity {
    private static final String TAG = "autherror";
    private static final int Unauthorized = 401;
    private static Context context;
    private String user;
    private String password;
    private static String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        SharedPreferences sPref = getSharedPreferences("loginInfo",MODE_PRIVATE);
        if(sPref.contains("token")){//Se já existir a token, é mudado para o menu mytuxis
            token = sPref.getString("token",null);
            VolleyRequest.setToken(token);//Atualiza o valor token no VolleyRequest
            changeToMyTuxis();
        }else{
            setContentView(R.layout.activity_login);
        }
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
}
