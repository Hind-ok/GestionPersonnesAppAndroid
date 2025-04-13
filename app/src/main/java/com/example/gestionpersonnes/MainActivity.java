package com.example.gestionpersonnes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
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
import com.google.android.gms.tasks.OnSuccessListener;
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
        capturedImage = findViewById(R.id.capturedImage);
        captureButton = findViewById(R.id.captureButton);
        locationButton = findViewById(R.id.getLocationButton);
        mapsButton = findViewById(R.id.mapsButton);
        locationText = findViewById(R.id.locationText);

        // Base de données
        databaseHelper = new DatabaseHelper(this);
        personsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, personsList);
        listView.setAdapter(adapter);

        // Localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Charger les personnes
        loadPersons();

        // Ajout d'une personne
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

        // Sélection d'une personne pour voir ses détails
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPerson = personsList.get(position);
            Cursor cursor = databaseHelper.getPersonByName(selectedPerson);

            if (cursor.moveToFirst()) {
                String personName = cursor.getString(1);
                String personPhone = cursor.getString(3);

                Log.d("DEBUG", "Nom: " + personName + ", Téléphone: " + personPhone);

                Intent intent = new Intent(MainActivity.this, PersonDetailFragment.class);
                intent.putExtra("name", personName);
                intent.putExtra("phone", personPhone);
                startActivity(intent);
            } else {
                Log.e("ERROR", "Aucune personne trouvée avec le nom : " + selectedPerson);
                Toast.makeText(this, "Erreur : personne non trouvée", Toast.LENGTH_SHORT).show();
            }
        });

        // Capture d'image avec la caméra
        captureButton.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        });

        // Obtenir la localisation
        locationButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            } else {
                getLocation();
            }
        });

        // Ouvrir Google Maps
        mapsButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
        });
    }

    // Charger les personnes depuis la base de données
    private void loadPersons() {
        personsList.clear();
        Cursor cursor = databaseHelper.getPersons();
        while (cursor.moveToNext()) {
            personsList.add(cursor.getString(1));  // Nom de la personne
        }
        adapter.notifyDataSetChanged();
    }

    // Gérer la capture d'image
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    capturedImage.setImageBitmap(photo);
                }
            });

    // Obtenir la localisation actuelle
    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                locationText.setText("Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
            }
        });
    }

    // Gérer la permission de localisation
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }
}
