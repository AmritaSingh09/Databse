package com.example.onlinedatabase.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "metadata")
public class Metadata {

    public Metadata() {}
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String url;

    @ColumnInfo(name = "type")
    private String format;
    @ColumnInfo(name = "isUploaded")
    private Boolean sync;

    @ColumnInfo(name = "file" , typeAffinity = ColumnInfo.BLOB)
    private byte[] file;

    public Metadata(@NonNull String id, String url, String format, Boolean sync, byte[] file) {
        this.id = id;
        this.url = url;
        this.format = format;
        this.sync = sync;
        this.file = file;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
