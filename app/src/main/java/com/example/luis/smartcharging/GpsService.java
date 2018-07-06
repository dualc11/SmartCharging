package com.example.luis.smartcharging;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class GpsService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "get location";
    // Location Variables
    private LocationManager locationManager;
    private final static int DISTANCE_UPDATES = 1;
    private final static int TIME_UPDATES = 5;
    private static DBManager db;
    private static boolean servicoIniciado = false;
    private static int viagemId = 0;
    private Timer timer;    //Timer
    private TimerTask timerTask;    //Ação que irá ser desempenhada de x em x tempo.
    private String data;
    private static double longitude, latitude, altitude, longitudeAnterior;
    private final Handler handler = new Handler();
    private int bateriaInicial, bateriaFinal;
    private boolean guardouAlgumaCoordenada;

    //Google api
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private static int tipoLocalizacao = 2;//Guarda o tipo de localização(GPS ou WIFI) GPS=1 WIFI/DADOS=2
    private static final int TIPO_GPS = 1;
    private static final int TIPO_INTERNET = 2;


    //O sistema chama este método antes de chamar onStartCommand ou onBind para operações de configuração
    //este método é chamado apenas uma vez antes do serviço se iniciar.

    @Override
    public void onCreate() {

        super.onCreate();
        db = MyTukxis.getDb();

        viagemId = DBManager.getIdViagemAnterior();
        viagemId++;
        guardouAlgumaCoordenada = false;
        //Mudei aqui
        //bateriaInicial = rand.nextInt(11);
        bateriaInicial = IntroduzirPerBat.getPercentagemBat();
        db.insertViagemIdBateriaInicialData(viagemId, bateriaInicial, MyTukxis.getIdCarro());//Passar idCarro também
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        String versao = Build.VERSION.CODENAME;
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationRequest = new LocationRequest();
                locationRequest.setInterval(10000);
                locationCallback = new LocationCallback() {
                    //Método que é evocado no fusedLocationProviderClient.requestLocationUpdates()
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult.getLastLocation() != null) {
                            guardarLocalização(locationResult.getLastLocation());


                        } else {
                            tipoLocalizacao = TIPO_GPS;
                            fusedLocationProviderClient.removeLocationUpdates(this);
                        }
                    }
                    //Método evocado que verifica se existe wifi ou dados moveis
                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                            if (locationAvailability.isLocationAvailable() == false) {//Se não foi possivel obter localização por Internet
                                tipoLocalizacao = TIPO_GPS;
                            /*if(!isConnectWifiOrMobile(getApplicationContext())){//Verifica se existe INternet
                                Toast.makeText(getApplicationContext(),"Não existe ligação a Internet",Toast.LENGTH_LONG).show();
                                tipoLocalizacao = TIPO_GPS;//Muda a forma de obter localização

                            }*/
                        }
                    }


                };
            } else {
                Log.e("Erro", "Não permissão");
            }
       // }


        startTimer();
    }

    //Este método é usado para destruir o serviço.
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Mudei aqui
        if (guardouAlgumaCoordenada) //Caso durante a viagem tenha sido guardada alguma coordenada.
        {
            bateriaFinal = IntroduzirPerBat.getPercentagemBat();
            double kmViagem = 0;

            try {
                kmViagem = DBManager.calculaKmViagem(viagemId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            db.updateKmBateriaFinal(kmViagem, bateriaFinal, viagemId);

            //Enviar dados da viagem para o servidor
            String idCarro = Integer.toString(MyTukxis.getIdCarro());
            String idDriver = MyTukxis.getUserId();
            String batInicial = Integer.toString(bateriaInicial);
            String batFinal = Integer.toString(bateriaFinal);
            String kmsViagem = Double.toString(kmViagem);

            VolleyRequest.postRequest(idCarro, idDriver, batInicial, batFinal, kmsViagem, DBManager.getJsonArray());

        } else //Caso a viagem não tenha registado nenhuma coordenada apagamos o registo da outra tabela
        // ViagemInfo pois não faz sentido ter
        {
            //Apagar o resgisto actual que não faz sentido estar
            db.apagaInfoViagem(viagemId);
        }

        stopSelf();
        Toast.makeText(this, "Serviço parado", Toast.LENGTH_SHORT).show();
        servicoIniciado = false;
        stoptimertask();
        locationManager.removeUpdates(this);
        fusedLocationProviderClient.removeLocationUpdates(getlocationCallback());
    }

    //O serviço chama este método quando outro componente da aplicação inicia o serviço chamando o
    //método MyTukxis() iniciando o serviço em segundo plano indefinidamente até ser chamado o
    //método stopService() (chamado por outro componente) ou stopSelf() (interrompido pelo próprio serviço)..
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        servicoIniciado = true;
        return super.onStartCommand(intent, flags, startId);
    }

    //O sistema chama este método quando outro componete pretende vincular-se ao serviºo através do
    //método bindService().
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Implementação da interface LocationService

    /**
     * Monitor for location changes
     *
     * @param location holds the new location
     */
    @Override
    public void onLocationChanged(Location location) {

        data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
        Toast.makeText(getApplicationContext(), "IN ON LOCATION CHANGE, lat=" + latitude + ", lon=" + longitude, Toast.LENGTH_SHORT).show();

    }

    /**
     * GPS turned off, stop watching for updates.
     *
     * @param provider contains data on which provider was disabled
     */
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Desligou GPS, ligue por favor", Toast.LENGTH_LONG).show();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(1000);
    }

    /**
     * GPS turned back on, re-enable monitoring
     *
     * @param provider contains data on which provider was enabled
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Ligou GPS, viagem iniciada!", Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    public void startTimer() {
        //Cria o timer
        timer = new Timer();
        //inicializa o trabalho que irá ser desempenhado pelo timerTask.
        initializeTimerTask();
        //o timerTask vai executar-se depois dos primeiros 5000ms e depois a cada 30000ms.
        timer.schedule(timerTask, 5000, 10000); //
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
                         mudarTipoLocalizacao();//Verificar o wifi/dados, e modifica a forma de localização
                         switch (tipoLocalizacao){
                             case TIPO_INTERNET:
                                 if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                     locationManager.removeUpdates(getGpsService());//Para os updates atráves de GPS
                                     fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                                 }
                                 break;
                             case TIPO_GPS:
                                 if (ActivityCompat.checkSelfPermission(getGpsService(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                     fusedLocationProviderClient.removeLocationUpdates(getlocationCallback());//Para os updates atraves da internet
                                     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, getGpsService());
                                 }
                                 break;
                             default:
                                 break;
                         }



                        boolean verif = false;
                        //Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
                        //Caso a a longitude actual seja diferente da anterior e o gps já tenha captado alguma
                        //localização

                        if (data != null && longitude != longitudeAnterior) {
                            verif = db.insertData(longitude, latitude, altitude, data, viagemId);
                            longitudeAnterior = longitude;//Guardamos o valor da longitude anterior
                            if (verif) {
                                String tipo = "GPS";
                                if(tipoLocalizacao == TIPO_INTERNET)
                                    tipo = "INTERNET";
                                Toast.makeText(getApplicationContext(), tipo+": Dados guardados " + data, Toast.LENGTH_SHORT).show();
                                guardouAlgumaCoordenada = true;
                               }
                        } else {
                            String tipo = "GPS";
                            if(tipoLocalizacao == TIPO_INTERNET)
                                tipo = "INTERNET";
                            Toast.makeText(getApplicationContext(), tipo+": Mesmos Dados guardados " + data, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
    }

    /*Passar de coordenadas para kms*/

    public static double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Raio da Terra em km
        double dLat = degToRad(lat2 - lat1);  // Graus para rad
        double dLon = degToRad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distância em km.
        return d;
    }

    //Passar de graus para radianos
    public static double degToRad(double deg) {
        return deg * (Math.PI / 180);
    }

    public static int getIdViagem() {
        return viagemId;
    }

    public static boolean getServicoIniciado() {
        return servicoIniciado;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static double getLatitude() {
        return latitude;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void guardarLocalização(Location location) {
        data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
    }
    public GpsService getGpsService(){
        return this;
    }
    //Método para verificar o estado a ligação a internet para obter a localização
    public boolean isConnectWifiOrMobile(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean estadoWifi = false;
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        if(isWifiConn)
            return true;
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isMobileConn = networkInfo.isConnected();
        if(isMobileConn)
            return true;

        return estadoWifi;

    }

    public void mudarTipoLocalizacao() {
        if (isConnectWifiOrMobile(getApplicationContext())) {
            tipoLocalizacao = TIPO_INTERNET;
        } else {
            tipoLocalizacao = TIPO_GPS;
        }
    }
    public LocationCallback getlocationCallback(){
            return locationCallback;

    }
}
