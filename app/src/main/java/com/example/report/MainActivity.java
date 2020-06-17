package com.example.report;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button btnLogout, btnLocation, btnSubmit;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    private EditText edtName, edtMobile, edtNoOfSuspect, edtLocation;
    String userID, strLatitude, strLongitude;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogout = findViewById(R.id.btnlogout);
        btnLocation = findViewById(R.id.btnlocation);
        edtName = findViewById(R.id.edtName);
        edtMobile = findViewById(R.id.edtmobile);
        edtNoOfSuspect = findViewById(R.id.edtsuspect);
        edtLocation = findViewById(R.id.edtlocation);
        btnSubmit = findViewById(R.id.btnsubmit);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    getCurrentLocation();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getText().toString().trim();
                String mobile = edtMobile.getText().toString().trim();
                String suspect = edtNoOfSuspect.getText().toString().trim();
                String location = edtLocation.getText().toString().trim();

                if (name.isEmpty()) {
                    edtName.setError("Enter name");
                } else if (mobile.isEmpty() || mobile.length() < 10 || mobile.length() > 10) {
                    edtMobile.setError("Enter proper mobile number");
                } else if (suspect.isEmpty()) {
                    edtNoOfSuspect.setError("Enter suspect");
                } else {
                    DocumentReference documentReference = firestore.collection("users").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("mobile number", mobile);
                    user.put("No. of Suspect", suspect);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Reported successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        //progress bar start
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                    .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex  = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            strLatitude = latitude + "";
                            strLongitude = longitude + "";
                            Log.d("location", "onLocationResult: longitude :" + strLongitude + "latitude: " + strLatitude);
                        }
                    }
                    //progress bar stop
                }, Looper.getMainLooper());
    }
}
