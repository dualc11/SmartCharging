package com.example.luis.smartcharging;

import java.util.Date;

/**
 * Created by claudio on 04-07-2018.
 */

public class GPSLogger {
    int id;
    float longitude;
    float altitude;
    Date data;
    int viagemId;

    public GPSLogger() {
    }

    public GPSLogger(int id, float longitude, float altitude, Date data, int viagemId) {
        this.id = id;
        this.longitude = longitude;
        this.altitude = altitude;
        this.data = data;
        this.viagemId = viagemId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public int getViagemId() {
        return viagemId;
    }

    public void setViagemId(int viagemId) {
        this.viagemId = viagemId;
    }


}
