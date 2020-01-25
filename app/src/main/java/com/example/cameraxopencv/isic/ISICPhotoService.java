package com.example.cameraxopencv.isic;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ISICPhotoService {
    @GET("image/")
Call<ISICPhotoContainer> getPhotos(@Query("limit") String limit);
}
