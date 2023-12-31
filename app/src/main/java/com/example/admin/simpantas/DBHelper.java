package com.example.admin.simpantas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dbSimpantas";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Titik.CREATE_TABLE);
        db.execSQL(Hotspot.CREATE_TABLE);
        db.execSQL(Hotspot.CREATE_TABLE_UPDATE);
        db.execSQL(TitikFilter.CREATE_TABLE);
        db.execSQL(Transform.CREATE_TABLE);
        db.execSQL(Tanggal.CREATE_TABLE);
        db.execSQL(Tspade.CREATE_TABLE);
        db.execSQL(TvFrequent.CREATE_TABLE);
        db.execSQL(Cluster.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Titik.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Hotspot.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Hotspot.TABLE_NAME_UPDATE);
        db.execSQL("DROP TABLE IF EXISTS " + TitikFilter.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Transform.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tanggal.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tspade.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TvFrequent.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Cluster.TABLE_NAME);
        onCreate(db);
    }

    public boolean initTanggal(String tgl) {
        boolean checkDate = false;
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT * FROM " + Tanggal.TABLE_NAME;

        Cursor cursor = db.rawQuery(count, null);
        if (cursor!=null){
            cursor.moveToFirst();
            if (cursor.getCount() ==0) {
                ContentValues values = new ContentValues();
                values.put(Tanggal.COLUMN_TANGGAL, tgl);
                db.insert(Tanggal.TABLE_NAME, null, values);
            }else{
                String dateDb = cursor.getString(cursor.getColumnIndex(Tanggal.COLUMN_TANGGAL));
                if (!dateDb.equals(tgl))
                {
                    db.delete(Tanggal.TABLE_NAME, null, null);
                    ContentValues values = new ContentValues();
                    values.put(Tanggal.COLUMN_TANGGAL, tgl);
                    db.insert(Tanggal.TABLE_NAME, null, values);
                    checkDate = true;
                }
            }
        }
        db.close();
        return checkDate;
    }

    public void removeTitik()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Titik.TABLE_NAME, null, null);
        db.delete(TitikFilter.TABLE_NAME, null, null);
        db.delete(Transform.TABLE_NAME, null, null);
        db.delete(Tspade.TABLE_NAME, null, null);
        db.delete(TvFrequent.TABLE_NAME, null, null);
        db.delete(Cluster.TABLE_NAME, null, null);
    }

    public void removeTitikFilter()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TitikFilter.TABLE_NAME, null, null);
    }
    public void removeHotspotUpdate()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Hotspot.TABLE_NAME_UPDATE, null, null);
    }
    public void removeTransform()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Transform.TABLE_NAME, null, null);
    }

    public void removeTspade()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Tspade.TABLE_NAME, null, null);
    }
    public void removeFrequent()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TvFrequent.TABLE_NAME, null, null);
    }

    public boolean insertTitik(double latitude, double longitude, long unixDate, long unixDateTime, String tanggal, String provinsi, String kabupaten, String kecamatan, String desa, String bulan, String tahun) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Titik.COLUMN_LATITUDE, latitude);
        values.put(Titik.COLUMN_LONGITUDE, longitude);
        values.put(Titik.COLUMN_UNIXDATE, unixDate);
        values.put(Titik.COLUMN_UNIXDATETIME, unixDateTime);
        values.put(Titik.COLUMN_TANGGAL, tanggal);
        values.put(Titik.COLUMN_PROVINSI, provinsi);
        values.put(Titik.COLUMN_KABUPATEN, kabupaten);
        values.put(Titik.COLUMN_KECAMATAN, kecamatan);
        values.put(Titik.COLUMN_DESA, desa);
        values.put(Titik.COLUMN_BULAN, bulan);
        values.put(Titik.COLUMN_TAHUN, tahun);

        long id = db.insert(Titik.TABLE_NAME, null, values);
        db.close();
        return id != -1;
    }

    public boolean insertCluster(int cluster, String instance, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Cluster.COLUMN_CLUSTER, cluster);
        values.put(Cluster.COLUMN_INSTANCE, instance);
        values.put(Cluster.COLUMN_LATITUDE, latitude);
        values.put(Cluster.COLUMN_LONGITUDE, longitude);

        long id = db.insert(Cluster.TABLE_NAME, null, values);
        db.close();
        return id != -1;
    }

    public List<Titik> getAllTitik() {
        List<Titik> titiks = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + Titik.TABLE_NAME + " ORDER BY " + Titik.COLUMN_ID + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Titik titik = new Titik();
                titik.setLatitude(cursor.getDouble(cursor.getColumnIndex(Titik.COLUMN_LATITUDE)));
                titik.setLongitude(cursor.getDouble(cursor.getColumnIndex(Titik.COLUMN_LONGITUDE)));
                titik.setUnixDate(cursor.getInt(cursor.getColumnIndex(Titik.COLUMN_UNIXDATE)));
                titik.setUnixDateTime(cursor.getInt(cursor.getColumnIndex(Titik.COLUMN_UNIXDATETIME)));
                titik.setTanggal(cursor.getString(cursor.getColumnIndex(Titik.COLUMN_TANGGAL)));
                titik.setProvinsi(cursor.getString(cursor.getColumnIndex(Titik.COLUMN_PROVINSI)));
                titik.setKabupaten(cursor.getString(cursor.getColumnIndex(Titik.COLUMN_KABUPATEN)));
                titik.setKecamatan(cursor.getString(cursor.getColumnIndex(Titik.COLUMN_KECAMATAN)));
                titik.setDesa(cursor.getString(cursor.getColumnIndex(Titik.COLUMN_DESA)));

                titiks.add(titik);
            } while (cursor.moveToNext());
        }
        db.close();
        return titiks;
    }

    public ArrayList<HashMap<String, String>> getTitikForFilter(String bulanValue, String tahunValue){
        ArrayList<HashMap<String, String>> titikFilter = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT A."+Titik.COLUMN_LATITUDE+",A."+Titik.COLUMN_LONGITUDE+",A."+Titik.COLUMN_UNIXDATE+",A."+Titik.COLUMN_UNIXDATETIME+",A."+Titik.COLUMN_TANGGAL+",A."+Titik.COLUMN_KABUPATEN+",A."+Titik.COLUMN_KECAMATAN+",A."+Titik.COLUMN_ID+",A."+Titik.COLUMN_BULAN+",A."+Titik.COLUMN_TAHUN+" FROM "+Titik.TABLE_NAME+
                " A JOIN (SELECT count(*) as count,latitude,longitude,unixdate,unixdatetime,bulan,tahun from "+Titik.TABLE_NAME+" WHERE bulan ='"+bulanValue+"' AND tahun ="+tahunValue+" GROUP BY latitude,longitude having count >=2) B on A."+Titik.COLUMN_LATITUDE+"=B."+Titik.COLUMN_LATITUDE+" AND A."+Titik.COLUMN_LONGITUDE+"=B."+Titik.COLUMN_LONGITUDE+" AND A."+Titik.COLUMN_BULAN+"=B."+Titik.COLUMN_BULAN+" AND A."+Titik.COLUMN_TAHUN+"=B."+Titik.COLUMN_TAHUN+
                " ORDER BY A."+Titik.COLUMN_LATITUDE+",A."+Titik.COLUMN_LONGITUDE+",A."+Titik.COLUMN_UNIXDATE+",A."+Titik.COLUMN_UNIXDATETIME ;
        Cursor cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String,String> tf = new HashMap<>();
                tf.put("latitude",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_LATITUDE)));
                tf.put("longitude",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_LONGITUDE)));
                tf.put("unixdate",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_UNIXDATE)));
                tf.put("unixdatetime",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_UNIXDATETIME)));
                tf.put("tanggal",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_TANGGAL)));
                tf.put("kabupaten",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_KABUPATEN)));
                tf.put("kecamatan",cursor.getString(cursor.getColumnIndex(Titik.COLUMN_KECAMATAN)));
                titikFilter.add(tf);
            } while (cursor.moveToNext());
        }
        db.close();
        return titikFilter;
    }

    public ArrayList<HashMap<String, String>> getTitikForCluster(String bulanValue, String tahunValue){
        ArrayList<HashMap<String, String>> titikCluster = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + Cluster.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String,String> tf = new HashMap<>();
                tf.put("cluster",cursor.getString(cursor.getColumnIndex(Cluster.COLUMN_CLUSTER)));
                tf.put("instance",cursor.getString(cursor.getColumnIndex(Cluster.COLUMN_INSTANCE)));
                tf.put("latitude",cursor.getString(cursor.getColumnIndex(Cluster.COLUMN_LATITUDE)));
                tf.put("longitude",cursor.getString(cursor.getColumnIndex(Cluster.COLUMN_LONGITUDE)));
                titikCluster.add(tf);
            } while (cursor.moveToNext());
        }
        db.close();
        return titikCluster;
    }

    public void insertTitikFilter(ArrayList <HashMap<String, String>> tf) {
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i=0; i<tf.size(); i++){
            ContentValues values = new ContentValues();
            HashMap<String,String> valTF = new HashMap<>();
            valTF = tf.get(i);
            values.put(TitikFilter.COLUMN_LATITUDE, valTF.get("latitude"));
            values.put(TitikFilter.COLUMN_LONGITUDE, valTF.get("longitude"));
            values.put(TitikFilter.COLUMN_UNIXDATE, valTF.get("unixdate"));
            values.put(TitikFilter.COLUMN_UNIXDATETIME, valTF.get("unixdatetime"));
            values.put(TitikFilter.COLUMN_TANGGAL, valTF.get("tanggal"));
            values.put(TitikFilter.COLUMN_KABUPATEN, valTF.get("kabupaten"));
            values.put(TitikFilter.COLUMN_KECAMATAN, valTF.get("kecamatan"));
            db.insert(TitikFilter.TABLE_NAME, null, values);
        }
        db.close();
    }

    public void insertTitikCluster(ArrayList <HashMap<String, String>> tf, String bulan, String tahun) {
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i=0; i<tf.size(); i++){
            ContentValues values = new ContentValues();
            HashMap<String,String> valTF = new HashMap<>();
            valTF = tf.get(i);
            values.put(Tdbscan.COLUMN_CLUSTER, valTF.get("cluster"));
            values.put(Tdbscan.COLUMN_INSTANCE, valTF.get("instance"));
            values.put(Tdbscan.COLUMN_LATITUDE, valTF.get("latitude"));
            values.put(Tdbscan.COLUMN_LONGITUDE, valTF.get("longitude"));
            values.put(Tdbscan.COLUMN_BULAN, valTF.get("bulan"));
            values.put(Tdbscan.COLUMN_TAHUN, valTF.get("tahun"));
            db.insert(TitikFilter.TABLE_NAME, null, values);
        }
        db.close();
    }

    public boolean insertHotspot(double latitude, double longitude, int confidence, String kawasan, String tanggal, String kecamatan, String kabupaten, int temp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Hotspot.COLUMN_LATITUDE, latitude);
        values.put(Hotspot.COLUMN_LONGITUDE, longitude);
        values.put(Hotspot.COLUMN_CONFIDENCE, confidence);
        values.put(Hotspot.COLUMN_KAWASAN, kawasan);
        values.put(Hotspot.COLUMN_TANGGAL, tanggal);
        values.put(Hotspot.COLUMN_KECAMATAN, kecamatan);
        values.put(Hotspot.COLUMN_KABUPATEN, kabupaten);
        values.put(Hotspot.COLUMN_TEMP, temp);

        long id = db.insert(Hotspot.TABLE_NAME, null, values);
        db.close();
        return id != -1;
    }

    public List<Hotspot> getAllHotspot() {
        List<Hotspot> hotspots = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Hotspot.TABLE_NAME + " ORDER BY " +
                Hotspot.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Hotspot hs = new Hotspot();
                hs.setLatitude(cursor.getDouble(cursor.getColumnIndex(Hotspot.COLUMN_LATITUDE)));
                hs.setLongitude(cursor.getDouble(cursor.getColumnIndex(Hotspot.COLUMN_LONGITUDE)));
                hs.setConfidence(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_CONFIDENCE)));
                hs.setKawasan(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KAWASAN)));
                hs.setTanggal(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_TANGGAL)));
                hs.setKecamatan(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KECAMATAN)));
                hs.setKabupaten(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KABUPATEN)));
                hs.setTemp(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_TEMP)));

                hotspots.add(hs);
            } while (cursor.moveToNext());
        }
        db.close();
        return hotspots;
    }

    public void updateHotspot(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + Hotspot.TABLE_NAME + " WHERE " +
                Hotspot.COLUMN_ID + " > 0";

        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst())
        {
            do{
                Hotspot hs = new Hotspot();
                if(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_ID))>0){
                    ContentValues values = new ContentValues();
                    values.put(Hotspot.COLUMN_TEMP, cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_TEMP))+1);
                    db.update(Hotspot.TABLE_NAME, values, Hotspot.COLUMN_ID + "= ?",new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_ID)))});
                }
            }
            while (cursor.moveToNext());
        }
        db.close();
        Log.d("Test Flow", "MASUK SINI GA 2");
    }

    public boolean insertHotspotUpdate(double latitude, double longitude, int confidence, String kawasan, String tanggal, String kecamatan, String kabupaten, int temp) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Hotspot.COLUMN_LATITUDE, latitude);
        values.put(Hotspot.COLUMN_LONGITUDE, longitude);
        values.put(Hotspot.COLUMN_CONFIDENCE, confidence);
        values.put(Hotspot.COLUMN_KAWASAN, kawasan);
        values.put(Hotspot.COLUMN_TANGGAL, tanggal);
        values.put(Hotspot.COLUMN_KECAMATAN, kecamatan);
        values.put(Hotspot.COLUMN_KABUPATEN, kabupaten);
        values.put(Hotspot.COLUMN_TEMP, temp);

        // insert row
        long id = db.insert(Hotspot.TABLE_NAME_UPDATE, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id != -1;
    }

    public void insertHotspotUpdateWithArray(HashMap<String, String> hs) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();
        double latitude = 0; double longitude = 0;
        int confidence = 0; String strConfidence = "";

        latitude = Double.parseDouble(hs.get("latitude"));
        longitude = Double.parseDouble(hs.get("longitude"));

        strConfidence = hs.get("confidence").substring(0,hs.get("confidence").length()-1);
        confidence = Integer.parseInt(strConfidence);

        ContentValues values = new ContentValues();
        values.put(Hotspot.COLUMN_LATITUDE, latitude);
        values.put(Hotspot.COLUMN_LONGITUDE, longitude);
        values.put(Hotspot.COLUMN_CONFIDENCE, confidence);
        values.put(Hotspot.COLUMN_KAWASAN, hs.get("kawasan"));
        values.put(Hotspot.COLUMN_TANGGAL, hs.get("tanggal"));
        values.put(Hotspot.COLUMN_KECAMATAN, hs.get("kecamatan"));
        values.put(Hotspot.COLUMN_KABUPATEN, hs.get("kabupatenKota"));
        values.put(Hotspot.COLUMN_PROVINSI, hs.get("provinsi"));
        values.put(Hotspot.COLUMN_TEMP, 0);

        // insert row
        db.insert(Hotspot.TABLE_NAME_UPDATE, null, values);

        // close db connection
        db.close();
    }

    public List<Hotspot> getAllHotspotUpdate() {
        List<Hotspot> hotspots = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + Hotspot.TABLE_NAME_UPDATE + " ORDER BY " +
                Hotspot.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Hotspot hs = new Hotspot();
                hs.setLatitude(cursor.getDouble(cursor.getColumnIndex(Hotspot.COLUMN_LATITUDE)));
                hs.setLongitude(cursor.getDouble(cursor.getColumnIndex(Hotspot.COLUMN_LONGITUDE)));
                hs.setConfidence(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_CONFIDENCE)));
                hs.setKawasan(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KAWASAN)));
                hs.setTanggal(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_TANGGAL)));
                hs.setKecamatan(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KECAMATAN)));
                hs.setKabupaten(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KABUPATEN)));
                hs.setProvinsi(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_PROVINSI)));
                hs.setTemp(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_TEMP)));
                hotspots.add(hs);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();
        // return notes list
        Log.d("Get : ",""+ hotspots);
        return hotspots;
    }

    public List<Hotspot> getSelectedHotspot(int i) {
        List<Hotspot> hotspots = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Hotspot.TABLE_NAME + " WHERE " +
                Hotspot.COLUMN_TEMP + "=" + i;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Hotspot hs = new Hotspot();
                hs.setLatitude(cursor.getDouble(cursor.getColumnIndex(Hotspot.COLUMN_LATITUDE)));
                hs.setLongitude(cursor.getDouble(cursor.getColumnIndex(Hotspot.COLUMN_LONGITUDE)));
                hs.setConfidence(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_CONFIDENCE)));
                hs.setKawasan(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KAWASAN)));
                hs.setTanggal(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_TANGGAL)));
                hs.setKecamatan(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KECAMATAN)));
                hs.setKabupaten(cursor.getString(cursor.getColumnIndex(Hotspot.COLUMN_KABUPATEN)));
                hs.setTemp(cursor.getInt(cursor.getColumnIndex(Hotspot.COLUMN_TEMP)));

                hotspots.add(hs);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();
        // return notes list
        return hotspots;
    }

    public void migrateHotspotData(List<Hotspot> hs){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (Hotspot hotspot : hs){
            values.put(Hotspot.COLUMN_LATITUDE, hotspot.getLatitude());
            values.put(Hotspot.COLUMN_LONGITUDE, hotspot.getLongitude());
            values.put(Hotspot.COLUMN_CONFIDENCE, hotspot.getConfidence());
            values.put(Hotspot.COLUMN_KAWASAN, hotspot.getKawasan());
            values.put(Hotspot.COLUMN_TANGGAL, hotspot.getTanggal());
            values.put(Hotspot.COLUMN_KECAMATAN, hotspot.getKecamatan());
            values.put(Hotspot.COLUMN_KABUPATEN, hotspot.getKabupaten());
            values.put(Hotspot.COLUMN_PROVINSI, hotspot.getProvinsi());
            values.put(Hotspot.COLUMN_TEMP, 1);
            Log.d("Added ",""+ values);
            db.insert(Hotspot.TABLE_NAME, null, values);
        }
        db.close();
        Log.d("Test Flow", "MASUK SINI GA 3");
    }

    //TRANSFORM
    public boolean insertTransform(int sid, String item, String bulan, String tahun) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Transform.COLUMN_SID, sid);
        values.put(Transform.COLUMN_ITEM, item);
        values.put(Transform.COLUMN_BULAN, bulan);
        values.put(Transform.COLUMN_TAHUN, tahun);

        // insert row
        long id = db.insert(Transform.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id != -1;
    }


    public boolean exportTransform() {
        boolean statsInput = true;
        File file = null;
        File exportFile = new File(Environment.getExternalStorageDirectory()+"/dbSimpantas","transform");
        if (!exportFile.exists())
        {
            Log.d("Cek ada folder ngga", "exportTransform: ");
            exportFile.mkdirs();
            Log.d("Cek ada folder ngga 2", "exportTransform: ");
        }
        file = new File(exportFile,"transform-data.csv");
        try{
            file.createNewFile();
            CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + Transform.TABLE_NAME;
            Cursor c = db.rawQuery(selectQuery,null);
            while(c.moveToNext()){
                String rowData = c.getString(1);
                rowData = rowData.replaceAll("\"","");
                String arrStr[] = {rowData};
                csvWriter.writeNext(arrStr,false);
                Log.d("VALUE INPUTNYA ", Arrays.toString(arrStr));
            }
            csvWriter.close();
            c.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Export Transform", e.getMessage(), e);
            statsInput = false;
        }
        return statsInput;
    }

    public boolean exportCluster() {
        boolean statsInput = true;
        File file = null;
        File exportFile = new File(Environment.getExternalStorageDirectory()+"/dbSimpantas","transform");
        if (!exportFile.exists())
        {
            Log.d("Cek ada folder ngga", "exportTransform: ");
            exportFile.mkdirs();
            Log.d("Cek ada folder ngga 2", "exportTransform: ");
        }
        file = new File(exportFile,"transform-data.csv");
        try{
            file.createNewFile();
            CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT LATITUDE, LONGITUDE FROM " + Titik.TABLE_NAME;
            Cursor c = db.rawQuery(selectQuery,null);
            while(c.moveToNext()){
                String rowLat = c.getString(0);
                String rowLong = c.getString(1);
                String arrStr[] = {rowLat + " " + rowLong};
                csvWriter.writeNext(arrStr,false);
                Log.d("VALUE INPUTNYA ", Arrays.toString(arrStr));
            }
            csvWriter.close();
            c.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Export Transform", e.getMessage(), e);
            statsInput = false;
        }
        return statsInput;
    }

    //TSPADE
    public void insertTspade(HashMap<String, String> ts) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Tspade.COLUMN_UNIXDATETIME, ts.get("unixdatetime"));
        values.put(Tspade.COLUMN_BULAN, ts.get("month"));
        values.put(Tspade.COLUMN_TAHUN, ts.get("year"));

        // insert row
        db.insert(Tspade.TABLE_NAME, null, values);

        // close db connection
        db.close();
    }

    public  ArrayList<HashMap<String, String>> getTspadeByDate(String month, String year) {
        ArrayList<HashMap<String, String>> tspadeFilter = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + Tspade.TABLE_NAME + " WHERE " +
                Tspade.COLUMN_BULAN + "='" + month + "' AND " + Tspade.COLUMN_TAHUN + "='" + year+"'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("String Query SPADE", selectQuery);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> ts = new HashMap<>();
                ts.put("unixdatetime", cursor.getString(cursor.getColumnIndex(Tspade.COLUMN_UNIXDATETIME)));
                ts.put("tahun", cursor.getString(cursor.getColumnIndex(Tspade.COLUMN_TAHUN)));
                ts.put("bulan", cursor.getString(cursor.getColumnIndex(Tspade.COLUMN_BULAN)));
                tspadeFilter.add(ts);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();

        return tspadeFilter;
    }

    public  ArrayList<HashMap<String, String>> getClusterByDate(String month, String year) {
        ArrayList<HashMap<String, String>> clusterFilter = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + Tdbscan.TABLE_NAME + " WHERE " +
                Tdbscan.COLUMN_BULAN + "='" + month + "' AND " + Tdbscan.COLUMN_TAHUN + "='" + year+"'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("String Query SPADE", selectQuery);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> ts = new HashMap<>();
                ts.put("cluster", cursor.getString(cursor.getColumnIndex(Tdbscan.COLUMN_CLUSTER)));
                ts.put("instance", cursor.getString(cursor.getColumnIndex(Tdbscan.COLUMN_INSTANCE)));
                ts.put("latitude", cursor.getString(cursor.getColumnIndex(Tdbscan.COLUMN_LATITUDE)));
                ts.put("longitude", cursor.getString(cursor.getColumnIndex(Tdbscan.COLUMN_LONGITUDE)));
                ts.put("bulan", cursor.getString(cursor.getColumnIndex(Tdbscan.COLUMN_BULAN)));
                ts.put("tahun", cursor.getString(cursor.getColumnIndex(Tdbscan.COLUMN_TAHUN)));
                clusterFilter.add(ts);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();

        return clusterFilter;
    }



    public List<TvFrequent> getCoordinatesByResultTspade(String unix) {
        List<TvFrequent> result = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TitikFilter.TABLE_NAME + " WHERE " +
                TitikFilter.COLUMN_UNIXDATETIME + "='" +unix+"' ORDER BY "+TitikFilter.COLUMN_LATITUDE+" ASC";
        Log.d("QUERY",selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TvFrequent tf = new TvFrequent();
                tf.setLatitude(cursor.getDouble(cursor.getColumnIndex(TitikFilter.COLUMN_LATITUDE)));
                tf.setLongitude(cursor.getDouble(cursor.getColumnIndex(TitikFilter.COLUMN_LONGITUDE)));
                tf.setKabupaten(cursor.getString(cursor.getColumnIndex(TitikFilter.COLUMN_KABUPATEN)));
                tf.setKecamatan(cursor.getString(cursor.getColumnIndex(TitikFilter.COLUMN_KECAMATAN)));
                tf.setTanggal1(unix);
                tf.setTanggal2("NA");
                tf.setTanggal3("NA");
                tf.setTanggal4("NA");
                result.add(tf);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();
        // return notes list
        return result;
    }

    public List<TvFrequent> getCoordinatesByResultTspadeWith2Input(String unix1,String unix2) {
        List<TvFrequent> result = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TitikFilter.TABLE_NAME + " WHERE " +
                TitikFilter.COLUMN_UNIXDATETIME + "='" + unix1+"'"+ " OR "+ TitikFilter.COLUMN_UNIXDATETIME + "= '"+ unix2+"' ORDER BY "+ TitikFilter.COLUMN_LATITUDE+" ASC";
        Log.d("QUERY",selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TvFrequent tf = new TvFrequent();
                tf.setLatitude(cursor.getDouble(cursor.getColumnIndex(TitikFilter.COLUMN_LATITUDE)));
                tf.setLongitude(cursor.getDouble(cursor.getColumnIndex(TitikFilter.COLUMN_LONGITUDE)));
                tf.setKabupaten(cursor.getString(cursor.getColumnIndex(TitikFilter.COLUMN_KABUPATEN)));
                tf.setKecamatan(cursor.getString(cursor.getColumnIndex(TitikFilter.COLUMN_KECAMATAN)));
                tf.setUnix1(unix1);
                tf.setUnix2(unix2);
                tf.setUnix3("NA");
                tf.setUnix4("NA");
                tf.setTanggal1(cursor.getString(cursor.getColumnIndex(TitikFilter.COLUMN_TANGGAL)));
                tf.setTanggal2("NA");
                tf.setTanggal3("NA");
                tf.setTanggal4("NA");
                result.add(tf);
            } while (cursor.moveToNext());
        }
        // close db connection
        db.close();
        // return notes list
        return result;
    }

    public void insertTvFrequentDate(List<TvFrequent> tfq){
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for(TvFrequent valTF : tfq){
            values.put(TvFrequent.COLUMN_LATITUDE, valTF.getLatitude());
            values.put(TvFrequent.COLUMN_LONGITUDE, valTF.getLongitude());
            values.put(TvFrequent.COLUMN_KABUPATEN, valTF.getKabupaten());
            values.put(TvFrequent.COLUMN_KECAMATAN, valTF.getKecamatan());
            values.put(TvFrequent.COLUMN_UNIX1, valTF.getUnix1());
            values.put(TvFrequent.COLUMN_UNIX2, valTF.getUnix2());
            values.put(TvFrequent.COLUMN_UNIX3, valTF.getUnix3());
            values.put(TvFrequent.COLUMN_UNIX4, valTF.getUnix4());
            values.put(TvFrequent.COLUMN_TANGGAL1, valTF.getTanggal1());
            values.put(TvFrequent.COLUMN_TANGGAL2, valTF.getTanggal2());
            values.put(TvFrequent.COLUMN_TANGGAL3, valTF.getTanggal3());
            values.put(TvFrequent.COLUMN_TANGGAL4, valTF.getTanggal4());
            db.insert(TvFrequent.TABLE_NAME, null, values);
        }

        // close db connection
        db.close();
    }

    public List<Cluster> getAllClusters() {
        List<Cluster> clusterList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Cluster.TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Cluster cluster = new Cluster();
                cluster.setCluster(Integer.parseInt(cursor.getString(0)));
                cluster.setInstance(cursor.getString(1));
                cluster.setLatitude(cursor.getDouble(2));
                cluster.setLongitude(cursor.getDouble(3));
                clusterList.add(cluster);
            } while (cursor.moveToNext());
        }
        db.close();
        return clusterList;
    }
}
