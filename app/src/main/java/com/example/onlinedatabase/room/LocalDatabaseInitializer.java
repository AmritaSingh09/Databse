package com.example.onlinedatabase.room;

import android.content.Context;

import androidx.room.Room;

public class LocalDatabaseInitializer {
    static Context context;
    static LocalDatabaseInitializer localDatabaseInitializer;

    public static LocalDatabaseInitializer getInstance(Context context){
        if (localDatabaseInitializer == null){
            localDatabaseInitializer = new LocalDatabaseInitializer();
        }
        LocalDatabaseInitializer.context = context;
        return localDatabaseInitializer;

    }

    public FetchDao initializeDatabase() {
        LocalDatabase localDatabase = Room.databaseBuilder(context,LocalDatabase.class,"database-name").build();
        return localDatabase.fetchDao();
    }
}
