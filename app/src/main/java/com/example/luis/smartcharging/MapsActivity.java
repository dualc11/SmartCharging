package com.example.luis.smartcharging;

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

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends MyTukxis implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Toolbar toolbar;
    private ListView listaMyTrip;
    private String [] myTripInfo;
    private ArrayAdapter<String> listaAdapter;

    private Timer timer;    //Timer
    private TimerTask timerTask;    //Ação que irá ser desempenhada de x em x tempo.
    private Handler handler=new Handler();
    int i=0;
    private double latAntiga,longAntiga,latAtual,longAtual;
    private Marker marker;

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
        getSupportActionBar().setTitle("My Trip");
        navigationClick(toolbar);

        listaMyTrip=findViewById(R.id.listaMyTrip);
        myTripInfo =new String [2];
        preencheListaMyTrip();

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
                            if(longAtual!=0 && latAtual!=0) {
                                // Add a marker in Sydney and move the camera
                                LatLng coordenadas = new LatLng(latAtual, longAtual);
                                //mMap.clear();
                                if(marker!=null)
                                {
                                    marker.remove();
                                }
                                marker=mMap.addMarker(new MarkerOptions().position(coordenadas).title("Madeira"));
                                if(i==0) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 17));
                                    i++;
                                }
                                if(i>=2) {
                                    Polyline line = mMap.addPolyline(new PolylineOptions()
                                            .add(new LatLng(latAntiga, longAntiga), new LatLng(latAtual, longAtual))
                                            .width(5)
                                            .color(Color.RED));
                                }
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
        myTripInfo[0]="Distance travelled during the tour               15Km"
                +"\n"+"This value is an estimation based on real time data about your location";
        myTripInfo[1]="Battery autonomy          25Km"+"\n"
                +"This value is an estimation based on real time data about your location";

        listaAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,myTripInfo);
        listaMyTrip.setAdapter(listaAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(32.64414499363083, -16.91396713256836);
        marker=mMap.addMarker(new MarkerOptions().position(sydney).title("Madeira"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 7));
    }
}
