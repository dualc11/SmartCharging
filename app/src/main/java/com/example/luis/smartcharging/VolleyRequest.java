package com.example.luis.smartcharging;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.example.luis.smartcharging.MyTukxis.getContext;

public class VolleyRequest {

    private static final RequestQueue queue = Volley.newRequestQueue(Login.getContext());
    private static final String url="http://66.175.221.248:300/test";
    //private static final String url="http://10.2.0.70:3000/teste";
    //Para a conta
    private static final String urlCars="https://smile.prsma.com/tukxi/api/cars/status";
    private static final String urlActionPickDrop="";
    private static final String URL_CARROS ="https://smile.prsma.com/tukxi/api/cars";
    private static String token = "";
    private static String URL_PLUG = "https://smile.prsma.com/tukxi/api/plugs?access_token="+token;
    private static final String URL_SEND_DRIVER = "https://smile.prsma.com/tukxi/api/car/";
    private static boolean existToken = false;


    /**
 * VARIAVEIS SOBRE A PASSWORD
 */

   private static String username;
    private static String password;
    private static final String URL_LOGIN ="https://smile.prsma.com/tukxi/api/auth/token";
    public static void getRequest()
    {
        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        Log.i("Response", response.toString());
                        //JSONObject jsonObject = response.getJSONObject(i);
                        //jsonObject.getString("title");
                        //OU
                        //response.getString("title");

                        //username e password
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

// add it to the RequestQueue
        queue.add(getRequest);
    }

    public static void postRequest(String idCarro, String idDriver, String batInicial, String batFinal, String kmsViagem, JSONArray jsonArray)
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("idCarro", idCarro);
                params.put("idDriver", idDriver);
                params.put("batInicial",batInicial);
                params.put("batFinal",batFinal);
                params.put("kmsViagem",kmsViagem);
                params.put("coordenadas",jsonArray.toString());
                //plug, a null quando não for para pôr a carregar
                //time
                return params;
            }
        };
        queue.add(postRequest);
    }



    public static ArrayList<DadosFleet> getCarsStatus(final VolleyCallback callback){
        ArrayList<DadosFleet> listaDadosCarros = new ArrayList<>();
        JsonArrayRequest postRequest = new JsonArrayRequest(urlCars,new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFail(error);
            }
        });
        queue.add(postRequest);
        return listaDadosCarros;
    }
    public static void loadCarros(){

        JsonArrayRequest postRequest = new JsonArrayRequest(URL_CARROS,new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                int id;
                int numero;
              for (int i=0;i<response.length();i++){
                  try {
                      JSONObject carro = response.getJSONObject(i);
                      id = carro.getInt("id");
                      numero = carro.getInt("number");
                      DBManager.inserirCarro(id,numero);
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }

              }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Log.e("errorCars",""+error.toString());
                Toast toast = Toast.makeText(getContext(), "Não foi possível atualizar os carros", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        queue.add(postRequest);

    }
    public static void sendPickUp(int carId,int batLevel,int plugId){
        String url = URL_SEND_DRIVER+carId+"/action/pickup?access_token="+token;
        StringRequest driverInfoRequest =  new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject =  new JSONObject(response);
                    Log.e("sendPickup",jsonObject.getString("body"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.toString());
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    String plug = "";
                    if(plugId==0){plug = null;}else{plug = String.valueOf(plugId);}
                    return new JSONObject()
                            .put("batLevel",String.valueOf(batLevel))
                            .put("plugId",plug)
                            .toString().getBytes();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        queue.add(driverInfoRequest);
    }
    public static void getDriverInfo(){

    }
    public static void loadPlug(){
        StringRequest postRequest = new StringRequest(Request.Method.GET,URL_PLUG,new Response.Listener<String>(){
            @Override
            public void onResponse(String responseString) {
                try {
                    JSONArray response =new JSONArray(responseString);
                    for(int i = 0;i<response.length();i++){
                            JSONObject plug = response.getJSONObject(i);
                            DBManager.inserirPlug(plug.getInt("id"),plug.getInt("number"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.e("coisa",URL_PLUG);
                    String merda = new String(error.networkResponse.data,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Toast toast = Toast.makeText(getContext(), "Não foi possível atualizar as tomadas", Toast.LENGTH_LONG);
                toast.show();
            }

        }){


        };
        queue.add(postRequest);
    }
    public static void postViagem (int viagemId,int deslocacaoId){
         StringRequest postRequest = new StringRequest(Request.Method.POST, "https://smile.prsma.com/tukxi/api/car/1/action/pickup?access_token="+token
                ,
              new Response.Listener<String>()
              {
                  @Override
                  public void onResponse(String response) {
                      // response

                      JSONObject res = null;
                      try {
                          res = new JSONObject(response);
                          String body = res.getString("body");
                          Log.e("res",body);
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }


                      Log.d("Response", response);
                  }
              },
              new Response.ErrorListener()
              {
                  @Override
                  public void onErrorResponse(VolleyError error) {
                      Log.e("Error.Response", error.toString());
                  }
              }
      ) {
         @Override
         public byte[] getBody() throws AuthFailureError {

             String res = "";
             ArrayList<GPSLogger> listaGPSLogger = DBManager.getLogViagem(viagemId);
             ArrayList<GPSLogger> listaGpsDeslocacao = DBManager.getLogDeslocacao(deslocacaoId);
             JSONObject resultado = new JSONObject();
             JSONArray jsonArray = new JSONArray();
             for (int i = 0;i<listaGPSLogger.size();i++){
                 JSONObject jsonObject = new JSONObject();
                 try {
                     jsonObject.put("altitude", Float.toString(listaGPSLogger.get(i).getAltitude()));
                     jsonObject.put("longitude", Float.toString(listaGPSLogger.get(i).getLongitude()));
                     jsonObject.put("latitude",Float.toString(listaGPSLogger.get(i).getAltitude()));
                     jsonObject.put("data", Long.toString(listaGPSLogger.get(i).getData().getTime()));

                     jsonArray.put(jsonObject);

                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
             try {
                 resultado.put("logRota",jsonArray);
             } catch (JSONException e) {
                 e.printStackTrace();
             }

             for (int i = 0;i<listaGpsDeslocacao.size();i++){
                 JSONObject jsonObject = new JSONObject();
                 try {
                     jsonObject.put("altitude", Float.toString(listaGpsDeslocacao.get(i).getAltitude()));
                     jsonObject.put("longitude", Float.toString(listaGpsDeslocacao.get(i).getLongitude()));
                     jsonObject.put("latitude",Float.toString(listaGpsDeslocacao.get(i).getAltitude()));
                     jsonObject.put("data", Long.toString(listaGpsDeslocacao.get(i).getData().getTime()));

                     jsonArray.put(jsonObject);

                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
             try {
                 resultado.put("logDeslocacao",jsonArray);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return resultado.toString().getBytes();
         }

             @Override
             public String getBodyContentType() {
                 return "application/json; charset=utf-8";
             }

             @Override
          protected Map<String, String> getParams()
          {
              HashMap<String,String> params = new HashMap<>();
            return params;
          }

         @Override
         protected String getParamsEncoding() {
             return super.getParamsEncoding();
         }
     };
      queue.add(postRequest);
  }

  public static void getToken(String username, String password, final VolleyCallback volleyCallback){
      SharedPreferences sPref = Login.getContext().getSharedPreferences("loginInfo",MODE_PRIVATE);
      HashMap<String,String> coisas = (HashMap<String, String>) sPref.getAll();
          StringRequest postRequest = new StringRequest(Request.Method.POST,URL_LOGIN,new Response.Listener<String>(){
              @Override
              public void onResponse(String responseString) {
              }

          }, new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                  volleyCallback.onFail(error);
              }

          }){
              @Override
              protected Map<String, String> getParams() throws AuthFailureError {
                  HashMap<String,String> params = new HashMap<>();
                  params.put("username",username);
                  params.put("password",password);
                  return params;
              }

              @Override
              protected void deliverResponse(String response) {
                  try {
                       JSONObject loginInfo = new JSONObject(response);
                      volleyCallback.onSucess(loginInfo);
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }

              }

          };
          queue.add(postRequest);

  }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        VolleyRequest.token = token;
    }

    public static String getUrlPlug() {
        return URL_PLUG;
    }

    public static void setUrlPlug(String urlPlug) {
        URL_PLUG = urlPlug;
    }

    public static void sendViagemLogAndDeslocacaoLog(int viagemId,int deslocacaoId){

    }
}
