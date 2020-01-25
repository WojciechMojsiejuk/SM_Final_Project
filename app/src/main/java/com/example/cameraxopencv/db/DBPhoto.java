package com.example.cameraxopencv.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "photo")
public class DBPhoto{
    public DBPhoto(String filepath, Date creationDate)
    {
        this.filepath = filepath;
        this.creationDate = creationDate;
    }
    @PrimaryKey(autoGenerate = true)
    private Integer id;
    private String filepath;
    private Date creationDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
