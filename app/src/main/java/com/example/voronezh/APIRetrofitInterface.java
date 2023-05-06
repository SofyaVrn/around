package com.example.voronezh;


import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface APIRetrofitInterface {

    @GET("/server.php?")
    Call<ImageObjectList> getImageObjectList(@Query("object_id") long object_id);

    @FormUrlEncoded
    @POST("/getpreview.php?")
    Call<PreviewObjects> getPreviewObjects(@Field("objects_id[]") ArrayList<String> objects_id);
}
