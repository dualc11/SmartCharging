package com.example.luis.smartcharging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.luis.smartcharging.R;
import com.example.luis.smartcharging.RegisterInfo;

import java.util.ArrayList;

public class RegisterListAdapter extends ArrayAdapter<RegisterInfo> {

    public RegisterListAdapter(Context context, int resource, @NonNull ArrayList<RegisterInfo> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.register_info, parent, false);
        }

        TextView idCarro = (TextView) convertView.findViewById(R.id.idCarro);
        TextView idViagem=(TextView) convertView.findViewById(R.id.idViagem);
        TextView kmsViagem=(TextView) convertView.findViewById(R.id.kmsViagem);

        RegisterInfo registerInfo=getItem(position);

        idCarro.setText(""+registerInfo.getNrCarro());
        idViagem.setText(""+registerInfo.getNrViagem());
        kmsViagem.setText(""+registerInfo.getKmsViagem());

        return convertView;
    }
}
