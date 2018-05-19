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
import android.widget.ListView;
import android.widget.TableLayout;

public class FragmentHistory extends Fragment {

    private ListView listRegister;
    private RegisterListAdapter listAdapter;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_history, container, false);
        listRegister=v.findViewById(R.id.registerList);

        listAdapter = new RegisterListAdapter(this.getContext(), R.layout.register_info, DBManager.getRegister(2));
        listRegister.setAdapter(listAdapter);
        return v;
    }

   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listAdapter = new RegisterListAdapter(context, R.layout.register_info, DBManager.getRegister(1));
        listRegister.setAdapter(listAdapter);
    }*/
}
