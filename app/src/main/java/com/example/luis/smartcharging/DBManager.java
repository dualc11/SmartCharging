package com.example.luis.smartcharging;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DBManager {

    private static final String MODULE = "Db Manager";

    private static final String DATABASE_NAME = "myDatabase.sqlite";
    private static final String TABLE_GPS_LOGGER = "GpsLogger";
    private static final String ID="ID";
    private static final String LONGITUDE="longitude";
    private static final String LATITUDE="latitude";
    private static final String ALTITUDE="altitude";
    private static final String DATAEHORA="dataEhora";
    private static final String VIAGEMID="viagemId";

    private static final String CREATE_TABLE_GPS_LOGGER = "CREATE TABLE "+TABLE_GPS_LOGGER+
            " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+LONGITUDE+" DOUBLE ,"+
            LATITUDE+" DOUBLE ," +ALTITUDE+" DOUBLE ,"+
            DATAEHORA+" VARCHAR(45) ,"+ VIAGEMID+" INTEGER "+");";

    private static final String TABLE_VIAGEM_INFO="ViagemInfo";
    private static final String BATERIAINICIAL="bateriaInicial";
    private static final String BATERIAFINAL="bateriaFinal";
    private static final String DISTANCIAKM="distanciaKm";
    private static final String DATA="data";
    private static final String CARROID="carroId";

    private static final String CREATE_TABLE_VIAGEM_INFO="CREATE TABLE "+TABLE_VIAGEM_INFO+
            "("+VIAGEMID+" INTEGER, "+BATERIAINICIAL+" INTEGER, "+BATERIAFINAL+" INTEGER, "+
            DISTANCIAKM+" DOUBLE, "+DATA+" STRING, "+CARROID+" INTEGER, "+ "FOREIGN KEY ("+VIAGEMID+") REFERENCES "+TABLE_GPS_LOGGER+
            "("+VIAGEMID+")"+");";

    private static final String TABLE_REGISTO_DIARIO="RegistoDiario";
    private static final String DISTANCIADIARIA="distanciaDiariaKm";
    private static final String USERID="UserId";

    private static final String CREATE_TABLE_REGISTO="CREATE TABLE "+TABLE_REGISTO_DIARIO+
            "("+DATA+" STRING,"+DISTANCIADIARIA+" DOUBLE, "+USERID+" STRING "+")";

    //Tabela para os carregamentos dos tuc-tucs

    private static final String TABLE_CARREGAMENTOS="TucsCarregar";
    private static final String IDCARREGAMENTO="id";
    private static final String HORAINICIO="horaInicio";
    private static final String HORAFIM="horaFinal";
    private static final String TUCID="tucId";
    private static final String TOMADAID="tomadaId";

    private static final String CRATE_TABLE_CARREGAMENTO="CREATE TABLE "+TABLE_CARREGAMENTOS+
            "("+IDCARREGAMENTO+" INTEGER PRIMARY KEY AUTOINCREMENT, "+HORAINICIO+" STRING, "
            +HORAFIM+" STRING, "+TUCID+" INTEGER, "+TOMADAID+" INTEGER, "+USERID+" STRING, "+
            BATERIAINICIAL+" INTEGER, "+BATERIAFINAL+" INTEGER"+")";

    private static SQLiteDatabase db;
    private static JSONObject jsonObj;
    private static JSONArray jsonArray;
    private static Marker marker;

    public DBManager() {

        try {
            if (db == null)
                db = SQLiteDatabase.openDatabase(
                        Environment.getExternalStorageDirectory() + "/SmartCharging/" + DATABASE_NAME,
                        null,
                        0);
            //Log.i(MODULE,"Db openned");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class DBHolder {
        public static final DBManager INSTANCE = new DBManager();
    }

    public static DBManager getDBManager() {

        return DBHolder.INSTANCE;
    }

    public static boolean databaseExists() {

        File storage_file = new File(Environment.getExternalStorageDirectory(), "/SmartCharging/" + DATABASE_NAME);

        return storage_file.exists();
    }

    public static void initDatabase() {
        try {
            File db_storage_file = new File(Environment.getExternalStorageDirectory(), "/SmartCharging");
            if (!db_storage_file.exists()) {
                if (!db_storage_file.mkdir()) {
                    Log.e(MODULE, "error creating /SmartCharging directory");
                    return;
                }
                db_storage_file = new File(db_storage_file, DATABASE_NAME);
            }

            if (!db_storage_file.exists()) {
                db_storage_file.createNewFile();
                createDatabase(db_storage_file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDatabase(String path) {
        //Log.i(MODULE, "Creating new database");
        db = SQLiteDatabase.openDatabase(
                path,
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY);
        // create anchor events table
        db.execSQL(CREATE_TABLE_GPS_LOGGER);
        db.execSQL(CREATE_TABLE_VIAGEM_INFO);
        db.execSQL(CREATE_TABLE_REGISTO);
        db.execSQL(CRATE_TABLE_CARREGAMENTO);
    }

    public synchronized boolean insertData(double longitude, double latitude,double altitude, String dataEhora,int viagemId)
    {

        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABLE_GPS_LOGGER+"("+LONGITUDE+","+LATITUDE+","+ALTITUDE+","+DATAEHORA+","+VIAGEMID+")" +
                    "VALUES("+longitude+","+latitude+","+altitude+",'"+dataEhora+"',"+viagemId+")";
            Log.i("sdf",sql);
            stm = db.compileStatement(sql);
            Log.i("sdf",sql);
            if (stm.executeInsert() <= 0)
            {
                Log.i(MODULE, "Failed insertion of appliance into database");
            }
            res = true;

            db.setTransactionSuccessful();
        } catch (Exception e) {
            res=false;
            e.printStackTrace();
        } finally	{
            stm.close();
            db.endTransaction();
            Log.d(MODULE, "new appliance data inserted");

        }

        return true;
    }

    public static synchronized int getIdViagemAnterior()
    {
        Cursor c;

        c=db.rawQuery("SELECT MAX("+VIAGEMID+") FROM "+TABLE_GPS_LOGGER,null);
        int idViagemAnterior=0;
        while(c.moveToNext())
        {
            idViagemAnterior = c.getInt(0); //0 é o índice da coluna
        }
        return idViagemAnterior;
    }

    public static synchronized  double calculaKmViagem(int idViagem) throws JSONException {
        Cursor c;

        c=db.rawQuery("SELECT "+LONGITUDE+","+LATITUDE+","+ALTITUDE+","+DATAEHORA+
                " FROM "+TABLE_GPS_LOGGER+" WHERE "+VIAGEMID+"="+idViagem,null);

        double longAnterior=0,latAnterior=0,longSeguinte=0,latSeguinte=0;
        double kmTotalViagem=0;
        int i=0;

        jsonObj=new JSONObject();
        jsonArray=new JSONArray();

        while(c.moveToNext())
        {
            //Dados para mandar para o servidor
            jsonObj.put("longitude",Double.toString(c.getDouble(0)));
            jsonObj.put("latitude",Double.toString(c.getDouble(1)));
            jsonObj.put("altitude",Double.toString(c.getDouble(2)));
            jsonObj.put("dataEhora",c.getString(3));
            jsonArray.put(jsonObj);

            if(i==0)
            {
                longAnterior = c.getDouble(0);
                latAnterior=c.getDouble(1);
                i++;
            }
            else
            {
                longSeguinte=c.getDouble(0);
                latSeguinte=c.getDouble(1);
                kmTotalViagem+=GpsService.getDistanceFromLatLonInKm(latAnterior,longAnterior,latSeguinte,longSeguinte);
                longAnterior=longSeguinte;
                latAnterior=latSeguinte;
            }
        }
        return kmTotalViagem;
    }

    /*Método para quando é iniciada uma viagem guardar o id dessa viagem que se vai iniciar
    * e também guardar a percentagem de bateria antes da viagem começar*/
    public synchronized boolean insertViagemIdBateriaInicialData(int idViagem,int bateriaInicial,int carroId)
    {
        String data=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABLE_VIAGEM_INFO+"("+VIAGEMID+","+BATERIAINICIAL+","+DATA+","+CARROID+")"+
                    " VALUES("+idViagem+","+bateriaInicial+",'"+data+"',"+carroId+")";

            stm = db.compileStatement(sql);
            Log.i("sdfsd",sql);
            if (stm.executeInsert() <= 0)
            {
                Log.i(MODULE, "Failed insertion of appliance into database");
            }
            res = true;

            db.setTransactionSuccessful();
        } catch (Exception e) {
            res=false;
            e.printStackTrace();
        } finally	{
            stm.close();
            db.endTransaction();
            Log.d(MODULE, "new appliance data inserted");

        }
        return true;
    }

    /*Método para após a viagem terminar guardar os kms correspondentes a essa distância e
    * a percentagem de bateria final*/
    public boolean updateKmBateriaFinal(double kmViagem,int bateriaFinal,int viagemId)
    {
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="UPDATE "+TABLE_VIAGEM_INFO+" SET "+BATERIAFINAL+"="+bateriaFinal+","
                    +DISTANCIAKM+"="+kmViagem+" WHERE "+VIAGEMID+"="+viagemId;
            Log.i("query: ",sql);

            stm = db.compileStatement(sql);

            if (stm.executeInsert() <= 0)
            {
                Log.i(MODULE, "Failed insertion of appliance into database");
            }
            res = true;

            db.setTransactionSuccessful();
        } catch (Exception e) {
            res=false;
            e.printStackTrace();
        } finally	{
            stm.close();
            db.endTransaction();
            Log.d(MODULE, "new appliance data inserted");

        }
        return true;
    }

    public synchronized boolean apagaInfoViagem(int viagemId)
    {
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="DELETE FROM "+TABLE_VIAGEM_INFO+" WHERE "+VIAGEMID+"="+viagemId;
            Log.i("query: ",sql);

            stm = db.compileStatement(sql);

            if (stm.executeInsert() <= 0)
            {
                Log.i(MODULE, "Failed insertion of appliance into database");
            }
            res = true;

            db.setTransactionSuccessful();
        } catch (Exception e) {
            res=false;
            e.printStackTrace();
        } finally	{
            stm.close();
            db.endTransaction();
            Log.d(MODULE, "new appliance data inserted");

        }
        return true;
    }

    /*Método para calcular os kms feitos por um condutor durante o dia*/
    public synchronized boolean calculaKmTotaisDiariosCond()
    {
        String dataActual;
        dataActual= new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        Cursor c1;
        c1=db.rawQuery("SELECT "+DATA+" FROM "+TABLE_REGISTO_DIARIO+" WHERE "+DATA+"='"+dataActual+"'",null);

        //Caso ainda não haja um registo diário para o dia actual
        if(!(c1.moveToFirst()) || c1.getCount()==0)
        {
            Cursor c;
            c = db.rawQuery("SELECT SUM("+DISTANCIAKM+") FROM "+TABLE_VIAGEM_INFO+" WHERE "+DATA+"='"+dataActual+"'", null);

            double distanciaDiaria =0;
            Log.i("ssdf",Integer.toString(c.getCount()));

            if(c.getCount()>0) {
                c.moveToPosition(c.getCount() - 1);
                distanciaDiaria = c.getDouble(0);
            }

            SQLiteStatement stm = null;
            boolean res=false;
            db.beginTransaction();

            try{
                String sql = "INSERT INTO "+TABLE_REGISTO_DIARIO+"("+DATA+","+DISTANCIADIARIA+","+
                USERID+")"+
                        "VALUES('"+dataActual+"',"+distanciaDiaria+",'"+ MyTukxis.getUserId()+"')";

                stm = db.compileStatement(sql);

                if (stm.executeInsert() <= 0)
                {
                    Log.i(MODULE, "Failed insertion of appliance into database");
                }
                res = true;

                db.setTransactionSuccessful();
            } catch (Exception e) {
                res=false;
                e.printStackTrace();
            } finally	{
                stm.close();
                db.endTransaction();
                Log.d(MODULE, "new appliance data inserted");

            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized boolean colocaTucCarregar(int bateriaInicio,int tucId,int tomadaId,String userId)
    {
        String horaInicio=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABLE_CARREGAMENTOS+"("+HORAINICIO+","+BATERIAINICIAL+","+TUCID+","+TOMADAID+","+USERID+")"+
                    " VALUES('"+horaInicio+"',"+bateriaInicio+","+tucId+","+tomadaId+", '"+userId+"')";

            stm = db.compileStatement(sql);

            if (stm.executeInsert() <= 0)
            {
                Log.i(MODULE, "Failed insertion of appliance into database");
            }
            res = true;

            db.setTransactionSuccessful();
        } catch (Exception e) {
            res=false;
            e.printStackTrace();
        } finally	{
            stm.close();
            db.endTransaction();
            Log.d(MODULE, "new appliance data inserted");

        }
        return true;
    }

    public synchronized boolean atualizaInfoCarregamento(int bateriaFim, int tucId,int tomadaId)
    {
        String horaFim=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="UPDATE "+TABLE_CARREGAMENTOS+" SET "+HORAFIM+"='"+horaFim+"',"+BATERIAFINAL+"="+bateriaFim+
                    " WHERE "+TUCID+"="+tucId+
                    " AND "+TOMADAID+"="+tomadaId+" AND "+HORAFIM+" IS NULL";
            stm = db.compileStatement(sql);

            if (stm.executeUpdateDelete() <= 0)
            {
                res=false;
            }
            else
            {
            res = true;}

            db.setTransactionSuccessful();
        } catch (Exception e) {
            res=false;
            e.printStackTrace();
        } finally	{
            stm.close();
            db.endTransaction();
            //Log.d(MODULE, "new appliance data inserted");

        }
        if(res){return true;}else{return false;}
        //return true;
    }

    public static synchronized ArrayList<DadosCharging> tucsEmCarregamento() {
        ArrayList<DadosCharging> carrosCarregar = new ArrayList<DadosCharging>();
        Cursor c;

        c = db.rawQuery("SELECT " + HORAINICIO + "," + TUCID + "," + TOMADAID + " FROM "
                + TABLE_CARREGAMENTOS + " WHERE " + HORAFIM + " IS NULL", null);

        while (c.moveToNext()) {
            String horaInicio = c.getString(0);
            String carId = Integer.toString(c.getInt(1));
            String tomadaId = Integer.toString(c.getInt(2));
            String tempoEstimado = "2h";
            int bateriaEstimada = 7;

            DadosCharging dados = new DadosCharging(horaInicio, carId, tomadaId, tempoEstimado, bateriaEstimada);
            carrosCarregar.add(dados);
        }
        return carrosCarregar;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static synchronized ArrayList<RegisterInfo> getRegister(int nDias)
    {
        ArrayList<RegisterInfo> registerInfo=new ArrayList<RegisterInfo>();
        Cursor c;
        boolean temRegistos;
        RegisterInfo register=new RegisterInfo("Nr Carro","Nr Viagem","Km");
        registerInfo.add(register);

        for(int nrDias=0;nrDias<nDias;nrDias++) {
            String dataViagem = convertData(nrDias);
            temRegistos=false;
            register=new RegisterInfo("",dataViagem,"");
            registerInfo.add(register);

            c = db.rawQuery("SELECT " + VIAGEMID + "," + DISTANCIAKM + "," + CARROID +
                    " FROM " + TABLE_VIAGEM_INFO + " WHERE " + DATA + "='" + dataViagem + "'", null);
            double kmsTotais = 0;

            while (c.moveToNext())
            {
                kmsTotais += c.getDouble(1);
                String viagemId = Integer.toString(c.getInt(0));
                String distancia = Double.toString(c.getDouble(1));
                String carroId = Integer.toString(c.getInt(2));

                register=new RegisterInfo(carroId,viagemId,distancia);
                registerInfo.add(register);
                temRegistos=true;
            }
            if(!temRegistos)
            {
                register=new RegisterInfo("","Sem registos","");
                registerInfo.add(register);
            }
            register=new RegisterInfo("Kms totais diários: "+kmsTotais,"","");
            registerInfo.add(register);
            if(nDias-nrDias>1) {
                register = new RegisterInfo("", "", "");
                registerInfo.add(register);
            }
        }
        return registerInfo;
    }

    public static String convertData(int i)
    {
        String dataViagem = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        String dia=dataViagem.substring(8,10);
        int day=Integer.parseInt(dia);
        day=day-i;
        String newDia="";
        if(day/10==0) {
            newDia = "0" + Integer.toString(day);
        }
        else
        {
            newDia=Integer.toString(day);
        }

        String data=dataViagem.substring(0,8);
        data+=newDia;
        return data;

    }

    public static synchronized boolean preencheMapa(GoogleMap map)
    {
        Cursor c;
        //Marker marker=null;
        double longAtual=0,latAtual=0,longAntiga=0,latAntiga=0;
        int i=0;
        boolean temResultados=false;

        c = db.rawQuery("SELECT " + LONGITUDE +","+LATITUDE+" FROM "+TABLE_GPS_LOGGER+
                " WHERE " +VIAGEMID+"="+GpsService.getIdViagem(), null);

        while (c.moveToNext())
        {
            if(map!=null)
            {
                longAntiga=longAtual;
                latAntiga=latAtual;
                longAtual=c.getDouble(0);
                latAtual=c.getDouble(1);
                if(longAtual!=0 && latAtual!=0) {
                    // Add a marker in Sydney and move the camera
                    LatLng coordenadas = new LatLng(latAtual, longAtual);

                    if(marker!=null)
                    {
                        marker.remove();
                    }
                    marker=map.addMarker(new MarkerOptions().position(coordenadas).title("Madeira"));

                    if(i==0) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 17));
                        //i++;
                    }
                    if(i>=1) {
                        Polyline line = map.addPolyline(new PolylineOptions()
                                .add(new LatLng(latAntiga, longAntiga), new LatLng(latAtual, longAtual))
                                .width(5)
                                .color(Color.RED));
                    }
                    i++;
                }
                temResultados=true;
            }
        }
        return temResultados;
    }

    public static JSONArray getJsonArray(){return jsonArray;}
    public static Marker getMarker(){return marker;}
}
