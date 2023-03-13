package com.example.onlinedatabase.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FetchDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void put(Metadata metadata);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Metadata metadata);

    @Query("select * From metadata")
    List<Metadata> getAll();

    @Query("select * From metadata where isUploaded = 0")
    List<Metadata> getNotSyncedData();
}
