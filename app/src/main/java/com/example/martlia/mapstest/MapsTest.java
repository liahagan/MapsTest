package com.example.martlia.mapstest;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsTest extends FragmentActivity implements OnMapReadyCallback{

    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final PatternItem DOT = new Dot();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_test);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Not using this part, because everything is in KML
        /*HashMap<String, LatLng[]> pools = coordinates();
        Polyline pl;

        for (String pool_name: pools.keySet()) {
            pl = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(
                            pools.get(pool_name)[0],
                            pools.get(pool_name)[1],
                            pools.get(pool_name)[2]));
            pl.setTag(pool_name);
        }*/

        //Fetch data from KML file and add it to the map
        retrieveFileFromResource();

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(62.9796377911109, 8.74903160512098), 10));

    }

    private void retrieveFileFromResource() {
        try {
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.map, this);
            kmlLayer.addLayerToMap();

            // Set a listener for geometry clicked events.
            kmlLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                @Override
                public void onFeatureClick(Feature feature) {
                    Toast.makeText(MapsTest.this,
                            "Feature clicked: " + feature.getId(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            moveCameraToKml(kmlLayer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void moveCameraToKml(KmlLayer kmlLayer) {
        //Retrieve the first container in the KML layer
        KmlContainer container = kmlLayer.getContainers().iterator().next();
        //Retrieve a nested container within the first container
        container = container.getContainers().iterator().next();
        //Retrieve the first placemark in the nested container
        KmlPlacemark placemark = container.getPlacemarks().iterator().next();
        //Retrieve a polygon object in a placemark
        KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
        //Create LatLngBounds of the outer coordinates of the polygon
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polygon.getOuterBoundaryCoordinates()) {
            builder.include(latLng);
        }

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 1));
    }

    /*
    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if (polyline.getColor() == COLOR_BLACK_ARGB) {
            polyline.setColor(COLOR_GREEN_ARGB);
        } else {
            polyline.setColor(COLOR_BLACK_ARGB);
        }

        Toast.makeText(this, polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }*/


    public HashMap<String, LatLng[]> coordinates() {
        // zone;pool;beg_lat;beg_lon;mid_lat;mid_lon;end_lat;end_lon

        String csvFile = "coordinates.csv";
        String line = "";
        String cvsSplitBy = ";";
        HashMap<String, LatLng[]> pools = new HashMap<String, LatLng[]>();

        AssetManager am = this.getAssets();

        try {
            InputStream is = am.open(csvFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            //Skipping the first line
            line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] pool = line.split(cvsSplitBy);
                pools.put(pool[1],new LatLng[]{
                        new LatLng(Double.parseDouble(pool[2]), Double.parseDouble(pool[3])),
                        new LatLng(Double.parseDouble(pool[4]), Double.parseDouble(pool[5])),
                        new LatLng(Double.parseDouble(pool[6]), Double.parseDouble(pool[7]))});
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return pools;
    }
}
