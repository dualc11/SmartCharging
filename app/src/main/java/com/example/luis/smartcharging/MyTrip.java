package com.example.luis.smartcharging;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MyTrip extends StartAndStopService {

    private Toolbar toolbar;
    private ListView listaMyTrip;
    private String [] myTripInfo;
    private ArrayAdapter<String> listaAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip);

        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.menuicon));
        getSupportActionBar().setTitle("My Trip");
        navigationClick(toolbar);

        listaMyTrip=findViewById(R.id.listaMyTrip);
        myTripInfo =new String [2];
        preencheListaMyTrip();
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
}
