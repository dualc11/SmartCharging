package com.example.luis.smartcharging;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by claudio on 27-06-2018.
 */

interface VolleyCallback {
    void onSuccess(JSONArray result);
    void onSucess(JSONObject result);
    void onFail(VolleyError error);
}
