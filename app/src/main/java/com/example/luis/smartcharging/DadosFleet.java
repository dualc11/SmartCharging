package com.example.luis.smartcharging;

public class DadosFleet {

    private String idTuc;
    private String available;
    private String charging;
    private String bars;

    public DadosFleet(String idTuc,String available, String charging,String bars)
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

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getCharging() {
        return charging;
    }

    public void setCharging(String charging) {
        this.charging = charging;
    }

    public void setBars(String bars) {
        this.bars = bars;
    }

    public String getBars() {
        return bars;
    }
}
