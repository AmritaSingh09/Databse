package com.example.onlinedatabase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.onlinedatabase.networkManger.NetworkReceiver;
import com.example.onlinedatabase.room.FetchDao;
import com.example.onlinedatabase.room.LocalDatabaseInitializer;
import com.example.onlinedatabase.room.Metadata;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainActivity extends AppCompatActivity {

    private int PICKFILE_RESULT_CODE = 2002;
    private BroadcastReceiver networkStateReceiver;
    private RecyclerView rv_file;
    private CardView select;

    FetchDao fetchDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO file picker in android and upload to server
        rv_file = findViewById(R.id.rv_file);
        //sync = findViewById(R.id.sync);
        select = findViewById(R.id.select);

        select.setOnClickListener(vi->{
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        });

        rv_file.setLayoutManager(new GridLayoutManager(this,2));

        AsyncTask.execute(()->{
            fetchDao = LocalDatabaseInitializer.getInstance(MainActivity.this).initializeDatabase();
            rv_file.setAdapter(new FileAdapter(MainActivity.this,fetchDao.getAll()));
        });

        /*networkStateReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = manager.getActiveNetworkInfo();
                //doSomethingOnNetworkChange(ni);
            }
        };*/

        networkStateReceiver = new NetworkReceiver();
        registerNetworkBroadcastForNougat();
    }


    /*@Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        if (isNetWorkAvailable()){
            Toast.makeText(this, "Yes available", Toast.LENGTH_SHORT).show();
            //todo upload to server from room if available
        }
    }*/

    public static void dialog(boolean value){

        if(value){
            //tv_check_connection.setText("We are back !!!");
            //tv_check_connection.setBackgroundColor(Color.GREEN);
            //tv_check_connection.setTextColor(Color.BLACK);

            Handler handler = new Handler();
            Runnable delayrunnable = new Runnable() {
                @Override
                public void run() {
                    //tv_check_connection.setVisibility(View.GONE);
                }
            };
            handler.postDelayed(delayrunnable, 3000);
        }else {
            //tv_check_connection.setVisibility(View.VISIBLE);
            //tv_check_connection.setText("Could not Connect to internet");
            //tv_check_connection.setBackgroundColor(Color.RED);
            //tv_check_connection.setTextColor(Color.BLACK);
        }
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        String src = uri.getPath();
        File source = new File(src);
        String filename = uri.getLastPathSegment();
        File destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CustomFolder/" + filename);

        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = returnIntent.getData();
            try {
                mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "File not found.");
                return;
            }
            // Get a regular file descriptor for the file
            FileDescriptor fd = mInputPFD.getFileDescriptor();

        }
    }
*/

    private void copy(File source, File destination) throws IOException {

        FileChannel in = new FileInputStream(source).getChannel();
        FileChannel out = new FileOutputStream(destination).getChannel();

        try {
            in.transferTo(0, in.size(), out);
        } catch(Exception e){
            // post to log
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (out != null)
                out.close();
        }
    }

    public String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(networkStateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        AsyncTask.execute(()->{


        Uri uri = data.getData();
        String src = uri.getPath();
        byte[] bytes = new byte[0];
        String id = String.valueOf(System.currentTimeMillis());


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(uri.getPath()));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bytes = baos.toByteArray();
            ContentResolver cR = this.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String s = id+ "." +mime.getExtensionFromMimeType(cR.getType(uri));
        //String s = id  + src.substring(src.indexOf("."))+" ";
        if (isNetWorkAvailable()){
            FirebaseStorage.getInstance().getReference()
                    .child(s)
                    .putFile(uri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) Log.d("UPLOAD_STATUS: ", "File uploaded!");
                        else Log.e("UPLOAD_STATUS: ", "File uploaded!");
                    });

                    /*
                    task
                            .getResult()
                            .getStorage()
                            .getDownloadUrl()
                            .addOnSuccessListener(uri1 -> FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("Files")
                                    .setValue(Metadata.class)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) Log.d("UPLOAD_STATUS: ", "File uploaded!");
                                        else Log.e("UPLOAD_STATUS: ", "File uploaded!");
                                    })));
                     */
                fetchDao.put(new Metadata(id, s,
                        src.substring(src.lastIndexOf(".")+1),true,bytes));


        }else {
            fetchDao.put(new Metadata(id, s,
                    src.substring(src.lastIndexOf(".")+1),false,bytes));

        }


        });
    }

    private boolean isNetWorkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterNetworkChanges();
    }

}