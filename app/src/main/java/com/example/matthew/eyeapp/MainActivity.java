package com.example.matthew.eyeapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    RequestQueue queue;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    ArrayList filters = new ArrayList();
    ScanFilter filter;
    ScanSettings settings;
    String speed = "0";
    LocationManager locationManager;
    LocationListener locationListener;

    boolean newData = false;

    TextView text;
    TextView speedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView)findViewById(R.id.textbox);
        speedText = (TextView)findViewById(R.id.textspeed);

        init();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                location.getLatitude();
                speed = String.valueOf(location.getSpeed() * 2.23694);
                speedText.setText(speed);
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
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch(SecurityException e){

        }
    }

    protected void init(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        filter = new ScanFilter.Builder().setDeviceAddress("00:19:86:00:16:B8").build();
        filters.add(filter);

        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        settings = mBuilder.build();
        queue = Volley.newRequestQueue(this);
    }

    protected ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord mScanRecord = result.getScanRecord();
            byte[] b = mScanRecord.getBytes();
            text.setText(byteArrayToHex(b));
            if(b[8] == 0x77 && newData == false) {
                String url = "http://ctmusall.pythonanywhere.com/gather/";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                            }
                        }
                ) {


                    // SPEED AND DIRECTION HERE //

                    @Override
                    protected Map<String, String> getParams() {
                        String tempSpeed;

                        tempSpeed = speed;

                        Random r = new Random();
                        int dir = r.nextInt(4);
                        String dirStr;
                        switch(dir){
                            case 0:
                                dirStr = "down";
                                break;
                            case 1:
                                dirStr = "up";
                                break;
                            case 2:
                                dirStr = "left";
                                break;
                            case 3:
                                dirStr = "right";
                                break;
                            default:
                                dirStr = "up";
                                break;
                        }

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user_name", "clayton");
                        params.put("location", "Evansville");
                        params.put("speed", tempSpeed);
                        params.put("gaze", dirStr);
                        params.put("incident", "yes");
                        params.put("submit", "POST!");
                        return params;
                    }
                };
                queue.add(postRequest);
                newData = true;
            }
            else if(b[8] == 0x11){
                newData = false;
            }
        }
    };

    protected String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x ", b & 0xff));
        return sb.toString();
    }

}
