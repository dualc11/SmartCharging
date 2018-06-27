package com.example.luis.smartcharging;

import com.android.volley.VolleyError;

import org.json.JSONArray;

/**
 * Created by claudio on 27-06-2018.
 */

interface VolleyCallback {
    void onSuccess(JSONArray result);
    void onFail(VolleyError error);
}
