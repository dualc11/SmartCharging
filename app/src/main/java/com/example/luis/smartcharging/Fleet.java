package com.example.luis.smartcharging;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class Fleet extends MyTukxis {
    private static ListView listaFleet;
    private Toolbar toolbar;
    private ArrayList<DadosFleet> fleets=new ArrayList<>();
    private FleetListAdapter fleetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fleet);
        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("Fleet");

        navigationClick(toolbar);
        listaFleet=findViewById(R.id.listaTucsDisp);
        preencheFleet();

    }

    public void preencheFleet()
    {
        fleets = VolleyRequest.getCarsStatus(new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                //Informação dos dados do carro
                String idTuc;
                int available = -1;
                int charging = -1;
                String bars = "70";
                DadosFleet dadosCarro;

                for (int i = 0;i<result.length();i++) {
                    try {
                        JSONObject carro = result.getJSONObject(i);
                        idTuc = carro.getString("id");
                        if(carro.getString("available").compareTo("null") != 0)
                            available = carro.getInt("available");
                        if(carro.getString("charging").compareTo("null") != 0)
                            charging = carro.getInt("charging");
                        dadosCarro = new DadosFleet(idTuc,available,charging,bars);
                        fleets.add(dadosCarro);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                fleetAdapter = new FleetListAdapter(getContext(),R.layout.dados_fleet,fleets);
                listaFleet.setAdapter(fleetAdapter);
            }

            @Override
            public void onFail(VolleyError error) {
                Toast toast = Toast.makeText(getContext(), "Não foi possível atualizar o feed", Toast.LENGTH_LONG);
                toast.show();
            }
        });


    }
}
