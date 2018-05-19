package com.example.luis.smartcharging;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends MyTukxis implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Toolbar toolbar;
    private ListView listaMyTrip;
    private ArrayList<String> myTripInfo;
    private ArrayAdapter<String> listaAdapter;

    private Timer timer;    //Timer
    private TimerTask timerTask;    //Ação que irá ser desempenhada de x em x tempo.
    private Handler handler=new Handler();
    private int i;
    private double latAntiga,longAntiga,latAtual,longAtual;
    private  Marker marker;
    private boolean mapaPreenchido=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        getSupportActionBar().setTitle("Current tour");
        navigationClick(toolbar);

        listaMyTrip=findViewById(R.id.listaMyTrip);
        myTripInfo =new ArrayList<String>();
        preencheListaMyTrip();
        i=0;
        mapaPreenchido=false;
        startTimer();
    }

    public void startTimer() {
        //Cria o timer
        timer = new Timer();
        //inicializa o trabalho que irá ser desempenhado pelo timerTask.
        initializeTimerTask();
        //o timerTask vai executar-se depois dos primeiros 5000ms e depois a cada 30000ms.
        timer.schedule(timerTask, 5000, 5000); //
    }

    public void stoptimertask() {
        //para o timer se este ainda estiver em execução
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //Tarefa que é executada de x em x tempo do timer
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(mMap!=null)
                        {
                            longAntiga=longAtual;
                            latAntiga=latAtual;
                            longAtual=GpsService.getLongitude();
                            latAtual=GpsService.getLatitude();

                            //Caso o GPS já tenha encontrado alguma coordenada
                            if(longAtual!=0 && latAtual!=0)
                            {

                                LatLng coordenadas = new LatLng(latAtual, longAtual);

                                /*Caso o utilizador tenha entrado agora na actividade e já tenha coordenadas
                                na base de dados, foi desenhado o caminho no mapa com essas coordenadas e
                                agora a última coordenada é a do marker que está no mapa e a próxima é a que
                                o sinal de GPS apanhar*/
                                if(i==0 && mapaPreenchido) {
                                    marker=DBManager.getMarker();
                                    LatLng position = marker.getPosition();
                                    latAntiga = position.latitude;
                                    longAntiga = position.longitude;
                                    mapaPreenchido=false;
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 17));
                                }
                                /*Caso não tenha coordenadas da viagem atual na base de dados*/
                                else if(i==0 && !mapaPreenchido){
                                    longAntiga=longAtual;
                                    latAntiga=latAtual;
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 17));
                                }
                                //removemos o marker antigo para colocar o novo
                                if(marker!=null)
                                {
                                    marker.remove();
                                }
                                //Adicionamos o marker ao mapa
                                marker=mMap.addMarker(new MarkerOptions().position(coordenadas).title("Madeira"));

                                    //Desenhamos uma linha entre a coordenada anterior e a atual/nova
                                    Polyline line = mMap.addPolyline(new PolylineOptions()
                                            .add(new LatLng(latAntiga, longAntiga), new LatLng(latAtual, longAtual))
                                            .width(5)
                                            .color(Color.RED));
                                i++;
                            }
                        }
                    }
                });
            }
        };
    }

    public void preencheListaMyTrip()
    {
        myTripInfo.add("Distance travelled during the tour"+
                ","+"This value is an estimation based on real time data about your location"+","+
                " 15Km");

        myTripInfo.add("Battery autonomy"+","
                +"This value is an estimation based on real time data about your location"+","+
                "25Km");

        listaAdapter=new currentTourListAdapter(this,R.layout.current_tour,myTripInfo);
        listaMyTrip.setAdapter(listaAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapaPreenchido= DBManager.preencheMapa(mMap);
    }

    public void terminarViagem(View v) throws JSONException {
        pararServico();
    }


}
