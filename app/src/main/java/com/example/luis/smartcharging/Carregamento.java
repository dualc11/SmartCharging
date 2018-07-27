package com.example.luis.smartcharging;

import java.util.Date;

/**
 * Created by claudio on 27-07-2018.
 */

public class Carregamento {
    private int id;
    private int userId;
    private int tomadaId;
    private int carroId;
    private int batInicial;
    private int batFinal;
    private Date horaInicial;
    private Date horaFinal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTomadaId() {
        return tomadaId;
    }

    public void setTomadaId(int tomadaId) {
        this.tomadaId = tomadaId;
    }

    public int getCarroId() {
        return carroId;
    }

    public void setCarroId(int carroId) {
        this.carroId = carroId;
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

    public Date getHoraInicial() {
        return horaInicial;
    }

    public void setHoraInicial(Date horaInicial) {
        this.horaInicial = horaInicial;
    }

    public Date getHoraFinal() {
        return horaFinal;
    }

    public void setHoraFinal(Date horaFinal) {
        this.horaFinal = horaFinal;
    }

    public Carregamento(int id, int userId, int tomadaId, int carroId, int batInicial,
                        int batFinal, Date horaInicial, Date horaFinal) {
        this.id = id;
        this.userId = userId;
        this.tomadaId = tomadaId;
        this.carroId = carroId;
        this.batInicial = batInicial;
        this.batFinal = batFinal;
        this.horaInicial = horaInicial;
        this.horaFinal = horaFinal;
    }
}
