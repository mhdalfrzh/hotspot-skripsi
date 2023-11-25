package com.example.admin.simpantas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;

public class ClusteringActivity extends AppCompatActivity {
    EditText inputMinPts, inputEps;
    private DBHelper db;
    Button bDBSCAN;
    String tahunValue = "";
    String bulanValue = "";
    int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = new DBHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);
        inputMinPts = (EditText) findViewById(R.id.txMinPts);
        inputEps = (EditText) findViewById(R.id.txEps);
        bDBSCAN = (Button) findViewById(R.id.btnInput);

        Bundle bundle = getIntent().getExtras();
        tahunValue = bundle.getString("tahunValue");
        bulanValue = bundle.getString("bulanValue");
        state = bundle.getInt("state");
        Log.d("BULAN BERAPA",bulanValue);
        Log.d("TAHUN BERAPA",tahunValue);

        bDBSCAN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strMinPts = inputMinPts.getText().toString();
                String strEps = inputEps.getText().toString();
                int dMinPts = Integer.parseInt(strMinPts);
                double dEps = Double.parseDouble(strEps);
                try {
                    doDBSCAN(dMinPts, dEps);
                    processCluster();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Intent i = new Intent(ClusteringActivity.this, ClusterResultActivity.class);
                i.putExtra("tahunValue",tahunValue);
                i.putExtra("bulanValue",bulanValue);
                i.putExtra("state",state);
                startActivity(i);
            }
        });
    }

    private void doDBSCAN(int dMinPts, double dEps) throws IOException {
        String inputFile = Environment.getExternalStorageDirectory() + "/dbSimpantas/transform/transform-data.csv";
        AlgoDBSCAN algorithm = new AlgoDBSCAN();
        algorithm.runAlgorithm(inputFile, dMinPts, dEps, " ");
        algorithm.printStatistics();
        String output = Environment.getExternalStorageDirectory() + "/dbSimpantas/transform/hasil-transform-data.csv";
        algorithm.saveToFile(output);
    }

    private void processCluster() throws IOException {
        File file = new File(Environment.getExternalStorageDirectory()+"/dbSimpantas/transform/","hasil-transform-data.csv");
        CSVReader reader = null;
        reader = new CSVReader(new FileReader(file.getAbsolutePath()));
        String[] nextLine;
        int count = 1;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine[0].startsWith("@")) {
                continue;
            }
            String output = nextLine[0].replace("][", " ").replace("[", "").replace("]", "");
            String[] columns = output.split("\\s+");
            for (int i = 0; i < columns.length; i += 3) {
                db.insertCluster(count, columns[i], Double.parseDouble(columns[i + 1]), Double.parseDouble(columns[i + 2]));
            }
            count++;
        }
    }
}
