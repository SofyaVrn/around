package com.example.voronezh;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class PreviewObjects {
    @SerializedName("objects")
    public List<PreviewList> objects = null;

    public class PreviewList {

        @SerializedName("object_id")
        public long id;
        @SerializedName("path")
        public String path;
    }

}
