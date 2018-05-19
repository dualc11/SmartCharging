package com.example.luis.smartcharging;

public class RegisterInfo {

    private String nrCarro;
    private String nrViagem;
    private String kmsViagem;

    public RegisterInfo(String nrCarro,String nrViagem,String kmsViagem)
    {
        this.nrCarro=nrCarro;
        this.nrViagem=nrViagem;
        this.kmsViagem=kmsViagem;
    }

    public String getNrCarro(){return nrCarro;}
    public String getNrViagem(){return nrViagem;}
    public String getKmsViagem(){return kmsViagem;}
}
