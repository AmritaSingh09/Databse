package com.example.onlinedatabase.networkManger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.onlinedatabase.MainActivity;
import com.example.onlinedatabase.R;
import com.example.onlinedatabase.room.FetchDao;
import com.example.onlinedatabase.room.LocalDatabaseInitializer;
import com.example.onlinedatabase.room.Metadata;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FileUploadService extends Service {

    FetchDao fetchDao;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fetchDao = LocalDatabaseInitializer.getInstance(this).initializeDatabase();

        Intent notificatioIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificatioIntent,PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(this,"File Channel")//same as channel
                .setContentTitle("Uploading")
                .setContentText("Files being uploading...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("File Channel", "CH1",NotificationManager.IMPORTANCE_MIN);
        manager.createNotificationChannel(channel);

        startForeground(1,notification);


        UploadToServer();

        return Service.START_STICKY;
    }

    public synchronized void UploadToServer(){
        AsyncTask.execute(()-> {
            List<Metadata> list = fetchDao.getNotSyncedData();
            for (Metadata metadata : list) {
                StorageReference ref = FirebaseStorage
                        .getInstance()
                        .getReference()
                        .child(metadata.getUrl());


                ref.putBytes(metadata.getFile())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                metadata.setSync(true);
                                Toast.makeText(this, "File uploaded!", Toast.LENGTH_SHORT).show();
                                Log.d("UPLOAD_STATUS: ", "File uploaded!");
                            } else {
                                metadata.setSync(false);
                                Toast.makeText(this, "File Not Uploaded!", Toast.LENGTH_SHORT).show();
                                Log.e("UPLOAD_STATUS: ", "File uploaded!");
                                stopForeground(true);
                            }
                            AsyncTask.execute(() -> {
                                fetchDao.update(metadata);
                            });

                        });

            }


        });
    }


    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
