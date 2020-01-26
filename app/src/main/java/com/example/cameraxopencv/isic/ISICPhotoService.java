package com.example.cameraxopencv.isic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ISICPhotoService {
    @GET("image/")
Call<List<ISICPhoto>> getPhotos(@Query("limit") String limit, @Query("offset") String offset);
}
