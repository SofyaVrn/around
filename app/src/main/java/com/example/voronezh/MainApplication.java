package com.example.voronezh;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

public class MainApplication extends Application {
    private final String MAPKIT_API_KEY = "";
    public static final boolean IMAGE_CACHING = true;


    @Override
    public void onCreate() {
        super.onCreate();
        // Set the api key before calling initialize on MapKitFactory.
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
    }
}
