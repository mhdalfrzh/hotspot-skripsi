package com.example.admin.simpantas;

import static org.apache.commons.lang3.StringUtils.isNumeric;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
public class SpadeResultActivity extends AppCompatActivity {

    ArrayList<HashMap<String, String>> spadeList;
    ArrayList<HashMap<String, String>> finalList;
    ArrayList<String> resultList = new ArrayList<>();
    List<TvFrequent> frequentVisualizationFix = new ArrayList<>();
    private DBHelper db;
    private ProgressDialog pDialog;
    ListView lv;
    TextView showDate;
    Button resultMap;

    String tahunValue = "";
    String bulanValue = "";
    int state = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spade_result);
        db = new DBHelper(this);
        db.removeFrequent();
        lv = (ListView) findViewById(R.id.spadeList);
        showDate = (TextView) findViewById(R.id.strDateResult);
        resultMap = (Button) findViewById(R.id.btnResultMap);
        spadeList = new ArrayList<>();
        resultList = new ArrayList<>();
        finalList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        tahunValue = bundle.getString("tahunValue");
        bulanValue = bundle.getString("bulanValue");
        state = bundle.getInt("state");
        Log.d("BULAN BERAPA",bulanValue);
        Log.d("TAHUN BERAPA",tahunValue);
        showDate.setText("Result Spade : "+bulanValue+" "+tahunValue);

        new GetSpadeResult().execute();

        resultMap.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                Log.d("comment 1 ", "onClick: ");
                processVisualization();
                Log.d("comment 2", "onClick: ");
                Intent i = new Intent(SpadeResultActivity.this, VisualisasiActivity.class);
                int x = 1;
                i.putExtra("resultObjectFreq", (Serializable) frequentVisualizationFix);
                i.putExtra("menuIdentifier",x);
                Log.d("comment 3", "onClick: ");
                startActivity(i);
                Log.d("comment 4", "onClick: ");
            }
        });
    }

    private class GetSpadeResult extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(SpadeResultActivity.this);
            pDialog.setMessage("Getting Result...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(state==1){
                File file = new File(Environment.getExternalStorageDirectory()+"/dbSimpantas/transform/","hasil-transform-data.csv");
                CSVReader reader = null;
                try {
                    reader = new CSVReader(new FileReader(file.getAbsolutePath()));
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        String[] unixArray = nextLine[0].split(" ");
                        char total = nextLine[0].charAt(nextLine[0].length() - 1);
                        resultList.clear();
                        for (String unixTimestamp : unixArray) {
                            if (!unixTimestamp.equals("-1") && isNumeric(unixTimestamp)) {
                                long unixSeconds = Long.parseLong(unixTimestamp);
                                System.out.println(unixSeconds);
                                if(unixSeconds > 100){
                                    Date date = new Date(unixSeconds * 1000L);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                    String formattedDate = dateFormat.format(date);
                                    resultList.add(formattedDate);
                                }
                            }
                        }

                        StringBuilder resultBuilder = new StringBuilder();
                        resultBuilder.append("Kemunculan pada tanggal ");
                        for (int i = 0; i < resultList.size(); i++) {
                            resultBuilder.append(resultList.get(i));
                            if (i != resultList.size() - 1) {
                                resultBuilder.append(", ");
                            }
                        }
                        resultBuilder.append(" di ").append(total).append(" titik");
                        HashMap<String, String> res = new HashMap<>();
                        String resultString = resultBuilder.toString();
                        res.put("result", resultString);
                        finalList.add(res);

                        HashMap<String, String> spades = new HashMap<>();
                        spades.put("unixdatetime",nextLine[0]);
                        Log.d("RESULT TERLIHAT? ",nextLine[0]);
                        spades.put("month",bulanValue);
                        spades.put("year",tahunValue);
                        spadeList.add(spades);
                        db.insertTspade(spades);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(state==2){
                spadeList = db.getTspadeByDate(bulanValue,tahunValue);
                Log.d("Find result",spadeList.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            ListAdapter adapter = new SimpleAdapter(
                    SpadeResultActivity.this, finalList,
                    R.layout.list_spade, new String[]{"result"}, new int[]{R.id.spade_list});
            lv.setAdapter(adapter);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void processVisualization(){
        List<String> newUnix0 = new ArrayList<String>();
        List<String> newUnix = new ArrayList<String>();
        List<TvFrequent> frequentVisualization = new ArrayList<>();

        for (int i=0;i<spadeList.size();i++) {
            HashMap<String, String> hashmap= spadeList.get(i);
            String recentUnix = hashmap.get("unixdatetime");
            Log.d("Unix Value before", recentUnix);
            if(recentUnix.length() == 21 || recentUnix.length() == 22){
                String strTemp = recentUnix.substring(0,10);
                newUnix0.add(strTemp);
            }else if(recentUnix.length() == 35 || recentUnix.length() == 36){
                String strTemp1 = recentUnix.substring(0,10);
                String strTemp2 = recentUnix.substring(14,24);
                String joinUnix = strTemp1 + "," + strTemp2;
                newUnix.add(joinUnix);
            }
        }
        if(newUnix.isEmpty()){
            for(int j=0; j<newUnix0.size(); j++){
                Log.d("VALUE GET", newUnix0.get(j));
                frequentVisualizationFix.addAll(db.getCoordinatesByResultTspade(newUnix0.get(j)));
            }
            db.insertTvFrequentDate(frequentVisualizationFix);
        }else{
            for(int j=0;j<newUnix.size();j++){
                if(newUnix.get(j).length()==10){
//                Log.d("VALUE GET ", newUnix.get(j));
//                frequentVisualization.addAll(db.getCoordinatesByResultTspade(newUnix.get(j)));
                }else if(newUnix.get(j).length()==21){
                    String[] str = newUnix.get(j).split(",");
                    frequentVisualization.addAll(db.getCoordinatesByResultTspadeWith2Input(str[0],str[1]));
                    for (int i=0; i<frequentVisualization.size(); i++){
                        if (i==0) continue;
                        if ((frequentVisualization.get(i-1).getLatitude()==frequentVisualization.get(i).getLatitude())&&(frequentVisualization.get(i-1).getLongitude()==frequentVisualization.get(i).getLongitude())){
                            frequentVisualization.get(i).setTanggal2(frequentVisualization.get(i-1).getTanggal1());
                            frequentVisualizationFix.add(frequentVisualization.get(i));
                        }else{

                        }
                    }
                    frequentVisualization.clear();
                }
            }
            db.insertTvFrequentDate(frequentVisualizationFix);
        }
    }


}
