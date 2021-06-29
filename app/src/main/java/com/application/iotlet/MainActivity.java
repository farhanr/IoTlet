package com.application.iotlet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
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
// PENGHUBUNG FIREBASE KE ANDROID STUDIO
public class MainActivity extends AppCompatActivity {
    TextView textViewSuhu;
    TextView textViewKelembaban;
    TextView textViewWaterPump;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(i);
        } else {
            startService(i);
        }

        textViewSuhu = findViewById(R.id.tv_temperature);
        textViewKelembaban = findViewById(R.id.tv_humidity);
        textViewWaterPump = findViewById(R.id.tv_waterpump);
        switchRelay = findViewById(R.id.switch_relay);


        database = FirebaseDatabase.getInstance(); // FARIABEL DATABASE
        relayRef = database.getReference("relay_on");
        suhuRef = database.getReference("suhu");
        kelembabanRef = database.getReference("kelembaban");

        suhuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // UPDATE NILAI SUHU PADA APLIKASI
                suhu = snapshot.getValue(Double.class);
                textViewSuhu.setText(String.format("%.1f", suhu));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        kelembabanRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // UPDATE NILAI KELEMBABAN PADA APLIKASI
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
                // SWITCH UNTUK RELAY ( TRUE DAN FLASE )
                relayRef.setValue(isChecked);
            }
        });

        relayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relayOn = snapshot.getValue(Boolean.class);
                // KETERANGAN NOTIFIKASI KECIL ON OFF RELAY YANG TERHUBUNG KE FIREBASE ( MEMBUAT NOTIF JIKA VARIABEL RELAY BERNILAI TRUE ATAU FALSE )
                if (relayOn) {
                    switchRelay.setChecked(true);
                    Toast.makeText(MainActivity.this, "Water pump is on", Toast.LENGTH_SHORT).show();
                    textViewWaterPump.setText("Water Pump: ON");
                } else {
                    switchRelay.setChecked(false);
                    Toast.makeText(MainActivity.this, "Water pump is off", Toast.LENGTH_SHORT).show();
                    textViewWaterPump.setText("Water Pump: OFF");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}