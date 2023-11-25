package com.example.admin.simpantas;

public class Tdbscan {
    private int cluster;
    double  latitude, longitude;
    private String instance, bulan, tahun;
    public static final String TABLE_NAME = "Tdbscan";
    public static final String COLUMN_CLUSTER = "cluster";
    public static final String COLUMN_INSTANCE = "instance";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_BULAN = "bulan";
    public static final String COLUMN_TAHUN = "tahun";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_CLUSTER + " INTEGER,"
            + COLUMN_INSTANCE + " TEXT,"
            + COLUMN_LATITUDE + " DOUBLE,"
            + COLUMN_LONGITUDE + " DOUBLE,"
            + COLUMN_BULAN + " TEXT,"
            + COLUMN_TAHUN + " TEXT"
            + ")";

    public Tdbscan(){

    }

    public Tdbscan(int cluster,String instance, double latitude, double longitude, String bulan, String tahun){
        this.cluster = cluster;
        this.instance = instance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bulan = bulan;
        this.tahun = tahun;
    }

    public int getCluster(){
        return cluster;
    }

    public String getInstance(){
        return instance;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setCluster(int cluster){
        this.cluster = cluster;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getBulan() {
        return bulan;
    }

    public void setBulan(String bulan) {
        this.bulan = bulan;
    }

    public String getTahun() {
        return tahun;
    }

    public void setTahun(String tahun) {
        this.tahun = tahun;
    }
}
