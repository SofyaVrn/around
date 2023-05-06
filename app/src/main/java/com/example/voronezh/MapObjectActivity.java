package com.example.voronezh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.BoundingBoxHelper;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.FilterVehicleTypes;
import com.yandex.mapkit.transport.masstransit.MasstransitRouter;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.Section;
import com.yandex.mapkit.transport.masstransit.SectionMetadata;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import com.yandex.mapkit.transport.masstransit.TransitOptions;
import com.yandex.mapkit.transport.masstransit.Transport;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MapObjectActivity extends AppCompatActivity implements  DrivingSession.DrivingRouteListener,Session.RouteListener{
    private  Point ROUTE_START_LOCATION = null;
    private  Point ROUTE_END_LOCATION;
    private MapView mapview;

    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    PlacemarkMapObject markUserObject = null;
    private MapObjectCollection mapObjects = null;
    private DrivingRouter drivingRouter;
    private PedestrianRouter pedestrianRouter;
    private MasstransitRouter masstransitRouter;
    private Session pedestrianSession;
    private Session masstransitSession;
    private DrivingSession drivingSession;
    private boolean isVisibilityButtonsRoute = false;
    private boolean isMasstransitRouter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_map_object);

        Bundle arguments = getIntent().getExtras();
        Object object;
        if(arguments!=null) {
            object = (Object) arguments.getSerializable(Object.class.getSimpleName());

            //получение координат для отрисовки на карте из Object
            String[] points = null;
            points = object.getLocation().split(",");
            Point pointObject = new Point(Double.valueOf(points[0]), Double.valueOf(points[1]));
            ROUTE_END_LOCATION = pointObject;

            mapview = findViewById(R.id.mapview);

            //проверка какая тема день/ночь, установка светлой/темной темы для карты
            int nightModeFlags =
                    getApplicationContext().getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    mapview.getMap().setNightModeEnabled(true);
                    break;
            }
            mapview.getMap().move(
                    new CameraPosition(pointObject, 16.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 1),
                    null);
            //удаление всех меток с карты
            mapview.getMap().getMapObjects().clear();
            //установка и позиционирование метки объекта относительно низа середины картинки
            IconStyle istyle = new IconStyle();
            istyle.setAnchor(new PointF(0.5f, 1.0f));
            PlacemarkMapObject mark = mapview.getMap().getMapObjects().addPlacemark(pointObject, ImageProvider.fromResource(this, R.drawable.lable));
            mark.setIconStyle(istyle);

            TextView textNameAppBar = findViewById(R.id.textNameAppBar);
            textNameAppBar.setText(object.getName());

            //кнопка назад в список объектов
            ImageButton imageButtonBack = findViewById(R.id.imageButtonBack);
            imageButtonBack.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onBackPressed();
                }
            });


        }

        FloatingActionButton fabMasstransit = findViewById(R.id.fabMasstransit);
        FloatingActionButton fabPedestrian = findViewById(R.id.fabPedestrian);
        FloatingActionButton fabUserLocation = findViewById(R.id.fabUserLocation);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        fabUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapview.getMap().move(
                        new CameraPosition(ROUTE_START_LOCATION, 16.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null);
            }
        });

        FloatingActionButton fabDriving = findViewById(R.id.fabDriving);
        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
        fabDriving.setScaleX(0);
        fabDriving.setScaleY(0);
        fabDriving.setClickable(false);
        fabDriving.setVisibility(View.GONE);
        fabDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_selected));
                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));

                setDriving();
            }
        });

        fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
        fabPedestrian.setScaleX(0);
        fabPedestrian.setScaleY(0);
        fabPedestrian.setClickable(false);
        fabPedestrian.setVisibility(View.GONE);
        fabPedestrian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_selected));
                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));

                setPedestrian();
            }
        });


        fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
        fabMasstransit.setScaleX(0);
        fabMasstransit.setScaleY(0);
        fabMasstransit.setClickable(false);
        fabMasstransit.setVisibility(View.GONE);
        fabMasstransit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_selected));
                fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));

                setMasstransit();
            }
        });


        FloatingActionButton fabRoute = findViewById(R.id.fabRoute);
        fabRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isVisibilityButtonsRoute) {
                    fabRoute.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.close));
                    fabDriving.setVisibility(View.VISIBLE);
                    fabDriving.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabDriving.setClickable(true);

                    fabPedestrian.setVisibility(View.VISIBLE);
                    fabPedestrian.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabPedestrian.setClickable(true);

                    fabMasstransit.setVisibility(View.VISIBLE);
                    fabMasstransit.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabMasstransit.setClickable(true);

                    isVisibilityButtonsRoute = true;
                } else {

                    fabRoute.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.route));
                    fabDriving.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabDriving.setClickable(false);


                    fabPedestrian.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabPedestrian.setClickable(false);


                    fabMasstransit.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabMasstransit.setClickable(false);

                    isVisibilityButtonsRoute = false;
                }
            }
        });


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageButton imageButtonClose = findViewById(R.id.imageButtonClose);

        imageButtonClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CardView viewRoute = findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.GONE);
            }
        });
        // получение положения пользователя на карте
        getLastLocation();

        //устанавливаем свайп для карточки с данными маршрута
        CardView viewRoute = findViewById(R.id.viewRoute);
        final SwipeDismissBehavior<CardView> swipe = new SwipeDismissBehavior<CardView>() {
            @Override
            public boolean canSwipeDismissView (@NonNull View view)
            {
                return view == viewRoute;
            }
        };

        swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);

        swipe.setListener(
                new SwipeDismissBehavior.OnDismissListener() {
                    @Override public void onDismiss(View view) {}
                    @Override
                    public void onDragStateChanged(int state) {}

                });

        CoordinatorLayout.LayoutParams coordinatorParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();

        coordinatorParams.setBehavior(swipe);

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            // установка маркера положения пользователя на карте
                            ROUTE_START_LOCATION = new Point(location.getLatitude(),location.getLongitude());
                            IconStyle istyle= new IconStyle();
                            istyle.setAnchor(new PointF(0.5f,1.0f));
                            MapView mapview = findViewById(R.id.mapview);
                            if (markUserObject != null) {
                                mapview.getMap().getMapObjects().remove(markUserObject);
                            }
                            markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getApplicationContext(), R.drawable.user_arrow));
                            markUserObject.setIconStyle(istyle);
                            markUserObject.setZIndex(5);

                        }
                    }
                });
            } else {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            //установка маркера положения пользователя на карте
            ROUTE_START_LOCATION = new Point(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            IconStyle istyle= new IconStyle();
            istyle.setAnchor(new PointF(0.5f,1.0f));
            MapView mapview = findViewById(R.id.mapview);
            if (mapview != null) {
                if (markUserObject != null) {
                    mapview.getMap().getMapObjects().remove(markUserObject);
                }
                markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getApplicationContext(), R.drawable.user_arrow));
                markUserObject.setIconStyle(istyle);
                markUserObject.setZIndex(5);
            }
        }
    };

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    @Override
    public void onStop() {

        mapview.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapview.onStart();
    }

    public void setDriving() {
        //запрос маршрута для автомобилей
        if (ROUTE_START_LOCATION != null) {
            MapView mapview = findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }

            drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequest();
        }
    }

    public void setPedestrian() {
        //запрос маршрута для пешеходов
        if (ROUTE_START_LOCATION != null) {
            isMasstransitRouter = false;
            MapView mapview = findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }

            pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequestPedestrian();
        }
    }

    public void setMasstransit() {
        //запрос маршрута для общественного транспорта
        if (ROUTE_START_LOCATION != null) {
            isMasstransitRouter = true;
            MapView mapview = findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }
            masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequestMasstransit();
        }
    }

    private String getVehicleType(Transport transport, HashSet<String> knownVehicleTypes) {
        // A public transport line may have a few 'vehicle types' associated with it
        // These vehicle types are sorted from more specific (say, 'histroic_tram')
        // to more common (say, 'tramway').
        // Your application does not know the list of all vehicle types that occur in the data
        // (because this list is expanding over time), therefore to get the vehicle type of
        // a public line you should iterate from the more specific ones to more common ones
        // until you get a vehicle type which you can process
        // Some examples of vehicle types:
        // "bus", "minibus", "trolleybus", "tramway", "underground", "railway"
        for (String type : transport.getLine().getVehicleTypes()) {
            if (knownVehicleTypes.contains(type)) {
                return type;
            }
        }
        return null;
    }
    private void drawSection(SectionMetadata.SectionData data,
                             Polyline geometry) {
        // Draw a section polyline on a map
        // Set its color depending on the information which the section contains
        PolylineMapObject polylineMapObject = mapObjects.addPolyline(geometry);
        // Masstransit route section defines exactly one on the following
        // 1. Wait until public transport unit arrives
        // 2. Walk
        // 3. Transfer to a nearby stop (typically transfer to a connected
        //    underground station)
        // 4. Ride on a public transport
        // Check the corresponding object for null to get to know which
        // kind of section it is
        if (data.getTransports() != null) {
            // A ride on a public transport section contains information about
            // all known public transport lines which can be used to travel from
            // the start of the section to the end of the section without transfers
            // along a similar geometry
            for (Transport transport : data.getTransports()) {
                // Some public transport lines may have a color associated with them
                // Typically this is the case of underground lines
                if (transport.getLine().getStyle() != null) {
                    polylineMapObject.setStrokeColor(
                            // The color is in RRGGBB 24-bit format
                            // Convert it to AARRGGBB 32-bit format, set alpha to 255 (opaque)
                            transport.getLine().getStyle().getColor() | 0xFF000000
                    );
                    return;
                }
            }
            // Let us draw bus lines in green and tramway lines in red
            // Draw any other public transport lines in blue
            HashSet<String> knownVehicleTypes = new HashSet<>();
            knownVehicleTypes.add("bus");
            knownVehicleTypes.add("tramway");
            for (Transport transport : data.getTransports()) {
                String sectionVehicleType = getVehicleType(transport, knownVehicleTypes);
                if (sectionVehicleType!=null) {
                    if (sectionVehicleType.equals("bus")) {
                        polylineMapObject.setStrokeColor(0xFF00FF00);  // Green
                        return;
                    } else if (sectionVehicleType.equals("tramway")) {
                        polylineMapObject.setStrokeColor(0xFFFF0000);  // Red
                        return;
                    }
                }
            }
            polylineMapObject.setStrokeColor(0xFF0000FF);  // Blue
        } else {
            // This is not a public transport ride section
            // In this example let us draw it in black
            polylineMapObject.setStrokeColor(0xFF09692b);  // Green
            polylineMapObject.setDashLength(10.0F);
            polylineMapObject.setGapLength(10.0F);
        }
    }
    @Override
    public void onMasstransitRoutes(@NonNull List<Route> routes) {
        //получает маршрут для пешеходов и общественного транспорта
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        if (isMasstransitRouter) {
            if (routes.size() > 0) {
                for (Section section : routes.get(0).getSections()) {
                    drawSection(
                            section.getMetadata().getData(),
                            SubpolylineHelper.subpolyline(
                                    routes.get(0).getGeometry(), section.getGeometry()));
                }

                MapView mapview = findViewById(R.id.mapview);
                BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
                CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);

                mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null);

                TextView textDistance = findViewById(R.id.textDistance);
                textDistance.setVisibility(View.GONE);

                TextView textTime = findViewById(R.id.textTime);
                String textTimeStr = "Время: " + routes.get(0).getMetadata().getWeight().getTime().getText();
                textTime.setText(textTimeStr);

                CardView viewRoute = findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.VISIBLE);

                CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();
                tParams.setMargins(20, 20, 20, 20);
                viewRoute.requestLayout();
                viewRoute.setAlpha(1.0f);

            } else {
                FloatingActionButton fabMasstransit = findViewById(R.id.fabMasstransit);
                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));

                CardView viewRoute = findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.GONE);
            }
        } else {

            if (routes != null && !routes.isEmpty()) {

                PolylineMapObject polylineMapObject = mapObjects.addPolyline(routes.get(0).getGeometry());
                polylineMapObject.setStrokeColor(0xFF09692b);
                polylineMapObject.setDashLength(10.0F);
                polylineMapObject.setGapLength(10.0F);

                TextView textDistance = findViewById(R.id.textDistance);
                textDistance.setVisibility(View.VISIBLE);
                String textDistanceStr = "Расстояние: " + routes.get(0).getMetadata().getWeight().getWalkingDistance().getText();
                textDistance.setText(textDistanceStr);

                TextView textTime = findViewById(R.id.textTime);
                String textTimeStr = "Время: " + routes.get(0).getMetadata().getWeight().getTime().getText();
                textTime.setText(textTimeStr);

                CardView viewRoute = findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.VISIBLE);

                CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();
                tParams.setMargins(20, 20, 20, 20);
                viewRoute.requestLayout();
                viewRoute.setAlpha(1.0f);


                MapView mapview = findViewById(R.id.mapview);
                BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
                CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);
                mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null);
            } else {
                FloatingActionButton fabPedestrian = findViewById(R.id.fabPedestrian);
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                CardView viewRoute = findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onMasstransitRoutesError(@NonNull Error error) {
        //получает ошибку при запросе маршрутов пешехода и общественного транспорта
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        FloatingActionButton fabMasstransit = findViewById(R.id.fabMasstransit);
        FloatingActionButton fabPedestrian = findViewById(R.id.fabPedestrian);
        FloatingActionButton fabDriving = findViewById(R.id.fabDriving);


        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
        fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
        fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));

    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
       //получает маршрут для автомобилистов
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        if (routes != null && !routes.isEmpty()) {
            mapObjects.addPolyline(routes.get(0).getGeometry());

            TextView textDistance = findViewById(R.id.textDistance);
            textDistance.setVisibility(View.VISIBLE);
            String textDistanceStr = "Расстояние: " + routes.get(0).getMetadata().getWeight().getDistance().getText();
            textDistance.setText(textDistanceStr);

            TextView textTime = findViewById(R.id.textTime);
            String textTimeStr= "Время: " + routes.get(0).getMetadata().getWeight().getTime().getText();
            textTime.setText(textTimeStr);

            CardView viewRoute = findViewById(R.id.viewRoute);
            viewRoute.setVisibility(View.VISIBLE);

            CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();
            tParams.setMargins(20, 20, 20, 20);
            viewRoute.requestLayout();
            viewRoute.setAlpha(1.0f);


            MapView mapview = findViewById(R.id.mapview);
            BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
            CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);
            mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0),
                    new Animation(Animation.Type.SMOOTH, 1),
                    null);

        } else {
            FloatingActionButton fabDriving = findViewById(R.id.fabDriving);
            fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
            CardView viewRoute = findViewById(R.id.viewRoute);
            viewRoute.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        //получает ошибку запроса маршрутов для автомобилистов
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        FloatingActionButton fabDriving = findViewById(R.id.fabDriving);
        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
    }

    private void submitRequest() {
        //отправляет запрос на авто маршрут
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
    }

    private void submitRequestPedestrian() {
        //отправляет запрос на пешеходный маршрут
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        pedestrianSession = pedestrianRouter.requestRoutes(requestPoints, new TimeOptions(), this);
    }

    private void submitRequestMasstransit() {
        //отправляет запрос на маршрут общественного транспорта
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        TransitOptions options = new TransitOptions(FilterVehicleTypes.NONE.value, new TimeOptions());

        masstransitSession = masstransitRouter.requestRoutes(requestPoints, options, this);

    }

}