package com.example.luis.smartcharging;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GpsService extends Service implements LocationListener {

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
    private double longitude, latitude, altitude, longitudeAnterior;
    private final Handler handler = new Handler();

    private Random rand = new Random();
    private int bateriaInicial, bateriaFinal;

    private boolean guardouAlgumaCoordenada;

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

                        boolean verif = false;
                        //Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
                        //Caso a a longitude actual seja diferente da anterior e o gps já tenha captado alguma
                        //localização
                        if (data != null && longitude != longitudeAnterior) {
                            verif = db.insertData(longitude, latitude, altitude, data, viagemId);
                            longitudeAnterior = longitude;//Guardamos o valor da longitude anterior

                            if (verif) {
                                guardouAlgumaCoordenada = true;
                                Toast.makeText(getApplicationContext(), "Dados guardados " + data, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        };
    }

    /*Passar de coordenadas para graus*/

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

    //O serviço chama este método quando outro componente da aplicação inicia o serviço chamando o
    //método StartAndStopService() iniciando o serviço em segundo plano indefinidamente até ser chamado o
    //método stopService() (chamado por outro componente) ou stopSelf() (interrompido pelo próprio serviço)..
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Serviço iniciado.", Toast.LENGTH_LONG).show();
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

    //O sistema chama este método antes de chamar onStartCommand ou onBind para operações de configuração
    //este método é chamado apenas uma vez antes do serviço se iniciar.
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        super.onCreate();
        db = StartAndStopService.getDb();

        viagemId = DBManager.getIdViagemAnterior();
        viagemId++;
        guardouAlgumaCoordenada = false;
        //Mudei aqui
        bateriaInicial = rand.nextInt(11);
        db.insertViagemIdBateriaInicialData(viagemId, bateriaInicial, StartAndStopService.getIdCarro());//Passar idCarro também

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
        startTimer();
    }

    //Este método é usado para destruir o serviço.
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //Mudei aqui
        if(guardouAlgumaCoordenada) //Caso durante a viagem tenha sido guardada alguma coordenada.
        {
            bateriaFinal = rand.nextInt(11);
            while (bateriaFinal >= bateriaInicial) {
                bateriaFinal = rand.nextInt(11);
            }
            double kmViagem = 0;

            try {
                kmViagem = DBManager.calculaKmViagem(viagemId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            db.updateKmBateriaFinal(kmViagem, bateriaFinal, viagemId);

            //Enviar dados da viagem para o servidor
            String idCarro=Integer.toString(StartAndStopService.getIdCarro());
            String idDriver=StartAndStopService.getUserId();
            String batInicial=Integer.toString(bateriaInicial);
            String batFinal=Integer.toString(bateriaFinal);
            String kmsViagem=Double.toString(kmViagem);

            VolleyRequest.postRequest(idCarro,idDriver,batInicial,batFinal,kmsViagem,DBManager.getJsonArray());
            //DEBUG
            Log.i("idCarro",idCarro);
            Log.i("idDriver",idDriver);
            Log.i("batInicial",batInicial);
            Log.i("batFinal",batFinal);
            Log.i("kmsViagem",kmsViagem);
            Log.i("JSON", DBManager.getJsonArray().toString());
        }
        else //Caso a viagem não tenha registado nenhuma coordenada apagamos o registo da outra tabela
        // ViagemInfo pois não faz sentido ter
        {
            //Apagar o resgisto actual que não faz sentido estar
            db.apagaInfoViagem(viagemId);
        }

        stopSelf();
        Toast.makeText(this,"Serviço parado", Toast.LENGTH_LONG).show();
        servicoIniciado=false;
        stoptimertask();
        locationManager.removeUpdates(this);
    }

    //Implementação da interface LocationService

    /**
     * Monitor for location changes
     * @param location holds the new location
     */
    @Override
    public void onLocationChanged(Location location)
    {
        data= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        longitude=location.getLongitude();
        latitude=location.getLatitude();
        altitude=location.getAltitude();
    }

    /**
     * GPS turned off, stop watching for updates.
     * @param provider contains data on which provider was disabled
     */
    @Override
    public void onProviderDisabled(String provider)
    {
        Toast.makeText(this, "Desligou GPS, ligue por favor", Toast.LENGTH_LONG).show();
    }

    /**
     * GPS turned back on, re-enable monitoring
     * @param provider contains data on which provider was enabled
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider)
    {
        Toast.makeText(this, "Ligou GPS", Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    public static int getIdViagem()
    {
        return viagemId;
    }
    public static boolean getServicoIniciado()
    {
        return servicoIniciado;
    }
}
