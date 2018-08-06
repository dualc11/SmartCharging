package com.example.luis.sampledata;

import java.util.Date;

/**
 * Created by claudio on 03-08-2018.
 */

public class LogInfo {
    private int id;
    private int batInicial;
    private int batFinal;
    private float distanciaKm;
    private Date data;
    private int tucId;
    private int utilizacaoId;
    private int type;

    public LogInfo(int id, int batInicial, int batFinal, float distanciaKm, Date data, int tucId, int utilizacaoId, int type) {
        this.id = id;
        this.batInicial = batInicial;
        this.batFinal = batFinal;
        this.distanciaKm = distanciaKm;
        this.data = data;
        this.tucId = tucId;
        this.utilizacaoId = utilizacaoId;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBatInicial() {
        return batInicial;
    }

    public void setBatInicial(int batInicial) {
        this.batInicial = batInicial;
    }

    public int getBatFinal() {
        return batFinal;
    }

    public void setBatFinal(int batFinal) {
        this.batFinal = batFinal;
    }

    public float getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(float distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public int getTucId() {
        return tucId;
    }

    public void setTucId(int tucId) {
        this.tucId = tucId;
    }

    public int getUtilizacaoId() {
        return utilizacaoId;
    }

    public void setUtilizacaoId(int utilizacaoId) {
        this.utilizacaoId = utilizacaoId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
