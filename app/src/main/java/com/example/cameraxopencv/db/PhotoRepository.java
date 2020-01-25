package com.example.cameraxopencv.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoRepository {
    private PhotoDao photoDao;
    private LiveData<List<DBPhoto>> photos;

    PhotoRepository(Application application)
    {
        PhotoDatabase database = PhotoDatabase.getDatabase(application);
        photoDao = database.photoDao();
        photos = photoDao.findAll();
    }

    LiveData<List<DBPhoto>> findAllPhotos()
    {
        return photos;
    }

    void insert(DBPhoto photo)
    {
       PhotoDatabase.databaseWriteExecutor.execute(() -> {
            photoDao.insert(photo);});
    }

    void update(DBPhoto photo)
    {
        PhotoDatabase.databaseWriteExecutor.execute(() -> {
            photoDao.update(photo);});
    }

    void delete(DBPhoto photo)
    {
        PhotoDatabase.databaseWriteExecutor.execute(() -> {
            photoDao.delete(photo);});
    }
}
