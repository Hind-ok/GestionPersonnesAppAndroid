package com.example.gestionpersonnes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PersonDetailFragment extends AppCompatActivity {
    private TextView personName, personPhone;
    private Button callButton, smsButton;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_person_detail);

        personName = findViewById(R.id.personName);
        personPhone = findViewById(R.id.personPhone);
        callButton = findViewById(R.id.callButton);
        smsButton = findViewById(R.id.smsButton);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("name") && intent.hasExtra("phone")) {
            personName.setText(intent.getStringExtra("name"));
            phoneNumber = intent.getStringExtra("phone");
            personPhone.setText(phoneNumber);
        }

        callButton.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        });

        smsButton.setOnClickListener(v -> {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", "Bonjour !");
            startActivity(smsIntent);
        });
    }
}
