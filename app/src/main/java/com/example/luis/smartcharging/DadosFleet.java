package com.example.luis.smartcharging;

public class DadosFleet {

    private String idTuc;
    private int available;
    private int charging;
    private String bars;

    public DadosFleet(String idTuc,int available, int charging,String bars)
    {
        this.idTuc=idTuc;
        this.available = available;
        this.charging = charging;
        this.bars=bars;
    }

    public String getIdTuc() {
        return idTuc;
    }

    public void setIdTuc(String idTuc) {
        this.idTuc = idTuc;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getCharging() {
        return charging;
    }

    public void setCharging(int charging) {
        this.charging = charging;
    }

    public void setBars(String bars) {
        this.bars = bars;
    }

    public String getBars() {
        return bars;
    }
}
