package com.example.luis.smartcharging;

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
import com.google.android.gms.maps.model.MarkerOptions;

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
        //toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.menuicon));
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

                            double longitude=GpsService.getLongitude();
                            double latitude=GpsService.getLatitude();
                            if(longitude!=0 && latitude!=0) {
                                // Add a marker in Sydney and move the camera
                                LatLng coordenadas = new LatLng(latitude, longitude);
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(coordenadas).title("Madeira"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(32.64414499363083, -16.91396713256836);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Madeira"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /*public void mudar(View v)
    {
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(32.67749666,-17.07856351);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Madeira"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }*/
}
