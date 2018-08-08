package com.example.luis.smartcharging;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

import com.example.luis.sampledata.LogInfo;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DBManager {
    private static int newVersion = 5;

    private  static final int TYPE_VIAGEM = 1;
    private  static final int TYPE_DESLOCACAO = 2;

    private static final String MODULE = "Db Manager";
    private static final String DROPTABLE = "DROP TABLE IF EXISTS ";
    private static final String DATABASE_NAME = "myDatabase.sqlite";
    private static final String TABLE_GPS_LOGGER = "GpsLogger";
    private static final String ID="ID";
    private static final String LONGITUDE="longitude";
    private static final String LATITUDE="latitude";
    private static final String ALTITUDE="altitude";
    private static final String DATAEHORA="dataEhora";
    private static final String VIAGEMID="viagemId";
    private static final String UTILIZACAOID = "utilizacaoId";

    /**
     * TABELA UTILIZAÇÃO CARRO
     */
    private static final String TABELA_UTILIZACAO_CARRO = "utilizacao_carro";
    private static final String ID_UTILIZACAO_CARRO = "id";
    private static final String BATERIAINICIAL_UTILIZACAO_CARRO="bateriaInicial";
    private static final String BATERIAFINAL_UTILIZACAO_CARRO="bateriaFinal";
    private static final String DISTANCIAKM_UTILIZACAO_CARRO="distanciaKm";
    private static final String DATA_UTILIZACAO_CARRO="data";
    private static final String CARROID_UTILIZACAO_CARRO="carroId";

    private static final String CREATE_TABELA_UTILIZACAO_CARRO="CREATE TABLE "+TABELA_UTILIZACAO_CARRO+
            "("+ID_UTILIZACAO_CARRO+" INTEGER PRIMARY KEY AUTOINCREMENT, "+BATERIAINICIAL_UTILIZACAO_CARRO+" INTEGER, "+BATERIAFINAL_UTILIZACAO_CARRO+" INTEGER, "+
            DISTANCIAKM_UTILIZACAO_CARRO+" DOUBLE, "+DATA_UTILIZACAO_CARRO+" STRING, "+CARROID_UTILIZACAO_CARRO+" INTEGER);";

    private static final String TABLE_VIAGEM_INFO="ViagemInfo";
    private static final String BATERIAINICIAL="bateriaInicial";
    private static final String BATERIAFINAL="bateriaFinal";
    private static final String DISTANCIAKM="distanciaKm";
    private static final String DATA = "data";
    private static final String VIAGEM_ID = "id";
    private static final String CARROID="carroId";
    private static final String UTILIZAO_ID_VIAGEM = "utilizacaoId";
    private static final String CREATE_TABLE_VIAGEM_INFO="CREATE TABLE "+TABLE_VIAGEM_INFO+
            "("+VIAGEM_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+BATERIAINICIAL+" INTEGER, "+BATERIAFINAL+" INTEGER, "+
            DISTANCIAKM+" DOUBLE, "+DATA+" STRING, "+CARROID+" INTEGER, "+
            UTILIZACAOID+" INTEGER,"+
            "FOREIGN KEY ("+UTILIZACAOID+") REFERENCES "+TABELA_UTILIZACAO_CARRO+
            "("+ID_UTILIZACAO_CARRO+")"+");";
    private static final String GPS_LOGGER_ID = "id";
    private static final String CREATE_TABLE_GPS_LOGGER = "CREATE TABLE "+TABLE_GPS_LOGGER+
            " ("+GPS_LOGGER_ID+" INTEGER  PRIMARY KEY AUTOINCREMENT, "+LONGITUDE+" DOUBLE ,"+
            LATITUDE+" DOUBLE ," +ALTITUDE+" DOUBLE ,"+
            DATAEHORA+" VARCHAR(45),"+UTILIZACAOID+" INTEGER NOT NULL, "+
            " FOREIGN KEY("+UTILIZACAOID+") REFERENCES "+TABELA_UTILIZACAO_CARRO+"("+ID_UTILIZACAO_CARRO+"))";

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
    /**
     * Tabela "Carros"
     */

    private static final String TABELA_CARROS = "carros";
    private static final String ID_CARRO = "id";
    private static final String NUMERO_CARRO = "numeroCarro";
    private static final String CREATE_TABLE_CARROS ="CREATE TABLE "+TABELA_CARROS+
            "("+ID_CARRO+" INTEGER PRIMARY KEY , "+NUMERO_CARRO+")";
    /**
     * Tabela "Plug"
     */

    private static final String TABELA_PLUG = "plug";
    private static final String ID_PLUG = "id";
    private static final String NUMBER_PLUG = "number";
    private static final String CREATE_TABLE_PLUG = "CREATE TABLE "+TABELA_PLUG+"" +
            "("+ID_PLUG+" INTEGER PRIMARY KEY ,"+NUMBER_PLUG+" INTEGER)";


    /**
     * TABELA DESTINOS
     */
    private static final String TABELA_PERCURSO = "percurso";
    private static final String ID_PERCURSO  = "id";
    private static final String ORIGEM_PERCURSO = "origem";
    private static final String DESTINO_PERCURSO= "destino";
    private static final String DISTANCIA_PERCURSO = "distnacia";
    private static final String CREATE_TABLE_PERCURSO ="CREATE TABLE "+TABELA_PERCURSO+
            "( "+ID_PERCURSO+" INTEGER PRIMARY KEY,"+ORIGEM_PERCURSO+" STRING, "+DESTINO_PERCURSO+" STRING," +
            DISTANCIA_PERCURSO +" DOUBLE)";
    /**
     * TABELA LOG DE VIAGEM
     */
    private static final String TABELA_LOG_VIAGEM = "logViagem";
    private static final String ID_LOG_VIAGEM = "id";
    private static final String LONGITUDE_LOG_VIAGEM = "longitude";
    private static final String LATITUDE_LOG_VIAGEM= "latitude";
    private static final String DATAEHORA_LOG_VIAGEM = "dataEhora";
    private static final String VIAGEMID_LOG_VIAGEM = "viagemId";
    private static final String ALTITUDE_LOG_VIAGEM= "altitude";
    private static final String CREATE_TABLE_LOG_VIAGEM = "CREATE TABLE "+ TABELA_LOG_VIAGEM+" ( "+ID_LOG_VIAGEM+" INTEGER PRIMARY KEY AUTOINCREMENT " +
            ", "+LONGITUDE_LOG_VIAGEM +" DOUBLE , "+LATITUDE_LOG_VIAGEM+" DOUBLE ,"+DATAEHORA_LOG_VIAGEM +" VARCHAR(45), " +
            " "+ALTITUDE_LOG_VIAGEM+" DOUBLE ,"+
            " "+VIAGEMID_LOG_VIAGEM+" INTEGER NOT NULL, FOREIGN KEY(`"+ VIAGEMID_LOG_VIAGEM+"`) REFERENCES `"+CREATE_TABLE_VIAGEM_INFO+"`(`"+VIAGEM_ID+"`))";

    /**
     *  TABELA LOG DE DESLOCAÇÃO
     */
    private static final String TABLE_DESLOCACAO="deslocacao";
    private static final String BATERIAINICIAL_DESLOCACAO="bateriaInicial";
    private static final String BATERIAFINAL_DESLOCACAO="bateriaFinal";
    private static final String DISTANCIAKM_DESLOCACAO="distanciaKm";
    private static final String DATA_DESLOCACAO="data";
    private static final String ID_DESLOCACAO = "id";
    private static final String CARROID_DESLOCACAO="carroId";
    private static final String UTILIZAO_ID_DESLOCACAO= "utilizacaoId";
    private static final String CREATE_TABLE_DESLOCACAO="CREATE TABLE "+TABLE_DESLOCACAO+
            "("+ID_DESLOCACAO+" INTEGER PRIMARY KEY AUTOINCREMENT, "+BATERIAINICIAL_DESLOCACAO+" INTEGER, "+BATERIAFINAL_DESLOCACAO+" INTEGER, "+
            DISTANCIAKM_DESLOCACAO+" DOUBLE, "+DATA_DESLOCACAO+" STRING, "+CARROID_DESLOCACAO+" INTEGER, "+
            UTILIZAO_ID_DESLOCACAO+" INTEGER,"+
            "FOREIGN KEY ("+UTILIZACAOID+") REFERENCES "+TABELA_UTILIZACAO_CARRO+
            "("+ID_UTILIZACAO_CARRO+")"+");";

    /**
     * TABELA LOG DE DESLOCAÇÃO
     */
    private static final String TABELA_LOG_DESLOCACAO = "logDeslocacao";
    private static final String ID_LOG_DESLOCACAO = "id";
    private static final String LONGITUDE_LOG_DESLOCACAO = "longitude";
    private static final String LATITUDE_LOG_DESLOCACAO= "latitude";
    private static final String ALTITUDE_LOG_DESLOCACAO= "altitude";
    private static final String DATAEHORA_LOG_DESLOCACAO = "dataEhora";
    private static final String DESLOCACAOID_LOG_DESLOCACAO = "deslocacaoId";

    private static final String CREATE_TABLE_LOG_DESLOCACAO = "CREATE TABLE "+ TABELA_LOG_DESLOCACAO+" ( "+ID_LOG_DESLOCACAO+" INTEGER PRIMARY KEY AUTOINCREMENT " +
            ", "+LONGITUDE_LOG_DESLOCACAO +" DOUBLE , "+LATITUDE_LOG_DESLOCACAO+" DOUBLE ," +
            " "+ALTITUDE_LOG_DESLOCACAO +" DOUBLE ,"+DATAEHORA_LOG_DESLOCACAO +" VARCHAR(45), " +
            " "+DESLOCACAOID_LOG_DESLOCACAO+" INTEGER NOT NULL, FOREIGN KEY(`"+ DESLOCACAOID_LOG_DESLOCACAO+"`) REFERENCES `"+TABLE_DESLOCACAO+"`(`"+ID_DESLOCACAO+"`))";



    private static SQLiteDatabase db;
    private static JSONObject jsonObj;
    private static JSONArray jsonArray;
    private static Marker marker;
    /**
     * Jsons para a utilização
     */

    private static JSONObject jsonObjUtilizacao;
    private static JSONArray jsonArrayUtilizacao;
    public DBManager() {
        try {
            if (db == null)
                db = SQLiteDatabase.openDatabase(
                        Environment.getExternalStorageDirectory() + "/SmartCharging/" + DATABASE_NAME,
                        null,
                        0);
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
    public static boolean isToUpdateDB(){
        Log.e("updatecoisa",""+db.getVersion());
        return db.getVersion()!= newVersion;
    }
    public static void updateDB(){
        db.execSQL(DROPTABLE+TABLE_VIAGEM_INFO);
        db.execSQL(DROPTABLE+TABELA_UTILIZACAO_CARRO);
        db.execSQL(DROPTABLE+TABLE_REGISTO_DIARIO);
        db.execSQL(DROPTABLE+TABLE_CARREGAMENTOS);
        db.execSQL(DROPTABLE+TABELA_CARROS);
        db.execSQL(DROPTABLE+TABELA_PLUG);
        db.execSQL(DROPTABLE+TABELA_PERCURSO);
        db.execSQL(DROPTABLE+TABLE_GPS_LOGGER);
        db.execSQL(DROPTABLE+TABELA_LOG_VIAGEM);
        db.execSQL(DROPTABLE+TABLE_DESLOCACAO);
        db.execSQL(DROPTABLE+TABELA_LOG_DESLOCACAO);

        initTable();
        Log.e("updateDB","Update Database");
        db.setVersion(newVersion);
    }
    private static void createDatabase(String path) {
        //Log.i(MODULE, "Creating new database");
        db = SQLiteDatabase.openDatabase(
                path,
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY);
        // create anchor events table
        initTable();

    }
    public static void initTable(){
        db.execSQL(CREATE_TABLE_VIAGEM_INFO);
        db.execSQL(CREATE_TABELA_UTILIZACAO_CARRO);
        db.execSQL(CREATE_TABLE_REGISTO);
        db.execSQL(CRATE_TABLE_CARREGAMENTO);
        db.execSQL(CREATE_TABLE_CARROS);
        db.execSQL(CREATE_TABLE_PLUG);
        db.execSQL(CREATE_TABLE_PERCURSO);
        db.execSQL(CREATE_TABLE_GPS_LOGGER);
        db.execSQL(CREATE_TABLE_LOG_VIAGEM);
        db.execSQL(CREATE_TABLE_DESLOCACAO);
        db.execSQL(CREATE_TABLE_LOG_DESLOCACAO);
    }
    public synchronized boolean insertDataDeslocamento(double longitude, double latitude,double altitude, String dataEhora,int deslocacaoId)
    {

        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABELA_LOG_DESLOCACAO+"("+LONGITUDE+","+LATITUDE+","+ALTITUDE+","+DATAEHORA+", "+DESLOCACAOID_LOG_DESLOCACAO+")" +
                    "VALUES("+longitude+","+latitude+","+altitude+",'"+dataEhora+"',"+deslocacaoId+")";
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
    public synchronized boolean insertDataViagem(double longitude, double latitude,double altitude, String dataEhora,int viagemId)
    {

        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABELA_LOG_VIAGEM+"("+LONGITUDE+","+LATITUDE+","+ALTITUDE+","+DATAEHORA+", "+VIAGEMID_LOG_VIAGEM+")" +
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

        c=db.rawQuery("SELECT MAX("+VIAGEM_ID+") FROM "+TABLE_VIAGEM_INFO,null);
        int idViagemAnterior=0;
        while(c.moveToNext())
        {
            idViagemAnterior = c.getInt(0); //0 é o índice da coluna
        }
        return idViagemAnterior;
    }
    public static synchronized  double calculaKmUtilizacao(int idUtilizacao) throws JSONException {
        Cursor c;

        c=db.rawQuery("SELECT "+TABLE_VIAGEM_INFO+"."+DISTANCIAKM+" , "+TABLE_DESLOCACAO+"."+DISTANCIAKM_DESLOCACAO+
                " FROM "+TABLE_VIAGEM_INFO+", "+TABLE_DESLOCACAO+" WHERE ( ("+TABLE_VIAGEM_INFO+"."+UTILIZAO_ID_VIAGEM+"="+idUtilizacao+" OR "
                +TABLE_DESLOCACAO+"."+DISTANCIAKM_DESLOCACAO+" = "+idUtilizacao+") AND "+TABLE_VIAGEM_INFO+"."+UTILIZAO_ID_VIAGEM+" = "+TABLE_DESLOCACAO+"."+UTILIZAO_ID_DESLOCACAO+")",null);

        double longAnterior=0,latAnterior=0,longSeguinte=0,latSeguinte=0;
        double kmTotalUtilizacao=0;
        int i=0;
        double kmVigem,kmDeslocacao;

        while(c.moveToNext())
        {
            kmVigem = c.getDouble(0);
            kmDeslocacao = c.getDouble(1);

            kmTotalUtilizacao += kmTotalUtilizacao +kmVigem + kmDeslocacao;
        }
        return kmTotalUtilizacao;
    }
    public static synchronized  double calculaKmDeslocacao(int deslocacoId) throws JSONException {
        Cursor c;

        c=db.rawQuery("SELECT "+LONGITUDE+","+LATITUDE+","+ALTITUDE+","+DATAEHORA+
                " FROM "+TABELA_LOG_DESLOCACAO+" WHERE "+DESLOCACAOID_LOG_DESLOCACAO+"="+deslocacoId,null);

        double longAnterior=0,latAnterior=0,longSeguinte=0,latSeguinte=0;
        double kmTotalViagem=0;
        int i=0;

        //jsonObjUtilizacao=new JSONObject();
        //jsonArrayUtilizacao=new JSONArray();

        while(c.moveToNext())
        {
            //Dados para mandar para o servidor
           /* jsonObjUtilizacao.put("longitude",Double.toString(c.getDouble(0)));
            jsonObjUtilizacao.put("latitude",Double.toString(c.getDouble(1)));
            jsonObjUtilizacao.put("altitude",Double.toString(c.getDouble(2)));
            jsonObjUtilizacao.put("dataEhora",c.getString(3));
            jsonArrayUtilizacao.put(jsonObjUtilizacao);*/

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
    public static synchronized  double calculaKmViagem(int idViagem) throws JSONException {
        Cursor c;

        c=db.rawQuery("SELECT "+LONGITUDE+","+LATITUDE+","+ALTITUDE+","+DATAEHORA+
                " FROM "+TABELA_LOG_VIAGEM+" WHERE "+VIAGEMID+"="+idViagem,null);

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
    public static synchronized boolean insertUtilizaçãoIdBateriaInicialData(int bateriaInicial,int carroId){
        String data = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        SQLiteStatement stm = null;
        boolean res = false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABELA_UTILIZACAO_CARRO+"("+BATERIAINICIAL_UTILIZACAO_CARRO+","+DATA_UTILIZACAO_CARRO+","+CARROID_UTILIZACAO_CARRO+")"+
                    " VALUES("+bateriaInicial+",'"+data+"',"+carroId+")";

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
    public static synchronized boolean insertDeslocaoIdBateriaInicialData(int deslocacaoId, int bateriaInicial,int carroId){
        String data=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABLE_DESLOCACAO+"("+BATERIAINICIAL_DESLOCACAO+","+DATA_DESLOCACAO+","+CARROID_DESLOCACAO+" , "+UTILIZAO_ID_DESLOCACAO+")"+
                    " VALUES("+bateriaInicial+",'"+data+"',"+carroId+", "+deslocacaoId+")";

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


    public static int getUltimoUlizacaoId(){
        int res = -1;
        db.beginTransaction();
        try {
            String[] Select = {"MAX(" + ID_UTILIZACAO_CARRO + ")"};
            String From = TABELA_UTILIZACAO_CARRO;
            // String   Where =ID+"=="+ID+" AND "+VIAGEMID+" == "+viagemId;
            Cursor maxId = db.query(From, Select, null, null, null, null, null);
            while (maxId.moveToNext()) {
               res = maxId.getInt(0);
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally	{
            db.endTransaction();

        }
        return res;

    }
    public static int getUltimoDeslocacaoId(){
        int res = -1;
        db.beginTransaction();
        try {
            String[] Select = {"MAX(" + ID_DESLOCACAO + ")"};
            String From = TABLE_DESLOCACAO;
            // String   Where =ID+"=="+ID+" AND "+VIAGEMID+" == "+viagemId;
            Cursor maxId = db.query(From, Select, null, null, null, null, null);
            while (maxId.moveToNext()) {
                res = maxId.getInt(0);
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally	{
            db.endTransaction();

        }
        return res;

    }
    public static synchronized boolean insertViagemIdBateriaInicialData(int utilizacaoId,int bateriaInicial,int carroId)
    {
        String data=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="INSERT INTO "+TABLE_VIAGEM_INFO+"("+BATERIAINICIAL+","+DATA+","+CARROID+","+UTILIZAO_ID_VIAGEM+")"+
                    " VALUES("+bateriaInicial+",'"+data+"',"+carroId+" ,"+utilizacaoId+")";

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
    public static boolean updateKmBateriaFinalUtilizacao(double kmUtilizacao,int bateriaFinal,int utilizacaoId){
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="UPDATE "+TABELA_UTILIZACAO_CARRO+" SET "+BATERIAFINAL+"="+bateriaFinal+","
                    +DISTANCIAKM+"="+kmUtilizacao+" WHERE "+ID_UTILIZACAO_CARRO+"="+utilizacaoId;
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

    public static boolean updateKmBateriaFinalViagem(double kmViagem,int bateriaFinal,int viagemId){
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="UPDATE "+TABLE_VIAGEM_INFO+" SET "+BATERIAFINAL+"="+bateriaFinal+","
                    +DISTANCIAKM+"="+kmViagem+" WHERE "+VIAGEM_ID+"="+viagemId;
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
    public static boolean updateKmBateriaFinalDeslocacao(double kmDeslocacao,int bateriaFinal,int deslocacaoId){
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();

        try{

            String sql="";

            sql="UPDATE "+TABLE_DESLOCACAO+" SET "+BATERIAFINAL+"="+bateriaFinal+","
                    +DISTANCIAKM+"="+kmDeslocacao+" WHERE "+ID_DESLOCACAO+"="+deslocacaoId;
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

    public synchronized boolean apagaInfoUtilizacao(int utilizacaoId){
        SQLiteStatement stm = null;
        boolean res=false;
        db.beginTransaction();
        try{

            String sql="";

            sql="DELETE FROM "+TABELA_UTILIZACAO_CARRO+" WHERE "+utilizacaoId+"="+ID_UTILIZACAO_CARRO;
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
        String horaInicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
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
    public static synchronized Carregamento getInfoCarregamento(int bateriaFim, int tucId,int tomadaId){
        Carregamento carregamento = null;
        String[] col = new String[]{IDCARREGAMENTO,HORAINICIO,HORAFIM,TUCID,
                TOMADAID,USERID,BATERIAINICIAL,BATERIAFINAL};
        Cursor cursor = db.query(TABLE_CARREGAMENTOS, col, BATERIAFINAL+" IS NULL AND "+TUCID+" == "+tucId+" AND "+TOMADAID+ " == "+tomadaId,
                null, null, null, null);
        Percurso percurso = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");
        while (cursor.moveToNext()){
            int idCarregamento = cursor.getInt(0);
            String dI = cursor.getString(1);
            String dF = cursor.getString(2);
            Date horaInicio = null,horaFinal = null;
            try {
                horaInicio= simpleDateFormat.parse(dI);
                horaFinal = simpleDateFormat.parse(dF);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int userId =  cursor.getInt(5);
            int batInicial =  cursor.getInt(6);
            carregamento = new Carregamento(idCarregamento,userId,tomadaId,tucId,batInicial,batInicial,horaInicio,horaFinal);
        }
        return carregamento;
    }
    public static synchronized boolean atualizaInfoCarregamento(int bateriaFim, int tucId,int tomadaId)
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
        RegisterInfo register = new RegisterInfo("Nr Carro","Nr Viagem","Km");
        registerInfo.add(register);

        for(int nrDias=0;nrDias<nDias;nrDias++) {
            String dataViagem = convertData(nrDias);
            temRegistos = false;
            register = new RegisterInfo("",dataViagem,"");
            registerInfo.add(register);

            c = db.rawQuery("SELECT " + ID_UTILIZACAO_CARRO + "," + DISTANCIAKM + "," + CARROID +
                    " FROM " + TABELA_UTILIZACAO_CARRO + " WHERE " + DATA + "='" + dataViagem + "'", null);
            double kmsTotais = 0;

            while (c.moveToNext())
            {
                kmsTotais += c.getDouble(1);
                String viagemId = Integer.toString(c.getInt(0));
                String distancia = Double.toString(c.getDouble(1));
                String carroId = Integer.toString(c.getInt(2));

                register = new RegisterInfo(carroId,viagemId,distancia);
                registerInfo.add(register);
                temRegistos=true;
            }
            if(!temRegistos)
            {
                register = new RegisterInfo("","Sem registos","");
                registerInfo.add(register);
            }
            register = new RegisterInfo("Kms totais diários: "+kmsTotais,"","");
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
        else{
            newDia=Integer.toString(day);
        }

        String data=dataViagem.substring(0,8);
        data+=newDia;
        return data;

    }
    public static synchronized LogInfo getViagem(int viagemId){
        ArrayList<GPSLogger> res = new ArrayList<>();
        String[] col = new String[]{"*"};
        Cursor cursor = db.query(TABLE_VIAGEM_INFO, null, VIAGEM_ID+" = '" + viagemId+"'",
                null, null, null, null);
        LogInfo viagem =  null;
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            int batInicial = cursor.getInt(1);
            int batFinal = cursor.getInt(2);
            float distanciaKm = cursor.getFloat(3);
            String data = cursor.getString(4);
            int tucId = cursor.getInt(5);
            int utilizacaoId = cursor.getInt(6);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            Date date = new Date();
            try {
                date = format.parse(data);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viagem = new LogInfo(id,batInicial,batFinal,distanciaKm,date,tucId,utilizacaoId,TYPE_VIAGEM);
        }
        return viagem;
    }

    public static synchronized LogInfo getDeslocacao(int deslocacaoId){
        ArrayList<GPSLogger> res = new ArrayList<>();
        Cursor cursor = db.query(TABLE_DESLOCACAO, null, VIAGEM_ID+" = '" + deslocacaoId+"'",
                null, null, null, null);
        LogInfo deslocacao =  null;
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            int batInicial = cursor.getInt(1);
            int batFinal = cursor.getInt(2);
            float distanciaKm = cursor.getFloat(3);
            String data = cursor.getString(4);
            int tucId = cursor.getInt(5);
            int utilizacaoId = cursor.getInt(6);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            Date date = new Date();
            try {
                date = format.parse(data);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            deslocacao = new LogInfo(id,batInicial,batFinal,distanciaKm,date,tucId,utilizacaoId,TYPE_DESLOCACAO);
        }
        return deslocacao;
    }

    public static synchronized ArrayList<GPSLogger> getLogViagemFromUtilizacao(int utilizacaoId){
        ArrayList<GPSLogger> res = new ArrayList<>();
        String[] col = new String[]{VIAGEM_ID};
        Cursor cursor = db.query(TABLE_VIAGEM_INFO, col, UTILIZACAOID+" = '" + utilizacaoId+"'",
                null, null, null, null);

     while (cursor.moveToNext()){
         ArrayList<GPSLogger> listaLogViagemFromViagem = getLogViagem(cursor.getInt(0));
         res.addAll(listaLogViagemFromViagem);
     }
     return res;
    }

    public static synchronized ArrayList<GPSLogger> getDeslocacaoFromUtilizacao(int utilizacaoId){
        ArrayList<GPSLogger> res = new ArrayList<>();
        String[] col = new String[]{ID_DESLOCACAO};
        Cursor cursor = db.query(TABLE_DESLOCACAO, col, UTILIZACAOID+" = '" + utilizacaoId+"'",
                null, null, null, null);

        while (cursor.moveToNext()){
            ArrayList<GPSLogger> listaLogViagemFromDeslocacao = getLogDeslocacao(cursor.getInt(0));
            res.addAll(listaLogViagemFromDeslocacao);
        }
        return res;
    }

    public static synchronized boolean preencheMapa(GoogleMap map) {
        //Marker marker=null;
        double longAtual = 0, latAtual = 0, longAntiga = 0, latAntiga = 0;
        int i = 0, utilizacaoId = getUltimoUlizacaoId();
        boolean temResultados = false;

        ArrayList<GPSLogger> allLogs = getLogViagemFromUtilizacao(utilizacaoId);
        allLogs.addAll(getDeslocacaoFromUtilizacao(utilizacaoId));
        if (!allLogs.isEmpty()) {
            for (int j = 0; j <allLogs.size(); j++) {
                if (map != null) {
                    longAntiga = longAtual;
                    latAntiga = latAtual;
                    longAtual = allLogs.get(j).getLongitude();
                    latAtual = allLogs.get(j).getLatitude();
                    if (longAtual != 0 && latAtual != 0) {
                        // Add a marker in Sydney and move the camera
                        LatLng coordenadas = new LatLng(latAtual, longAtual);

                        if (marker != null) {
                            marker.remove();
                        }
                        marker = map.addMarker(new MarkerOptions().position(coordenadas).title("Madeira"));

                        if (i == 0) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 17));
                            //i++;
                        }
                        if (i >= 1) {
                            Polyline line = map.addPolyline(new PolylineOptions()
                                    .add(new LatLng(latAntiga, longAntiga), new LatLng(latAtual, longAtual))
                                    .width(5)
                                    .color(Color.RED));
                        }
                        i++;
                    }
                    temResultados = true;
                }
            }
        }
        return temResultados;
    }

    public static JSONArray getJsonArray(){return jsonArray;}
    public static Marker getMarker(){return marker;}

    public static void inserirCarro (int id, int numeroCarro){
        String query = "INSERT INTO "+TABELA_CARROS+"("+ID_CARRO+","+NUMERO_CARRO+")" +
                "SELECT "+id+","+numeroCarro+
                " WHERE NOT EXISTS(SELECT 1 FROM "+TABELA_CARROS+"" +
                " WHERE "+TABELA_CARROS+"."+ID_CARRO+"="+id+")";
        db.execSQL(query);
    }
    public static boolean existeCarro(int id){
        String[] col = new String[]{ID_CARRO};
        String[] where = new String[]{ID_CARRO+"=="+ID_CARRO};
        Cursor cursor = db.query(TABELA_CARROS, col, ID_CARRO+"= '" + id + "'",
                null, null, null, null);

       int size = cursor.getCount();
        cursor.close();
        if (size > 0) {
            return true;
        }
        return false;
    }

    public static boolean existePulg(int id){
        String[] col = new String[]{ID_PLUG};
        String[] where = new String[]{ID_PLUG+"=="+ID_PLUG};
        Cursor cursor = db.query(TABELA_PLUG, col, ID_PLUG+"= '" + id + "'",
                null, null, null, null);

        int size = cursor.getCount();
        cursor.close();
        if (size > 0) {
            return true;
        }
        return false;
    }

    public static void inserirPercurso(int id,String origem,String destino,float distancia){
        String query = "INSERT INTO "+TABELA_PERCURSO+"("+ID_PERCURSO+","+ORIGEM_PERCURSO+","+DESTINO_PERCURSO+","+DISTANCIA_PERCURSO+")" +
                "SELECT "+id+","+origem+","+destino+","+distancia+
                " WHERE NOT EXISTS(SELECT 1 FROM "+TABELA_PERCURSO+"" +
                " WHERE "+TABELA_PERCURSO+"."+ID_PERCURSO+"="+id+")";
        db.execSQL(query);
    }
    public static Percurso getPercurso(int id){
        String[] col = new String[]{ID_PERCURSO,ORIGEM_PERCURSO,DESTINO_PERCURSO,DISTANCIA_PERCURSO};
        String[] where = new String[]{ID_PERCURSO+"=="+ID_PERCURSO};
        Cursor cursor = db.query(TABELA_PERCURSO, col, ID_PERCURSO+"= '" + id + "'",
                null, null, null, null);
        Percurso percurso = null;
        while (cursor.moveToNext()){
            int id_percurso = cursor.getInt(0);
            String origem = cursor.getString(1);
            String destino = cursor.getString(2);
            float distancia = cursor.getFloat(3);
            percurso = new Percurso(id_percurso,origem,destino,distancia);
        }
        return percurso;
    }
    public static void inserirPlug (int id, int numeroPlug){
        String query = "INSERT INTO "+TABELA_PLUG+"("+ID_PLUG+","+NUMBER_PLUG+")" +
                "SELECT "+id+","+numeroPlug+
                " WHERE NOT EXISTS(SELECT 1 FROM "+TABELA_PLUG+"" +
                " WHERE "+TABELA_PLUG+"."+ID_PLUG+"="+id+")";
        db.execSQL(query);
    }
    public static ArrayList<GPSLogger> getLogViagem(int viagemId){
        ArrayList<GPSLogger> listaGpsLoggers = new ArrayList<>();


        String [] Select ={TABELA_LOG_VIAGEM+"."+ID_LOG_VIAGEM,TABELA_LOG_VIAGEM+"."+LONGITUDE,TABELA_LOG_VIAGEM+"."+ALTITUDE,
                TABELA_LOG_VIAGEM+"."+DATAEHORA,TABELA_LOG_VIAGEM+"."+VIAGEMID,
                TABELA_LOG_VIAGEM+"."+LATITUDE_LOG_VIAGEM};
        String From = TABELA_LOG_VIAGEM;
        String   Where = TABELA_LOG_VIAGEM+"."+VIAGEMID_LOG_VIAGEM+"=="+viagemId;
        Cursor clistaGpsLogger = db.query(From,Select,Where,null,null,null,null);
        while (clistaGpsLogger.moveToNext()){
            GPSLogger logViagem = new GPSLogger();


            logViagem.setId(clistaGpsLogger.getInt(0));
            logViagem.setLongitude(clistaGpsLogger.getFloat(1));
            logViagem.setAltitude(clistaGpsLogger.getFloat(2));
            String stringData = clistaGpsLogger.getString(3);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            try {
                Date date = format.parse(stringData);
                logViagem.setData(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            logViagem.setViagemId(clistaGpsLogger.getInt(4));
            logViagem.setLatitude(clistaGpsLogger.getFloat(5));
            listaGpsLoggers.add(logViagem);

        }
        return  listaGpsLoggers;
    }

    public static ArrayList<GPSLogger> getLogDeslocacao(int deslocacaoId){
        ArrayList<GPSLogger> listaGpsLoggers = new ArrayList<>();


        String [] Select ={TABELA_LOG_DESLOCACAO+"."+ID_LOG_DESLOCACAO,TABELA_LOG_DESLOCACAO+"."+LONGITUDE,TABELA_LOG_DESLOCACAO+"."+ALTITUDE,
                TABELA_LOG_DESLOCACAO+"."+DATAEHORA,TABELA_LOG_DESLOCACAO+"."+DESLOCACAOID_LOG_DESLOCACAO,
                TABELA_LOG_DESLOCACAO+"."+LATITUDE_LOG_DESLOCACAO};
        String From = TABELA_LOG_DESLOCACAO;
        String   Where = TABELA_LOG_DESLOCACAO+"."+DESLOCACAOID_LOG_DESLOCACAO+"=="+deslocacaoId;
        Cursor clistaGpsLogger = db.query(From,Select,Where,null,null,null,null);
        while (clistaGpsLogger.moveToNext()){
            GPSLogger logDeslocacao = new GPSLogger();


            logDeslocacao.setId(clistaGpsLogger.getInt(0));
            logDeslocacao.setLongitude(clistaGpsLogger.getFloat(1));
            logDeslocacao.setAltitude(clistaGpsLogger.getFloat(2));
            String stringData = clistaGpsLogger.getString(3);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            try {
                Date date = format.parse(stringData);
                logDeslocacao.setData(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            logDeslocacao.setViagemId(clistaGpsLogger.getInt(4));
            logDeslocacao.setLatitude(clistaGpsLogger.getFloat(5));
            listaGpsLoggers.add(logDeslocacao);

        }
        return  listaGpsLoggers;
    }


    public static String getMODULE() {
        return MODULE;
    }

    public static JSONObject getJsonObjUtilizacao() {
        return jsonObjUtilizacao;
    }

    public static void setJsonObjUtilizacao(JSONObject jsonObjUtilizacao) {
        DBManager.jsonObjUtilizacao = jsonObjUtilizacao;
    }

    public static JSONArray getJsonArrayUtilizacao() {
        return jsonArrayUtilizacao;
    }

    public static void setJsonArrayUtilizacao(JSONArray jsonArrayUtilizacao) {
        DBManager.jsonArrayUtilizacao = jsonArrayUtilizacao;
    }


}
