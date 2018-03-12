package com.example.luis.smartcharging;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyRequest {

    private static final RequestQueue queue=Volley.newRequestQueue(StartAndStopService.getContext());
    private static final String url="http://66.175.221.248:3000/test";
    //private static final String url="http://10.2.0.70:3000/teste";

    public static void getRequest()
    {
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.i("Response", response.toString());
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

                return params;
            }
        };
        queue.add(postRequest);
    }
}
