package com.example.cameraxopencv.isic;

import com.google.gson.annotations.SerializedName;

public class ISICPhoto {
    @SerializedName("_id")
    private String id;
    @SerializedName("updated")
    private String creationDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
