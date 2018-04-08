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
import android.widget.TableLayout;

public class FragmentHistory extends Fragment {

    private static TableLayout tableLayout;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_history, container, false);
        tableLayout=v.findViewById(R.id.tableLayout);
        DBManager.getRegister(this.getContext(),2,false);
        return v;
    }


    public static TableLayout getTableLayout()
    {
        return tableLayout;
    }
}
