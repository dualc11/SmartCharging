package com.example.luis.smartcharging;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class FragmentToday extends Fragment {

    private ListView listRegister;
    private RegisterListAdapter listAdapter;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_today, container, false);
        listRegister = v.findViewById(R.id.registerList);

        listAdapter = new RegisterListAdapter(this.getContext(), R.layout.register_info, DBManager.getRegister(1));
        listRegister.setAdapter(listAdapter);

        Button button = (Button) v.findViewById(R.id.bUploadKms);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                guardaKmsDiários();
            }
        });

        return v;
    }

    //Este método é usado para guardar o total de kms diários que um determinado condutor fez
    public void guardaKmsDiários()
    {
        if(!GpsService.getServicoIniciado()) {
            if (MyTukxis.getDb().calculaKmTotaisDiariosCond()) {
                Toast.makeText(getContext(), "Registo diário inserido com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "O registo diário já foi inserido anteriormente!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getContext(),"Termine primeiro a viagem",Toast.LENGTH_LONG).show();
        }
    }
}