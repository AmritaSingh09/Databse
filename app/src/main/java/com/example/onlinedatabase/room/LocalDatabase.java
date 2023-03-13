package com.example.onlinedatabase.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Metadata.class},  version = 4)
public abstract class LocalDatabase extends RoomDatabase {

    public abstract FetchDao fetchDao();

}
