package com.example.voronezh;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

public class MainApplication extends Application {
    private final String MAPKIT_API_KEY = "f0f2e1b2-28a8-49f5-a12f-fb3164feec22";


    @Override
    public void onCreate() {
        super.onCreate();
        // Set the api key before calling initialize on MapKitFactory.
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
    }
}
