package com.example.luis.smartcharging;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by claudio on 11-07-2018.
 */

public class Percurso implements Parcelable {
    private int id;
    private String origem;
    private String destino;
    private float distancia;

    public Percurso(int id, String origem, String destino, float distancia) {
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.distancia = distancia;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public float getDistancia() {
        return distancia;
    }

    public void setDistancia(float distancia) {
        this.distancia = distancia;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(origem);
        parcel.writeString(destino);
        parcel.writeFloat(distancia);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Percurso createFromParcel(Parcel in) {
            int id = in.readInt();
            String origem = in.readString();
            String destino = in.readString();
            float distancia = in.readFloat();
            return new Percurso(id,origem,destino,distancia);
        }

        public Percurso[] newArray(int size) {
            return new Percurso[size];
        }
    };

}
