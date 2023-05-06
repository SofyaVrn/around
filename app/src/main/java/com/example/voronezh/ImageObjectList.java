package com.example.voronezh;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
public class ImageObjectList {
    @SerializedName("images")
    public List<ImagesList> images = null;

    public class ImagesList {

        @SerializedName("id")
        public long id;
        @SerializedName("path")
        public String path;
    }

}
