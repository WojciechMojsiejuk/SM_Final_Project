package com.example.cameraxopencv.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "photo")
public class Photo {
    public Photo(String filepath, Date creationDate, int a, int b, int c, int d)
    {
        this.filepath = filepath;
        this.creationDate = creationDate;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String filepath;
    private Date creationDate;
    private int a;
    private int b;
    private int c;
    private int d;

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


    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }
}
