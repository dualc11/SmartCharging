package com.example.luis.smartcharging;

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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GpsService  extends Service implements LocationListener {

    // Location Variables
    private LocationManager locationManager;
    private final static int DISTANCE_UPDATES = 1;
    private final static int TIME_UPDATES = 5;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private boolean LocationAvailable;

    private static DBManager db;
    private static boolean servicoIniciado = false;
    private static int viagemId=0;

    private Timer timer;    //Timer
    private TimerTask timerTask;    //Ação que irá ser desempenhada de x em x tempo.
    private String data;
    private double longitude,latitude,altitude,longitudeAnterior;
    private final Handler handler = new Handler();

    private Random rand=new Random();
    private int bateriaInicial,bateriaFinal;

    private boolean guardouAlgumaCoordenada;

    public void startTimer() {
        //Cria o timer
        timer = new Timer();
        //inicializa o trabalho que irá ser desempenhado pelo timerTask.
        initializeTimerTask();
        //o timerTask vai executar-se depois dos primeiros 5000ms e depois a cada 30000ms.
        timer.schedule(timerTask, 5000, 10000); //
    }

    public void stoptimertask()
    {
        //para o timer se este ainda estiver em execução
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //Tarefa que é executada de x em x tempo do timer
    public void initializeTimerTask()
    {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        boolean verif=false;
                        //Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
                        //Caso a a longitude actual seja diferente da anterior e o gps já tenha captado alguma
                        //localização
                        if(data!=null && longitude!=longitudeAnterior)
                        {
                            verif = db.insertData(longitude, latitude, altitude, data,viagemId);
                            longitudeAnterior=longitude;//Guardamos o valor da longitude anterior

                            if(verif)
                            {
                                guardouAlgumaCoordenada=true;
                                Toast.makeText(getApplicationContext(), "Dados guardados "+data, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        };
    }

    /*Passar de coordenadas para graus*/

    public static double getDistanceFromLatLonInKm(double lat1,double lon1,double lat2,double lon2)
    {
        int R = 6371; // Raio da Terra em km
        double dLat = degToRad(lat2-lat1);  // Graus para rad
        double dLon = degToRad(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distância em km.
        return d;
    }

    public static double degToRad(double deg)
    {
        return deg * (Math.PI/180);
    }

    //O serviço chama este método quando outro componente da aplicação inicia o serviço chamando o
    //método StartAndStopService() iniciando o serviço em segundo plano indefinidamente até ser chamado o
    //método stopService() (chamado por outro componente) ou stopSelf() (interrompido pelo próprio serviço)..
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
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
    @Override
    public void onCreate() {

        super.onCreate();
        db=StartAndStopService.getDb();

        viagemId=DBManager.getIdViagemAnterior();
        viagemId++;
        guardouAlgumaCoordenada=false;
        //Mudei aqui
        bateriaInicial = rand.nextInt(11);
        db.insertViagemIdBateriaInicialData(viagemId,bateriaInicial,StartAndStopService.getIdCarro());//Passar idCarro também

        LocationAvailable = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkPermission())
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
            startTimer();
        }
        else
        {
            // Toast.makeText(this, "GPS ligado, permissão não concedida", Toast.LENGTH_LONG).show();
            requestPermission();
        }
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
            double kmViagem = DBManager.calculaKmViagem(viagemId);

            db.updateKmBateriaFinal(kmViagem, bateriaFinal, viagemId);
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
        if (checkPermission())
        {
            Toast.makeText(this, "Desligou GPS, ligue por favor", Toast.LENGTH_LONG).show();
        }
        else
        {
            requestPermission();
        }
    }

    /**
     * GPS turned back on, re-enable monitoring
     * @param provider contains data on which provider was enabled
     */
    @Override
    public void onProviderEnabled(String provider)
    {
        if (checkPermission())
        {
            Toast.makeText(this, "Ligou GPS", Toast.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
        }
        else
        {
            requestPermission();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    /**
     * See if we have permission for locations
     *
     * @return boolean, true for good permissions, false means no permission
     */
    private boolean checkPermission()
    {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            LocationAvailable = true;
            return true;
        }
        else
        {
            LocationAvailable = false;
            return false;
        }
    }

    /**
     * Request permissions from the user
     */
    private void requestPermission()
    {
        /**
         * Previous denials will warrant a rationale for the user to help convince them.
         */
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION))
        {
            Toast.makeText(this, "This app relies on location data for it's main functionality. Please enable GPS data to access all features.", Toast.LENGTH_LONG).show();
        }
        else
        {
            ActivityCompat.requestPermissions((Activity) getApplicationContext(),new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Monitor for permission changes.
     *
     * @param requestCode passed via PERMISSION_REQUEST_CODE
     * @param permissions list of permissions requested
     * @param grantResults the result of the permissions requested
     */

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /**
                     * We are good, turn on monitoring
                     */
                    if (checkPermission())
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_UPDATES, DISTANCE_UPDATES, this);
                    }
                    else
                    {
                        requestPermission();
                    }
                }
                else
                {
                    /**
                     * No permissions, block out all activities that require a location to function
                     */
                    Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public static int getIdViagem()
    {
        return viagemId;
    }
    public static boolean getServicoIniciado()
    {
        return servicoIniciado;
    }
    public static DBManager getDbManager(){return db;}
}
