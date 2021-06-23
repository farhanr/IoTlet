package com.application.iotlet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IotletService extends Service {
    FirebaseDatabase database;
    DatabaseReference suhuRef;
    DatabaseReference kelembabanRef;

    double suhu;
    double kelembaban;

    NotificationChannel notificationChannel;
    NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, "CHANNEL_IOTLET")
                .setContentText("IoT-let realtime updater is running")
                .setSmallIcon(R.drawable.iotlet_logo_purple)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        database = FirebaseDatabase.getInstance();
        suhuRef = database.getReference("suhu");
        kelembabanRef = database.getReference("kelembaban");

        suhuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suhu = snapshot.getValue(Double.class);
                if(suhu >= 45){
                    Notification notifTemperatureMax =
                            new Notification.Builder(IotletService.this, "CHANNEL_IOTLET")
                                    .setContentTitle("Peringatan!")
                                    .setSmallIcon(R.drawable.iotlet_logo_purple)
                                    .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iotlet_logo_purple), 256, 256, false))
                                    .setStyle(new Notification.BigTextStyle()
                                            .bigText("Suhu di rumah walet melebihi batas maksimum! "+
                                                    "Apakah anda ingin mengaktifkan Water Pump?"))
                                    .setContentIntent(pendingIntent).build();
                    manager.notify(2, notifTemperatureMax);
                } else if (suhu > 26) {
                    Notification notifTemperature =
                            new Notification.Builder(IotletService.this, "CHANNEL_IOTLET")
                                    .setContentTitle("Peringatan!")
                                    .setSmallIcon(R.drawable.iotlet_logo_purple)
                                    .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iotlet_logo_purple), 256, 256, false))
                                    .setStyle(new Notification.BigTextStyle()
                                            .bigText("Suhu di rumah walet telah mencapai "+ suhu +
                                                    " °C! Apakah anda ingin mengaktifkan Water Pump?"))
                                    .setContentIntent(pendingIntent).build();
                    manager.notify(2, notifTemperature);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return Service.START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = new NotificationChannel(
                    "CHANNEL_IOTLET", "Foreground Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
                    );
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();

    }
}

