package com.example.admin.simpantas;

public class Cluster {
    private int cluster;
    double  latitude, longitude;
    private String instance;
    public static final String TABLE_NAME = "Cluster";
    public static final String COLUMN_CLUSTER = "cluster";
    public static final String COLUMN_INSTANCE = "instance";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_CLUSTER + " INTEGER,"
            + COLUMN_INSTANCE + " TEXT,"
            + COLUMN_LATITUDE + " DOUBLE,"
            + COLUMN_LONGITUDE + " DOUBLE"
            + ")";

    public Cluster(){

    }

    public Cluster(int cluster,String instance, double latitude, double longitude){
        this.cluster = cluster;
        this.instance = instance;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
