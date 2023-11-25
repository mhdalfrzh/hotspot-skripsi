package com.example.admin.simpantas;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class VisualisasiCluster extends FragmentActivity implements OnMapReadyCallback{
    private GoogleMap mMap;
    private List<Cluster> clusterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualisasi_cluster);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DBHelper db = new DBHelper(this);
        clusterList = db.getAllClusters();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Tambahkan marker pada peta untuk setiap data dalam clusterList
        for (Cluster cluster : clusterList) {
            LatLng location = new LatLng(cluster.getLatitude(), cluster.getLongitude());
            int clusterNumber = cluster.getCluster();
            MarkerOptions markerOptions = new MarkerOptions().position(location).title("Cluster " + clusterNumber).snippet("Instance : " + cluster.getInstance() + ", " + "");
            // Berikan warna marker yang berbeda untuk setiap nomor cluster yang berbeda
            switch (clusterNumber) {
                case 1:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    break;
                case 2:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    break;
                case 3:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    break;
                case 4:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    break;
                case 5:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    break;
                case 6:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    break;
                case 7:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    break;
                case 8:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    break;
                case 9:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    break;
                case 10:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    break;
                default:
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    break;
            }
            mMap.addMarker(markerOptions);
        }

        // Zoom peta agar seluruh marker terlihat
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Cluster cluster : clusterList) {
            LatLng location = new LatLng(cluster.getLatitude(), cluster.getLongitude());
            builder.include(location);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
}
