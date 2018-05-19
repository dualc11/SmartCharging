package com.example.luis.smartcharging;

public class DadosFleet {

    private String idTuc;
    private String estado;
    private String bars;

    public DadosFleet(String idTuc,String estado,String bars)
    {
        this.idTuc=idTuc;
        this.estado=estado;
        this.bars=bars;
    }

    public String getIdTuc() {
        return idTuc;
    }

    public String getEstado() {
        return estado;
    }

    public String getBars() {
        return bars;
    }
}
