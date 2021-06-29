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
    DatabaseReference relayRef;


    double suhu;
    boolean relayOn;

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
        relayRef = database.getReference("relay_on");

        suhuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suhu = snapshot.getValue(Double.class);
                if(suhu >= 30) {
                    if (!relayOn) {
                        relayRef.setValue(true);
                        relayOn = true;
                    }
                    Notification notifTemperatureMax =
                            new Notification.Builder(IotletService.this, "CHANNEL_IOTLET")
                                    .setContentTitle("Peringatan!!!")
                                    .setSmallIcon(R.drawable.iotlet_logo_purple)
                                    .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iotlet_logo_purple), 256, 256, false))
                                    .setStyle(new Notification.BigTextStyle()
                                            .bigText("Suhu di Rumah Walet Anda Telah Mencapai 30°C " +
                                                    "Dan Kelembaban Tidak Mencapai 80%! " +
                                                    "Water pump telah dinyalakan secara otomatis!"))
                                    .setContentIntent(pendingIntent).build();
                    manager.notify(2, notifTemperatureMax);
                } else if (suhu < 30 && relayOn) {
                    relayRef.setValue(false);
                    relayOn = false;
                } else if (suhu >= 26) {
                    Notification notifTemperature =
                            new Notification.Builder(IotletService.this, "CHANNEL_IOTLET")
                                    .setContentTitle("Peringatan!")
                                    .setSmallIcon(R.drawable.iotlet_logo_purple)
                                    .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iotlet_logo_purple), 256, 256, false))
                                    .setStyle(new Notification.BigTextStyle()
                                            .bigText("Terjadi peningkatan suhu dan penurunan kelembaban di rumah walet anda! "+
                                                    "Disarankan untuk mengaktifkan water pump!"))
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

