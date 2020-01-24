package com.example.cameraxopencv.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoRepository {
    private PhotoDao photoDao;
    private LiveData<List<Photo>> photos;

    PhotoRepository(Application application)
    {
        PhotoDatabase database = PhotoDatabase.getDatabase(application);
        photoDao = database.photoDao();
        photos = photoDao.findAll();
    }

    LiveData<List<Photo>> findAllPhotos()
    {
        return photos;
    }

    void insert(Photo photo)
    {
       PhotoDatabase.databaseWriteExecutor.execute(() -> {
            photoDao.insert(photo);});
    }

    void update(Photo photo)
    {
        PhotoDatabase.databaseWriteExecutor.execute(() -> {
            photoDao.update(photo);});
    }

    void delete(Photo photo)
    {
        PhotoDatabase.databaseWriteExecutor.execute(() -> {
            photoDao.delete(photo);});
    }
}
