package com.example.admin.simpantas;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public class InputTitikActivity extends AppCompatActivity{
    private List<Titik> titiks = new ArrayList<>();
    String time = "", strTahun = "", strBulan = "", date = "";
    Button btnProcess, btnPattern, btnCari;
    Spinner spinnerTahun, spinnerBulan;
    File file;
    private DBHelper db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_titik);

        // Inisialisasi
        spinnerBulan = (Spinner) findViewById(R.id.spinnerBulan);
        spinnerTahun = (Spinner) findViewById(R.id.spinnerTahun);
        btnCari = (Button) findViewById(R.id.btnCari);
        btnProcess = (Button) findViewById(R.id.btnProses);
        btnPattern = (Button) findViewById(R.id.btnPattern);
        db = new DBHelper(this);

        Intent intent = getIntent();
        int valTemp = intent.getExtras().getInt("temp menu");
        Log.d("temp value",String.valueOf(valTemp));

        ProgressDialog pd = new ProgressDialog(InputTitikActivity.this);

        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, R.array.arrayTahun, R.layout.support_simple_spinner_dropdown_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTahun.setAdapter(aa);

        ArrayAdapter<CharSequence> ab = ArrayAdapter.createFromResource(this, R.array.arrayBulan, R.layout.support_simple_spinner_dropdown_item);
        ab.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBulan.setAdapter(ab);

        if (valTemp == 1){
            btnCari.setVisibility(View.GONE);
            btnPattern.setVisibility(View.GONE);
        } else {
            btnProcess.setVisibility(View.GONE);
            btnPattern.setVisibility(View.GONE);
        }

        btnProcess.setOnClickListener(v -> {
            // cari file di dalam direktori
            file = new File(Environment.getExternalStorageDirectory() + "/dbSimpantas/", ab.getItem(spinnerBulan.getSelectedItemPosition()) + "" + aa.getItem(spinnerTahun.getSelectedItemPosition()) + ".csv");
            if (file.exists()){
                strTahun = spinnerTahun.getSelectedItem().toString();
                strBulan = spinnerBulan.getSelectedItem().toString();
                btnProcess.setVisibility(View.GONE);

                pd.setMessage("Please Wait...");
                pd.setCancelable(false);
                pd.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processReadCSV(file);
                        pd.dismiss();
                    }
                }).start();
                btnPattern.setVisibility(View.VISIBLE);
            } else{
                Toast.makeText(InputTitikActivity.this, "File didn't exist. Check dbSimpantas folder again.", Toast.LENGTH_SHORT).show();
            }
        });

        btnPattern.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent titik = new Intent( InputTitikActivity.this, PolaSekuensActivity.class);
                titik.putExtra("tahunValue",aa.getItem(spinnerTahun.getSelectedItemPosition()));
                titik.putExtra("bulanValue",ab.getItem(spinnerBulan.getSelectedItemPosition()));
                titik.putExtra("state",1);
                startActivity(titik);
            }
        });

        btnCari.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent titik = new Intent( InputTitikActivity.this, SpadeResultActivity.class);
                titik.putExtra("tahunValue",aa.getItem(spinnerTahun.getSelectedItemPosition()));
                titik.putExtra("bulanValue",ab.getItem(spinnerBulan.getSelectedItemPosition()));
                titik.putExtra("state",2);
                startActivity(titik);
            }
        });
    }

    private void processReadCSV(File file) {
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("HH:mm:ss a");
        String strTime = "";
        CsvReader csvReader = new CsvReader();
        csvReader.setFieldSeparator(';');
        String line = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
            try{
                CsvParser csvParser = csvReader.parse(new BufferedReader(new FileReader(file.getAbsolutePath())));
                CsvRow row;
                boolean isFirstLine = true;
                while((row = csvParser.nextRow()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // Skip baris pertama
                    }

                    String[] tokens = row.getField(0).split(",");
                    Titik readTitik = new Titik();

                    // Inisialisasi latitude dan longitude dengan 3 angka desimal
                    double latitude = Double.parseDouble(tokens[8]);
                    double longitude = Double.parseDouble(tokens[9]);
                    String formattedLatitude = String.format("%.3f", latitude).replace(",", ".");
                    String formattedLongitude = String.format("%.3f", longitude).replace(",", ".");
                    Log.d("InputActivity", "Latitude: " + latitude + " Longitude: " + longitude);

                    // Mengubah atribut date ke bentuk unix
                    date = tokens[4];
                    SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
                    long unixDate = 0;
                    try {
                        Date tanggal = formatDate.parse(date);
                        unixDate = tanggal.getTime() / 1000;
                        System.out.println("Unix timestamp: " + unixDate);
                    } catch (ParseException e) {
                        System.out.println("Failed to parse date.");
                        e.printStackTrace();
                    }

                    // Mengubah atribut time ke bentuk unix
                    time = tokens[5].substring(0, tokens[5].length() - 4);
                    String dateTime = date + " " + time;
                    SimpleDateFormat formatTime = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    long unixTime = 0;
                    try {
                        Date waktu = formatTime.parse(dateTime);
                        unixTime = waktu.getTime() / 1000;
                        System.out.println("Unix timestamp: " + unixTime);
                    } catch (ParseException e) {
                        System.out.println("Failed to parse date and time.");
                        e.printStackTrace();
                    }
                    db.insertTitik(Double.parseDouble(formattedLatitude), Double.parseDouble(formattedLongitude), unixDate, unixTime, tokens[4], tokens[0], tokens[1], tokens[2], tokens[3], strBulan, strTahun);
                }
            } catch(IOException e){
                Log.d("InputActivity", "Error Reading on the line " + line, e);
                e.printStackTrace();
            }
        }
    }
}
