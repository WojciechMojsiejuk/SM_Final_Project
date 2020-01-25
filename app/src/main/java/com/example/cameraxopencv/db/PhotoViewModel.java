package com.example.cameraxopencv.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PhotoViewModel extends AndroidViewModel {
    private PhotoRepository photoRepository;

    private LiveData<List<DBPhoto>> photos;

    public PhotoViewModel(@NonNull Application application)
    {
        super(application);
        photoRepository = new PhotoRepository(application);
        photos = photoRepository.findAllPhotos();
    }

    public LiveData<List<DBPhoto>> findAll()
    {
        return photos;
    }

    public void insert(DBPhoto photo)
    {
        photoRepository.insert(photo);
    }

    public void update(DBPhoto photo)
    {
        photoRepository.update(photo);
    }
    public void delete(DBPhoto photo)
    {
        photoRepository.delete(photo);
    }
}
