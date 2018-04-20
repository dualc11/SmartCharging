package com.example.luis.smartcharging;

class DadosCharging {

    private String startTime;
    private String carId;
    private String plugId;
    private String tempoEstimado;
    private int bateriaEstimada;

    public DadosCharging(String startTime,String carId,String plugId,String tempoEstimado,int bateriaEstimada)
    {
        this.startTime=startTime;
        this.carId=carId;
        this.plugId=plugId;
        this.tempoEstimado=tempoEstimado;
        this.bateriaEstimada=bateriaEstimada;
    }

    public String getStartTime() {return startTime;}
    public String getCarId(){return carId;}
    public String getPlugId(){return plugId;}
    public String getTempoEstimado(){return tempoEstimado;}
    public int getBateriaEstimada(){return bateriaEstimada;}
}
