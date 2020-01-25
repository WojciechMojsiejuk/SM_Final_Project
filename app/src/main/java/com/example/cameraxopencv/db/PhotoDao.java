package com.example.cameraxopencv.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DBPhoto photo);

    @Update
    public void update(DBPhoto photo);

    @Delete
    public void delete(DBPhoto photo);

    @Query("DELETE FROM DBPhoto")
    public void deleteAll();

    @Query("SELECT * FROM DBPhoto ORDER BY creationDate")
    public LiveData<List<DBPhoto>> findAll();

}
