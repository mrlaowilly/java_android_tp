package com.example.background_tp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.background_tp.restClient.AsyncRestClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // initialize variable
    Button btLocation;
    TextView textView1, textView2, textView3, textView4, textView5;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign variable
        btLocation = findViewById(R.id.bt_location);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        textView4 = findViewById(R.id.text_view4);
        textView5 = findViewById(R.id.text_view5);

        // Initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //When permission granted
                    getLocation();
                } else {
                    //When permission denied
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });
        findViewById(R.id.btnCallApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLocation();
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location
                Location location = task.getResult();
                if (location != null) {
                    try {
                        //Initialize geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        //Initialize address list
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        //Set Latitude on TextView
                        textView1.setText(Html.fromHtml("<font color='#6200EE'><b>Latitude : </b><br></font>"
                                + addresses.get(0).getLatitude()
                        ));
                        //Set Longitude
                        textView2.setText(Html.fromHtml("<font color='#6200EE'><b>Longitude : </b><br></font>"
                                + addresses.get(0).getLongitude()
                        ));
                        //Set country name
                        textView3.setText(Html.fromHtml("<font color='#6200EE'><b>Country name : </b><br></font>"
                                + addresses.get(0).getCountryName()
                        ));
                        //Set Locality
                        textView4.setText(Html.fromHtml("<font color='#6200EE'><b>Locality : </b><br></font>"
                                + addresses.get(0).getLocality()
                        ));
                        //Set address
                        textView5.setText(Html.fromHtml("<font color='#6200EE'><b>Address : </b><br></font>"
                                + addresses.get(0).getAddressLine(0)
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void shareLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location
                Location location = task.getResult();
                if (location != null) {
                    try {
                        //Initialize geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        //Initialize address list
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        findViewById(R.id.btnCallApi).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String lat = String.valueOf(location.getLatitude());
                                String lng = String.valueOf(location.getLongitude());
                                Log.e("ooo", String.valueOf(lat));
                                AsyncRestClient arc = new AsyncRestClient(getApplicationContext(), findViewById(R.id.progressBar));
                                arc.execute(
                                        new Pair<String, String>("HTTP_METHOD", "GET"),
                                        new Pair<String, String>("HTTP_URL", "https://api.sunrise-sunset.org/json"),
                                        new Pair<String, String>("lat", lat),
                                        new Pair<String, String>("lng", lng),
                                        new Pair<String, String>("formatted", "1"),
                                        new Pair<String, String>("date", "today")
                                );
                                arc.setOnReceiveDataListener(new AsyncRestClient.OnReceiveDataListener() {
                                    @Override
                                    public void onReceiveData(JSONObject jsonObject) {
                                        Log.e(">>>>>", jsonObject.toString());
                                        try {
                                            ((TextView) findViewById(R.id.textViewSunrise)).setText(jsonObject.getJSONObject("results").getString("sunrise"));
                                            ((TextView) findViewById(R.id.textViewSunset)).setText(jsonObject.getJSONObject("results").getString("sunset"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}