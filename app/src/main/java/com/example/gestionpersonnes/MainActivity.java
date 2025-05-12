package com.example.gestionpersonnes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText name, dob, phone;
    private Button addPerson, captureButton, locationButton, mapsButton;
    private ImageView capturedImage;
    private ListView listView;
    private DatabaseHelper databaseHelper;
    private ArrayList<String> personsList;
    private ArrayAdapter<String> adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText;

    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private static final int CAMERA_PERMISSION_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        name = findViewById(R.id.name);
        dob = findViewById(R.id.dob);
        phone = findViewById(R.id.phone);
        addPerson = findViewById(R.id.addPerson);
        listView = findViewById(R.id.listView);
        locationButton = findViewById(R.id.getLocationButton);
        mapsButton = findViewById(R.id.mapsButton);
        locationText = findViewById(R.id.locationText);

        databaseHelper = new DatabaseHelper(this);
        personsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personsList);
        listView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadPersons();

        addPerson.setOnClickListener(v -> {
            String personName = name.getText().toString().trim();
            String personDob = dob.getText().toString().trim();
            String personPhone = phone.getText().toString().trim();

            if (personName.isEmpty() || personDob.isEmpty() || personPhone.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.addPerson(personName, personDob, personPhone);
                loadPersons();
                name.setText("");
                dob.setText("");
                phone.setText("");
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPerson = personsList.get(position);
            Cursor cursor = databaseHelper.getPersonByName(selectedPerson);

            if (cursor.moveToFirst()) {
                String personName = cursor.getString(1);
                String personPhone = cursor.getString(3);

                Intent intent = new Intent(MainActivity.this, PersonDetailFragment.class);
                intent.putExtra("name", personName);
                intent.putExtra("phone", personPhone);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Erreur : personne non trouvée", Toast.LENGTH_SHORT).show();
            }
        });


        locationButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            } else {
                getLocation();
            }
        });

        mapsButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
        });
    }

    private void loadPersons() {
        personsList.clear();
        Cursor cursor = databaseHelper.getPersons();
        while (cursor.moveToNext()) {
            personsList.add(cursor.getString(1));
        }
        adapter.notifyDataSetChanged();
    }


    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                locationText.setText("Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
            } else {
                locationText.setText("Localisation non disponible");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
