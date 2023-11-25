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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;


public class InputTitikCluster extends AppCompatActivity {

    private List<Titik> titiks = new ArrayList<>();

    String time = "", strTahun = "", strBulan = "", date = "";
    //    ProgressDialog pDialog;
    TextView lblTitle;
    Button btnProcess, btnDBSCAN, btnCari;
    Spinner spinnerTahun, spinnerBulan, spinnerTanggal;
    File file;

    private DBHelper db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_titik_cluster);

        spinnerBulan = (Spinner) findViewById(R.id.spinnerBulan);
        spinnerTahun = (Spinner) findViewById(R.id.spinnerTahun);
        spinnerTanggal = (Spinner) findViewById(R.id.spinnerTanggal);
        btnCari = (Button) findViewById(R.id.btnCari);
        btnProcess = (Button) findViewById(R.id.btnProses);
        btnDBSCAN = (Button) findViewById(R.id.btnDBSCAN);

        db = new DBHelper(this);
        Intent intent = getIntent();
        int valTemp = intent.getExtras().getInt("temp menu");
        Log.d("temp value", String.valueOf(valTemp));

        ProgressDialog pd = new ProgressDialog(InputTitikCluster.this);

        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, R.array.arrayTahun, R.layout.support_simple_spinner_dropdown_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTahun.setAdapter(aa);
        ArrayAdapter<CharSequence> ab = ArrayAdapter.createFromResource(this, R.array.arrayBulan, R.layout.support_simple_spinner_dropdown_item);
        ab.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBulan.setAdapter(ab);
        ArrayAdapter<CharSequence> ac = ArrayAdapter.createFromResource(this, R.array.arrayTanggal, R.layout.support_simple_spinner_dropdown_item);
        ac.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTanggal.setAdapter(ac);

        if (valTemp == 1) {
            btnCari.setVisibility(View.GONE);
            btnDBSCAN.setVisibility(View.GONE);
        } else {
            btnProcess.setVisibility(View.GONE);
            btnDBSCAN.setVisibility(View.GONE);
        }

        btnProcess.setOnClickListener(v -> {
            file = new File(Environment.getExternalStorageDirectory() + "/dbSimpantas/", ac.getItem(spinnerTanggal.getSelectedItemPosition()) + "" + ab.getItem(spinnerBulan.getSelectedItemPosition()) + "" + aa.getItem(spinnerTahun.getSelectedItemPosition()) + ".csv");
            Log.d("FILE NAME", String.valueOf(file));
            if (file.exists()) {
                strTahun = spinnerTahun.getSelectedItem().toString();
                strBulan = spinnerBulan.getSelectedItem().toString();
                btnProcess.setVisibility(View.GONE);

                pd.setMessage("Please Wait...");
                pd.setCancelable(false);
                pd.show();
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processReadCSV(file);
                        pd.dismiss();
                    }
                }).start();
                btnDBSCAN.setVisibility(View.VISIBLE);
            } else {
                Log.d("cek file", Environment.getExternalStorageDirectory().toString());
                Toast.makeText(InputTitikCluster.this, "file didn't exist. Check dataSimpantas folder again.", Toast.LENGTH_SHORT).show();
            }
        });
        btnDBSCAN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean initExport = db.exportCluster();
                Log.d("ClusteringActivity", "udah diexport apa belum sih");
                if (initExport) {
                    Toast.makeText(InputTitikCluster.this, "Success!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InputTitikCluster.this, "Error!!", Toast.LENGTH_SHORT).show();
                }

                Intent titik = new Intent(InputTitikCluster.this, ClusteringActivity.class);
                titik.putExtra("tahunValue", aa.getItem(spinnerTahun.getSelectedItemPosition()));
                titik.putExtra("bulanValue", ab.getItem(spinnerBulan.getSelectedItemPosition()));
                titik.putExtra("state", 2);
                startActivity(titik);
            }
        });

        btnCari.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent titik = new Intent(InputTitikCluster.this, SpadeResultActivity.class);
                titik.putExtra("tahunValue", aa.getItem(spinnerTahun.getSelectedItemPosition()));
                titik.putExtra("bulanValue", ab.getItem(spinnerBulan.getSelectedItemPosition()));
                titik.putExtra("state", 2);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                CsvParser csvParser = csvReader.parse(new BufferedReader(new FileReader(file.getAbsolutePath())));
                CsvRow row;
                boolean isFirstLine = true;
                while ((row = csvParser.nextRow()) != null) {
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
            } catch (IOException e) {
                Log.d("InputActivity", "Error Reading on the line " + line, e);
                e.printStackTrace();
            }
        }
    }
}
