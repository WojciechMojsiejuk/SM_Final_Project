package com.example.cameraxopencv.isic;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ISICPhotoContainer {
    @SerializedName("updated")
    private List<ISICPhoto> isicPhotoList;

    public List<ISICPhoto> getIsicPhotoList() {
        return isicPhotoList;
    }

    public void setIsicPhotoList(List<ISICPhoto> isicPhotoList) {
        this.isicPhotoList = isicPhotoList;
    }
}
