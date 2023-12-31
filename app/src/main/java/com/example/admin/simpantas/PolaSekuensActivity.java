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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoSPADE;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator_Bitmap;

public class PolaSekuensActivity extends AppCompatActivity{
    private DBHelper db;
    private ProgressDialog pDialog;
    private ListView lv;

    EditText inputMinsup;
    LinearLayout minsupView;
    Button bSpade,bMinSup;
    String tahunValue = "";
    String bulanValue = "";
    int state = 0;

    ArrayList<HashMap<String, String>> userList;
    ArrayList<HashMap<String, String>> userListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pola_sekuens);
        db = new DBHelper(this);
        db.removeTransform();
        userList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list_pola);
        minsupView = (LinearLayout) findViewById(R.id.minsupView);
        minsupView.setVisibility(LinearLayout.GONE);

        inputMinsup = (EditText) findViewById(R.id.txMinsup);
        bSpade = (Button) findViewById(R.id.btnSpade);
        bMinSup = (Button) findViewById(R.id.btnInputMinsup);

        Bundle bundle = getIntent().getExtras();
        tahunValue = bundle.getString("tahunValue");
        bulanValue = bundle.getString("bulanValue");
        state = bundle.getInt("state");
        Log.d("BULAN BERAPA",bulanValue);
        Log.d("TAHUN BERAPA",tahunValue);

        new GetFilter().execute();

        bSpade.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doTransform();
                minsupView.setVisibility(LinearLayout.VISIBLE);
            }
        });

        bMinSup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strMinsup = inputMinsup.getText().toString();
                float dMinsup = Float.parseFloat(strMinsup);
                if (dMinsup <= 1.0){
                    try {
                        doSpade(dMinsup,tahunValue);

                        Intent spade = new Intent( PolaSekuensActivity.this, SpadeResultActivity.class);
                        spade.putExtra("tahunValue",tahunValue);
                        spade.putExtra("bulanValue",bulanValue);
                        spade.putExtra("state",state);
                        startActivity(spade);

                    } catch (IOException e) {
                        Log.d("Error SPADE : ",e.getMessage());
                        e.printStackTrace();
                    } catch (NumberFormatException ignored){
                        Log.d("Error Min Support Input",ignored.getMessage());
                        ignored.printStackTrace();
                    }
                }else{
                    Toast.makeText(PolaSekuensActivity.this, "Nilai Minimum Support harus diantara 0 hingga 1", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private class GetFilter extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(PolaSekuensActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userList = db.getTitikForFilter(bulanValue,tahunValue);
            userListView = db.getTitikForFilter(bulanValue,tahunValue);
            db.insertTitikFilter(userList);
            for (int x=0;x<userListView.size();x++){
                Log.d("datanya mana", userListView.get(x).toString());
                userListView.get(x).put("latitude","Latitude : "+userListView.get(x).get("latitude"));
                userListView.get(x).put("longitude","Longitude : "+userListView.get(x).get("longitude"));
                userListView.get(x).put("tanggal","Tanggal (yyyymmdd): "+userListView.get(x).get("tanggal"));
                userListView.get(x).put("unixdatetime","Unixdatetime : "+userListView.get(x).get("unixdatetime"));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    PolaSekuensActivity.this, userListView,
                    R.layout.list_titikpanasfilter, new String[]{"latitude", "longitude", "tanggal", "unixdatetime"}, new int[]{R.id.latitude,
                    R.id.longitude,R.id.tanggal, R.id.unixdatetime});
            lv.setAdapter(adapter);
        }
    }

    // Melakukan transformasi data ke dalam bentuk masukan SPADE di SPMF
    private void doTransform(){
        boolean result;
        String strItem, strTahun = "";
        String initLat = "";
        String initLtd = "";
        String initUnixDate = "";
        String initResult = "";
        int tempSid = 1, i = 0;
        for(i=0; i<userList.size(); i++){
            HashMap<String,String> tempArray = userList.get(i);
            // Inisialisasi awal
            if(i == 0){
                initLat = tempArray.get("latitude");
                initLtd = tempArray.get("longitude");
                initUnixDate = tempArray.get("unixdate");
                initResult = tempArray.get("unixdatetime");
            } else{
                // Cek kondisi apakah berada di latitude dan longitude yang sama
                if(initLat.equals(tempArray.get("latitude")) && initLtd.equals(tempArray.get("longitude"))){
                    // Jika iya cek lagi apakah berada di hari yang sama
                    if(!initUnixDate.equals(tempArray.get("unixdate"))){
                        // Jika tidak tambahkan -1 pada initResult
                        initResult += " -1";
                    }
                    initResult += " " + tempArray.get("unixdatetime");
                } else{
                    // Jika latitude dan longitude tidak sama
                    String[] resultSplit = initResult.split("\\s+");
                    ArrayList<String> dateArr = new ArrayList<>();
                    boolean isMinus = true;
                    for(int j=0; j<resultSplit.length; j++){
                        if(!resultSplit[j].equals("-1")){
                            // Jika result tidak sama dengan -1 maka tambahkan ke ArrayList dateArr
                            dateArr.add(resultSplit[j]);
                            isMinus = false;
                        }
                    }
                    boolean isInsert = true;

                    // Cek kondisi apakah nilainya bukan -1
                    if(!isMinus){
                        Integer subs = Integer.valueOf(dateArr.get(0)) - Integer.valueOf(dateArr.get(1));
                        if (subs > -25200){
                            isInsert = false;
                        }
                    }
                    if(isInsert){
                        initResult += " -1 -2";
                        result = db.insertTransform(tempSid++,initResult,bulanValue,tahunValue);
                    }
                    initResult = tempArray.get("unixdatetime");
                    initLat = tempArray.get("latitude");
                    initLtd = tempArray.get("longitude");
                    initUnixDate = tempArray.get("unixdate");
                }
            }
        }
        initResult += " -1 -2";
        result = db.insertTransform(tempSid++,initResult,bulanValue,tahunValue);
        boolean initExport = db.exportTransform();
        if (result && initExport){
            Toast.makeText(PolaSekuensActivity.this, "Success!!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(PolaSekuensActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void doSpade(double mSupport, String tahunValue) throws IOException {
        boolean keepPatterns = true;
        boolean verbose = false;
        boolean dfs = true;
        boolean outputSequenceIdentifiers = false;

        ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator abstractionCreator =
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();

        ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator idListCreator =
                IdListCreator_Bitmap.getInstance();

        CandidateGenerator candidateGenerator = CandidateGenerator_Qualitative.getInstance();

        ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase sequenceDatabase =
                new ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase(abstractionCreator,idListCreator);

        sequenceDatabase.loadFile(Environment.getExternalStorageDirectory()+"/dbSimpantas/transform/transform-data.csv", mSupport);
        Log.d("Spade : ", sequenceDatabase.toString());

        AlgoSPADE algorithm = new AlgoSPADE(mSupport, dfs, abstractionCreator);

        algorithm.runAlgorithm(sequenceDatabase,candidateGenerator,keepPatterns,verbose,Environment.getExternalStorageDirectory()+"/dbSimpantas/transform/hasil-transform-data.csv",outputSequenceIdentifiers);
        Log.d("Minimum Support : ", String.valueOf(mSupport));
        System.out.print(algorithm.getNumberOfFrequentPatterns() + " frequent patterns.");
        System.out.print(algorithm.printStatistics());
    }

}
