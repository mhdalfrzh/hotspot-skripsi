package com.example.admin.simpantas;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ClusterResultActivity extends AppCompatActivity {
    ArrayList<HashMap<String, String>> clusterList;
    private DBHelper db;
    private ProgressDialog pDialog;
    Button resultMap;
    int state = 0;
    ArrayList<HashMap<String, String>> userList;
    ArrayList<HashMap<String, String>> userListView;
    String tahunValue = "";
    private int temp = 0;
    String bulanValue = "";
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_result);
        db = new DBHelper(this);

        lv = (ListView) findViewById(R.id.list_view);
        resultMap = (Button) findViewById(R.id.btnResultMap);
        clusterList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        tahunValue = bundle.getString("tahunValue");
        bulanValue = bundle.getString("bulanValue");
        state = bundle.getInt("state");
        Log.d("BULAN BERAPA",bulanValue);
        Log.d("TAHUN BERAPA",tahunValue);

        new GetClusterResult().execute();

        resultMap.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                temp = 1;
                Intent i = new Intent(ClusterResultActivity.this, VisualisasiCluster.class);
                i.putExtra("temp menu", temp);
                startActivity(i);
            }
        });

    }

    private class GetClusterResult extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ClusterResultActivity.this);
            pDialog.setMessage("Getting Result...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //KALO DARI INPUT BARU
            userList = db.getTitikForCluster(bulanValue,tahunValue);
            userListView = db.getTitikForCluster(bulanValue,tahunValue);
            db.insertTitikCluster(userList, bulanValue, tahunValue);
            for (int x=0;x<userListView.size();x++){
                Log.d("datanya mana", userListView.get(x).toString());
                userListView.get(x).put("cluster","Cluster : "+userListView.get(x).get("cluster"));
                userListView.get(x).put("instance","instance : "+userListView.get(x).get("instance"));
                userListView.get(x).put("latitude","latitude: "+userListView.get(x).get("latitude"));
                userListView.get(x).put("longitude","longitude : "+userListView.get(x).get("longitude"));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            ListAdapter adapter = new SimpleAdapter(
                    ClusterResultActivity.this, userListView,
                    R.layout.list_cluster, new String[]{"cluster", "instance", "latitude", "longitude"}, new int[]{R.id.cluster,
                    R.id.instance,R.id.latitude, R.id.longitude});
            lv.setAdapter(adapter);
        }
    }
}
