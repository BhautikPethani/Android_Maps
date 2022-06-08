package com.example.lab1_bhautikpethani_c0854487_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lab1_bhautikpethani_c0854487_android.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.BubbleIconFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    Polygon polygon;

    List<Marker> markers = new ArrayList();
    List<Polyline> polylines = new ArrayList();
    int counter = 0;

    ArrayList<String> zone = new ArrayList<String>(4);

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LatLng northAmerica = new LatLng(54, -105);
        setHomeMarker(northAmerica);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if(counter <= 3) {
                    setPolygoneMarker(latLng);
                    if(counter == 3) {
                        drawPolygon();
                        drawPolyline(markers.get(0), markers.get(markers.size() - 1));
                    }
                    counter ++;
                }else{
                    setGeneralMarker(latLng);
                }
            }

            private void setPolygoneMarker(LatLng latLng) {

                String zoneName = "";

                if(!zone.contains("A")){
                    zoneName = "A";
                }else if(!zone.contains("B")){
                    zoneName = "B";
                }else if(!zone.contains("C")){
                    zoneName = "C";
                }else if(!zone.contains("D")){
                    zoneName = "D";
                }

                MarkerOptions options = new MarkerOptions().position(latLng)
                        .title(zoneName);
                if(counter>0) {
                    double distance = calculationByDistance(latLng, markers.get(markers.size() - 1).getPosition());
                    options.snippet("Distance ( " + markers.get(markers.size() - 1).getTitle() + " to " + zoneName + ") : " + String.format("%.2f", distance) + " KM");
                }
                Marker temp = mMap.addMarker(options);
                temp.showInfoWindow();
                if(counter>0){
                    drawPolyline(markers.get(markers.size()-1), temp);
                }
                markers.add(temp);
                zone.add(zoneName);
            }

            private void setGeneralMarker(LatLng latLng) {
                MarkerOptions options = new MarkerOptions().position(latLng).title("Destination");
                Marker temp = mMap.addMarker(options);
            }

            private void drawPolygon() {
                PolygonOptions options = new PolygonOptions()
                        .fillColor((Color.argb(100, 3, 255, 70)));

                for (int i = 0; i < 4; i++) {
                    options.add(markers.get(i).getPosition());
                }

                polygon = mMap.addPolygon(options);
                polygon.setClickable(true);
            }

            private void drawPolyline(Marker source, Marker destination){
                PolylineOptions options = new PolylineOptions()
                        .color(Color.RED)
                        .width(10)
                        .add(source.getPosition(), destination.getPosition());
                Polyline temp = mMap.addPolyline(options);
                double distance = calculationByDistance(source.getPosition(), destination.getPosition());
                temp.setTag(String.valueOf(distance));
                polylines.add(temp);
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                //Log.d("Polygone click","Clicked");
                removeEverything();
            }

            private void removeEverything(){
                polygon.remove();
                for(Polyline temp : polylines){
                    temp.remove();
                }
                for(Marker temp : markers){
                    temp.remove();
                }
                markers.removeAll(markers);
                polylines.remove(polylines);
                counter = 0;
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if(markers.contains(marker)) {
                    markers.remove(marker);
                    String title = marker.getTitle();
                    zone.remove(title);
                    counter-=1;
                    removeAllPolyline();
                    marker.remove();

                    if(polygon != null)
                        polygon.remove();
                    addAllPolyline();
                }else{
                    if(!marker.getTitle().equals("NORTH AMERICA")){
                        marker.remove();
                    }
                }
                return true;
            }

            private void removeAllPolyline(){
                for(Polyline temp : polylines){
                    temp.remove();
                }
                polylines.removeAll(polylines);
            }

            private void addAllPolyline(){
                if(counter > 0){
                    for(int i=1; i<counter; i++){
                        drawPolyline(markers.get(i), markers.get(i-1));
                    }
                }
            }

            private void drawPolyline(Marker source, Marker destination){
                PolylineOptions options = new PolylineOptions()
                        .color(Color.RED)
                        .width(10)
                        .clickable(true)
                        .add(source.getPosition(), destination.getPosition());
                Polyline temp = mMap.addPolyline(options);
                double distance = calculationByDistance(source.getPosition(), destination.getPosition());
                temp.setTag(String.valueOf(distance));
                polylines.add(temp);
            }
        });


    }

    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(LatLng currentLocation) {
        LatLng userLocation = currentLocation;
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("NORTH AMERICA")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        homeMarker = mMap.addMarker(options);
        homeMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }
}