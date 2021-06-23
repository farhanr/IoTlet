package com.application.iotlet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView textViewSuhu;
    TextView textViewKelembaban;
    SwitchMaterial switchRelay;

    boolean relayOn;
    double suhu;
    double kelembaban;

    FirebaseDatabase database;
    DatabaseReference relayRef;
    DatabaseReference suhuRef;
    DatabaseReference kelembabanRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i= new Intent(this, IotletService.class);
        i.putExtra("KEY1", "Value to be used by the service");
        startService(i);

        textViewSuhu = findViewById(R.id.tv_temperature);
        textViewKelembaban = findViewById(R.id.tv_humidity);
        switchRelay = findViewById(R.id.switch_relay);

        database = FirebaseDatabase.getInstance();
        relayRef = database.getReference("relay_on");
        suhuRef = database.getReference("suhu");
        kelembabanRef = database.getReference("kelembaban");

        suhuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suhu = snapshot.getValue(Double.class);
                textViewSuhu.setText(String.format("%.1f", suhu));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        kelembabanRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                kelembaban = snapshot.getValue(Double.class);
                textViewKelembaban.setText(String.format("%.1f", kelembaban));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        switchRelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                relayRef.setValue(isChecked);
            }
        });

        relayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relayOn = snapshot.getValue(Boolean.class);
                if (relayOn) {
                    switchRelay.setChecked(true);
                    Toast.makeText(MainActivity.this, "Water pump telah dinyalakan", Toast.LENGTH_SHORT).show();
                } else {
                    switchRelay.setChecked(false);
                    Toast.makeText(MainActivity.this, "Water pump telah dimatikan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}