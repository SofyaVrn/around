package com.example.voronezh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import com.yandex.mapkit.geometry.Circle;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.logo.Alignment;
import com.yandex.mapkit.logo.HorizontalAlignment;
import com.yandex.mapkit.logo.VerticalAlignment;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CircleMapObject;
import com.yandex.mapkit.map.Cluster;
import com.yandex.mapkit.map.ClusterListener;
import com.yandex.mapkit.map.ClusterTapListener;
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements ClusterListener, ClusterTapListener, DrivingSession.DrivingRouteListener,Session.RouteListener {

    private static final float FONT_SIZE = 15;
    private static final float MARGIN_SIZE = 3;
    private static final float STROKE_SIZE = 3;
    private  Point ROUTE_START_LOCATION = null;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    PlacemarkMapObject markUserObject = null;
    List<Object> objects;
    Set<String> favorites;
    private Object objectSelected = null;
    boolean fabUserLocationVisibility;
    boolean fabRouteVisibility;
    FloatingActionButton fabRoute;
    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";
    SharedPreferences settings;
    int positionObjectSelected;
    BottomSheetBehavior bottomSheetBehavior;

    private boolean isVisibilityButtonsRoute = false;
    String filterText = "";

    private PedestrianRouter pedestrianRouter;
    private MasstransitRouter masstransitRouter;
    private Session pedestrianSession;
    private Session masstransitSession;
    private boolean isMasstransitRouter = false;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private  Point ROUTE_END_LOCATION;
    private MapObjectCollection mapObjects = null;
    public class TextImageProvider extends ImageProvider {
        @Override
        public String getId() {
            return "text_" + text;
        }

        private final String text;
        @Override
        public Bitmap getImage() {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);

            Paint textPaint = new Paint();
            textPaint.setTextSize(FONT_SIZE * metrics.density);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setAntiAlias(true);

            float widthF = textPaint.measureText(text);
            Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
            float heightF = Math.abs(textMetrics.bottom) + Math.abs(textMetrics.top);
            float textRadius = (float)Math.sqrt(widthF * widthF + heightF * heightF) / 2;
            float internalRadius = textRadius + MARGIN_SIZE * metrics.density;
            float externalRadius = internalRadius + STROKE_SIZE * metrics.density;

            int width = (int) (2 * externalRadius + 0.5);

            Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setColor(Color.RED);
            canvas.drawCircle(width / 2, width / 2, externalRadius, backgroundPaint);

            backgroundPaint.setColor(Color.WHITE);
            canvas.drawCircle(width / 2, width / 2, internalRadius, backgroundPaint);

            canvas.drawText(
                    text,
                    width / 2,
                    width / 2 - (textMetrics.ascent + textMetrics.descent) / 2,
                    textPaint);

            return bitmap;
        }

        public TextImageProvider(String text) {
            this.text = text;
        }
    }

    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
/*        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

 */
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            objectSelected = (Object) savedInstanceState.getSerializable(Object.class.getSimpleName());
            positionObjectSelected = savedInstanceState.getInt("positionObjectSelected");
        }

        settings = getContext().getSharedPreferences(PREFS_FILE, getContext().MODE_PRIVATE);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // method to get the location
        getLastLocation();
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

                            Log.d("onComplete getLastLocation : ", String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));

                            ROUTE_START_LOCATION = new Point(location.getLatitude(),location.getLongitude());
                            IconStyle istyle= new IconStyle();
                            istyle.setAnchor(new PointF(0.5f,1.0f));
                            MapView mapview = (MapView) getView().findViewById(R.id.mapview);
                            if (markUserObject != null) {
                                mapview.getMap().getMapObjects().remove(markUserObject);
                            }
                            markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getContext(), R.drawable.user_arrow));
                            markUserObject.setIconStyle(istyle);
                            markUserObject.setZIndex(5);

                            LinearLayout llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet_search);
                            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

                            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                                FloatingActionButton fabUserLocation = (FloatingActionButton) getView().findViewById(R.id.fabUserLocation);
                                fabUserLocation.setScaleX(1);
                                fabUserLocation.setScaleY(1);
                                fabUserLocationVisibility = true;
                            }

                            // latitudeTextView.setText(location.getLatitude() + "");
                            // longitTextView.setText(location.getLongitude() + "");
                        }
                    }
                });
            } else {
                // Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

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

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            Log.d("onLocationResult : ", String.valueOf(mLastLocation.getLatitude()) + " " + String.valueOf(mLastLocation.getLongitude()));
            ROUTE_START_LOCATION = new Point(mLastLocation.getLatitude(),mLastLocation.getLongitude());

            IconStyle istyle= new IconStyle();
            istyle.setAnchor(new PointF(0.5f,1.0f));
            MapView mapview = (MapView) getView().findViewById(R.id.mapview);
            if (mapview != null) {
                if (markUserObject != null) {
                    mapview.getMap().getMapObjects().remove(markUserObject);
                }
                markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getContext(), R.drawable.user_arrow));
                markUserObject.setIconStyle(istyle);
                markUserObject.setZIndex(5);
            }

            //setUserLayer(ROUTE_START_LOCATION);
            // latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            // longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    @Override
    public void onStop() {
        MapView mapview = (MapView) getView().findViewById(R.id.mapview);
        mapview.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        MapKitFactory.getInstance().onStart();
        MapView mapview = (MapView) getView().findViewById(R.id.mapview);
        mapview.onStart();
        mapview.getMap().setRotateGesturesEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (objectSelected != null) {
            outState.putSerializable(Object.class.getSimpleName(), objectSelected);
            outState.putInt("positionObjectSelected", positionObjectSelected);
        }

        MapView mapview = (MapView) getView().findViewById(R.id.mapview);

        CameraPosition cp = mapview.getMap().getCameraPosition();

        String  latitude = String.valueOf(cp.getTarget().getLatitude());
        String longitude = String.valueOf(cp.getTarget().getLongitude());
        String zoom  = String.valueOf(cp.getZoom());

        outState.putString("latitude", latitude);
        outState.putString("longitude", longitude);
        outState.putString("zoom", zoom);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        Log.d("SearchFragment onViewStateRestored","SearchFragment onViewStateRestored");

        ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(R.id.viewFlipper);
        LinearLayout llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet_search);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        FloatingActionButton fabRoute = (FloatingActionButton) getView().findViewById(R.id.fabRoute);
        FloatingActionButton fabBack = (FloatingActionButton) getView().findViewById(R.id.fabBack);

        if (objectSelected == null) {
            viewFlipper.setDisplayedChild(1);
            if (savedInstanceState != null) {
                String latitude = savedInstanceState.getString("latitude");
                String longitude = savedInstanceState.getString("longitude");
                String zoom = savedInstanceState.getString("zoom");
                MapView mapview = (MapView) getView().findViewById(R.id.mapview);
                mapview.getMap().move(new CameraPosition(new Point(Double.parseDouble(latitude), Double.parseDouble(longitude)),Float.parseFloat(zoom), 0, 0));
            }
        } else {

            if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)) {
                fabRoute.setScaleX(1);
                fabRoute.setScaleY(1);
                fabRoute.setClickable(true);
                fabRouteVisibility = true;
            }

            setDataObject(objectSelected,positionObjectSelected);;
            fabBack.setVisibility(View.VISIBLE);

            viewFlipper.setDisplayedChild(0);
        }
    }

    public void setDataFilter(String filter) {
        Log.d("setDataFilter","setDataFilter");

        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();

        List<Object> objectsGlobal = new ArrayList<>();
        if (filter.trim().isEmpty()) {
            objectsGlobal = adapter.getObjectsSearch();
        } else {
            objectsGlobal = adapter.getGlobalObjectsFilter(filter);
        }

        if (!objectsGlobal.isEmpty()) {
            for (Object object : objectsGlobal) {
                String filename = String.valueOf(object.getId()) + ".png";
                try (InputStream inputStream = getContext().getAssets().open(filename)) {
                    object.setImgUrl(filename);
                } catch (IOException e) {
                    filename = String.valueOf(object.getId()) + ".jpg";
                    try (InputStream inputStream = getContext().getAssets().open(filename)) {
                        object.setImgUrl(filename);
                    } catch (IOException e_jpg) {
                        e_jpg.printStackTrace();
                    }
                    // e.printStackTrace();
                }
            }

            objects.clear();
            objects.addAll(objectsGlobal);
            RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
            objectsList.getAdapter().notifyDataSetChanged();
            objectsList.scrollToPosition(0);
            setClusterized(true);
            IconStyle istyle= new IconStyle();
            MapView mapview = (MapView) getView().findViewById(R.id.mapview);
            if (ROUTE_START_LOCATION != null) {
                markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getContext(), R.drawable.user_arrow));
                markUserObject.setIconStyle(istyle);
                markUserObject.setZIndex(5);
            }
        } else {
            objects.clear();
            objects.addAll(objectsGlobal);
            RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
            objectsList.getAdapter().notifyDataSetChanged();

            MapView mapview = (MapView) getView().findViewById(R.id.mapview);
            mapview.getMap().getMapObjects().clear();
            IconStyle istyle= new IconStyle();
            markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getContext(), R.drawable.user_arrow));
            markUserObject.setIconStyle(istyle);
            markUserObject.setZIndex(5);
        }
        adapter.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        EditText userFilter = (EditText)view.findViewById(R.id.objectFilter);

        userFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            // при изменении текста выполняем фильтрацию
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d("ListFragment","onTextChanged");
                Log.d("FILTER :: ",  s.toString());
                //if (!filterText.trim().isEmpty()) setDataFilter(filterText);
                if (filterText != s.toString()) {
                    filterText = s.toString();
                    setDataFilter(filterText);
                }
            }
        });

        userFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        MapView mapview = (MapView) view.findViewById(R.id.mapview);

        ViewFlipper viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);


        LinearLayout llBottomSheet = (LinearLayout) view.findViewById(R.id.bottom_sheet_search);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        fabRoute = (FloatingActionButton) view.findViewById(R.id.fabRoute);
        FloatingActionButton fabMasstransit = view.findViewById(R.id.fabMasstransit);
        FloatingActionButton fabPedestrian = view.findViewById(R.id.fabPedestrian);
        FloatingActionButton fabDriving = view.findViewById(R.id.fabDriving);


        FloatingActionButton fabBack = (FloatingActionButton) view.findViewById(R.id.fabBack);
        fabBack.setVisibility(View.GONE);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FAB", "fabBack");
                setClusterized(false);
               // viewFlipper.setDisplayedChild(1);
                ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(R.id.viewFlipper);
                LinearLayout llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet_search);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
                FloatingActionButton fabBack = (FloatingActionButton) getView().findViewById(R.id.fabBack);
                FloatingActionButton fabRoute = (FloatingActionButton) getView().findViewById(R.id.fabRoute);
                MapView mapview = (MapView) getView().findViewById(R.id.mapview);
                if (viewFlipper.getDisplayedChild() == 0 && (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)) {
                    fabRoute.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabRoute.setClickable(false);
                    fabRouteVisibility = false;

                    if(isVisibilityButtonsRoute) {
                        fabDriving.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        fabDriving.setClickable(false);
//                        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                        fabMasstransit.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        fabMasstransit.setClickable(false);
//                        fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                        fabPedestrian.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        fabPedestrian.setClickable(false);
//                        fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                        fabRoute.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.route));
                        isVisibilityButtonsRoute = false;
                    }

                    fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
                    fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
                    fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));


                }

                viewFlipper.setInAnimation(getContext(), android.R.anim.slide_in_left);
                viewFlipper.setOutAnimation(getContext(), android.R.anim.slide_out_right);
                viewFlipper.showNext();
                fabBack.setVisibility(View.GONE);
                IconStyle istyle= new IconStyle();
                markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getContext(), R.drawable.user_arrow));
                markUserObject.setIconStyle(istyle);
                markUserObject.setZIndex(5);

                objectSelected = null;

            }
        });


        FloatingActionButton fabUserLocation = (FloatingActionButton) view.findViewById(R.id.fabUserLocation);

        fabUserLocation.setScaleX(0.0F);
        fabUserLocation.setScaleY(0.0F);
        fabUserLocationVisibility = false;
        fabRouteVisibility = false;
        fabUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FAB", "User Location");
                MapView mapview = (MapView) getView().findViewById(R.id.mapview);
                mapview.getMap().move(
                        new CameraPosition(ROUTE_START_LOCATION, 16.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 0),
                        null);
            }
        });


 //       FloatingActionButton fabRoute = (FloatingActionButton) view.findViewById(R.id.fabRoute);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
        fabDriving.setScaleX(0);
        fabDriving.setScaleY(0);
        fabDriving.setClickable(false);
        fabDriving.setVisibility(View.GONE);
        fabDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_selected));
                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                setDriving();
            }
        });

        fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
        fabPedestrian.setScaleX(0);
        fabPedestrian.setScaleY(0);
        fabPedestrian.setClickable(false);
        fabPedestrian.setVisibility(View.GONE);
        fabPedestrian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_selected));
                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                setPedestrian();
            }
        });


        fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
        fabMasstransit.setScaleX(0);
        fabMasstransit.setScaleY(0);
        fabMasstransit.setClickable(false);
        fabMasstransit.setVisibility(View.GONE);
        fabMasstransit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_selected));
                fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                setMasstransit();
            }
        });



        fabRoute.setScaleX(0.0F);
        fabRoute.setScaleY(0.0F);
        fabRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isVisibilityButtonsRoute) {
                    fabRoute.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.close));
                    //  fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                    fabDriving.setVisibility(View.VISIBLE);
                    fabDriving.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabDriving.setClickable(true);


                    //   fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                    fabPedestrian.setVisibility(View.VISIBLE);
                    fabPedestrian.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabPedestrian.setClickable(true);


                    //    fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getApplicationContext(), R.color.fab_not_selected));
                    fabMasstransit.setVisibility(View.VISIBLE);
                    fabMasstransit.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabMasstransit.setClickable(true);

                    isVisibilityButtonsRoute = true;
                } else {

                    fabRoute.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.route));
                    fabDriving.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabDriving.setClickable(false);


                    fabPedestrian.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabPedestrian.setClickable(false);


                    fabMasstransit.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabMasstransit.setClickable(false);

                    isVisibilityButtonsRoute = false;
                    // fabDriving.setVisibility(View.GONE);
                }
            }
        });


        //получаем высоту экрана
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;

        //устанавливаем высоту нижнего экрана
        int maxHeight = (int) (height*0.80);
        bottomSheetBehavior.setMaxHeight(maxHeight);
        llBottomSheet.setMinimumHeight(maxHeight);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                FloatingActionButton fabUserLocation = (FloatingActionButton) view.findViewById(R.id.fabUserLocation);
                FloatingActionButton fabMasstransit = view.findViewById(R.id.fabMasstransit);
                FloatingActionButton fabPedestrian = view.findViewById(R.id.fabPedestrian);
                FloatingActionButton fabDriving = view.findViewById(R.id.fabDriving);

                if (BottomSheetBehavior.STATE_DRAGGING == newState) {
                    if (fabUserLocationVisibility) {
                        fabUserLocation.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        fabUserLocation.setClickable(false);
                        fabUserLocationVisibility = false;
                    }

                    if (fabRouteVisibility) {
                        fabRoute.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        fabRoute.setClickable(false);
                        fabRouteVisibility = false;

                       // FloatingActionButton fabMasstransit = view.findViewById(R.id.fabMasstransit);
                        //FloatingActionButton fabPedestrian = view.findViewById(R.id.fabPedestrian);
                       // FloatingActionButton fabDriving = view.findViewById(R.id.fabDriving);

                        if(isVisibilityButtonsRoute) {
                            fabDriving.animate().scaleX(0).scaleY(0).setDuration(300).start();
                            fabDriving.setClickable(false);
                            //fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                            fabMasstransit.animate().scaleX(0).scaleY(0).setDuration(300).start();
                            fabMasstransit.setClickable(false);
                            //fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                            fabPedestrian.animate().scaleX(0).scaleY(0).setDuration(300).start();
                            fabPedestrian.setClickable(false);
                            //fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));



                            fabRoute.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.route));
                            isVisibilityButtonsRoute = false;
                        }

                    }

                }

                if (BottomSheetBehavior.STATE_COLLAPSED == newState && !fabUserLocationVisibility) {
                    fabUserLocation.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabUserLocation.setClickable(true);
                    fabUserLocationVisibility = true;
                }

                if (BottomSheetBehavior.STATE_EXPANDED == newState && fabUserLocationVisibility) {
                    fabUserLocation.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabUserLocation.setClickable(false);
                    fabUserLocationVisibility = false;
                }

                if (BottomSheetBehavior.STATE_COLLAPSED == newState && !fabRouteVisibility && viewFlipper.getDisplayedChild() == 0) {
                    fabRoute.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabRoute.setClickable(true);
                    fabRouteVisibility = true;
                }

                if (BottomSheetBehavior.STATE_EXPANDED == newState && fabRouteVisibility && viewFlipper.getDisplayedChild() == 0) {
                    fabRoute.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    fabRoute.setClickable(false);
                    fabRouteVisibility = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });



        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();
        objects = adapter.getObjectsSearch();

        for(Object object : objects){

            //Log.d("object.name :: ", object.getName());
            String filename = String.valueOf(object.getId()) + ".png";
            try(InputStream inputStream = getContext().getAssets().open(filename)){
                object.setImgUrl(filename);
            }
            catch (IOException e){
                filename = String.valueOf(object.getId()) + ".jpg";
                try(InputStream inputStream = getContext().getAssets().open(filename)){
                    object.setImgUrl(filename);
                } catch (IOException e_jpg) {e_jpg.printStackTrace();}
                // e.printStackTrace();
            }
        }


        RecyclerView objectsList = (RecyclerView) view.findViewById(R.id.objectsList);

        int spanCount = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);

        objectsList.setLayoutManager(gridLayoutManager);
       // int pixels = (int)ObjectTypeAdapter.convertDpToPixel(4,getContext());
        //objectsList.addItemDecoration(new EqualSpacingItemDecoration(pixels));
        objectsList.setHasFixedSize(true);

        ObjectAdapter.OnObjectClickListener objectClickListener = new ObjectAdapter.OnObjectClickListener() {
            @Override
            public void onObjectClick(Object object, int position) {
                //fragmentSendDataListListener.onSendDataListObject(object,position);
                objectSelected = object;
                positionObjectSelected = position;
                LinearLayout llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet_search);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
                ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(R.id.viewFlipper);

                if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)) {
                    fabRoute.animate().scaleX(1).scaleY(1).setDuration(300).start();
                    fabRoute.setClickable(true);
                    fabRouteVisibility = true;
                }

                setDataObject(object,position);
                //viewFlipper.setDisplayedChild(0);
                viewFlipper.setInAnimation(getContext(), R.anim.slide_in_right);
                viewFlipper.setOutAnimation(getContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
                FloatingActionButton fabBack = (FloatingActionButton) getView().findViewById(R.id.fabBack);
                fabBack.setVisibility(View.VISIBLE);
//!!!!!!!!!
            }

            @Override
            public void onObjectFavoriteClick(Object object, int position) {
                 setObjectFavorites(object,position);
            }
        };

        ObjectAdapter objectAdapter = new ObjectAdapter(getContext(), objects,objectClickListener,true);

        // устанавливаем адаптер
        objectsList.setAdapter(objectAdapter);

/*
        if (objectSelected == null) {
            viewFlipper.setDisplayedChild(1);
        } else {

            if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)) {
                fabRoute.setScaleX(1);
                fabRoute.setScaleY(1);
                fabRoute.setClickable(true);
                fabRouteVisibility = true;
            }

            setDataObject(objectSelected,positionObjectSelected);;
            fabBack.setVisibility(View.VISIBLE);

            viewFlipper.setDisplayedChild(0);
        }
*/
        ImageButton imageButtonCall = (ImageButton) view.findViewById(R.id.imageButtonCall);
        imageButtonCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String[] phones = null;
                phones = objectSelected.getPhone().split("\n");

                String[] phone_first = phones[0].split("\\D+");
                String phoneNumber =  String.join("", phone_first);

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                getActivity().startActivity(intent);

            }
        });

        ImageButton imageButtonWebsite = (ImageButton) view.findViewById(R.id.imageButtonWebsite);
        imageButtonWebsite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String url = objectSelected.getWebsite();
                // для url на русском необходимо сконвертировать домены
                String sURL[] = url.split("//");
                String s1 = sURL[1];

                String sURLHost[] = s1.split("/");
                String host = sURLHost[0];

                String releaseUrl = "";
                for (int i=0; i<sURLHost.length; i++)
                {
                    if (i == 0) {
                        releaseUrl = IDN.toASCII(host);
                    } else {
                        releaseUrl += "/" + sURLHost[i];
                    }
                }

                Intent openPage= new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + releaseUrl));
                getActivity().startActivity(openPage);
            }
        });

        ImageButton imageButtonShare = (ImageButton) view.findViewById(R.id.imageButtonShare);

        imageButtonShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = objectSelected.getName();
                String address = objectSelected.getAddress();
                String[] points = null;
                points = objectSelected.getLocation().split(",");
                String url = "https://yandex.ru/maps/?pt="+points[1].trim()+","+points[0].trim()+"&z=16&l=map";
                Log.d("url",url);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, name + "\n" + address + "\n" + url);
                getActivity().startActivity(sendIntent);
            }
        });

        ImageView imgFavorite = (ImageView) view.findViewById(R.id.imgFavorite);

        imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavorite();
            }
        });


        ImageButton imageButtonClose = (ImageButton) view.findViewById(R.id.imageButtonClose);

        imageButtonClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CardView viewRoute = (CardView)getView().findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.GONE);

                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
                coordinatorLayout.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2)).start();
         }
        });

        CardView viewRoute = (CardView)view.findViewById(R.id.viewRoute);

        final SwipeDismissBehavior<CardView> swipe = new SwipeDismissBehavior<CardView>() {
            @Override
            public boolean canSwipeDismissView (View view)
            {
                return view == viewRoute;
            }
        };

        swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);

        swipe.setListener(
                new SwipeDismissBehavior.OnDismissListener() {
                    @Override public void onDismiss(View view) {
                        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
                        coordinatorLayout.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2)).start();
                        CardView viewRoute = (CardView)getView().findViewById(R.id.viewRoute);
                        viewRoute.setVisibility(View.GONE);
                    }
                    @Override
                    public void onDragStateChanged(int state) {}
                });

        CoordinatorLayout.LayoutParams coordinatorParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();

        coordinatorParams.setBehavior(swipe);

       // MapView mapview = (MapView) view.findViewById(R.id.mapview);
       // MapView mapview = (MapView) getView().findViewById(R.id.mapview);
        ImageProvider imageProvider = ImageProvider.fromResource(getContext(),R.drawable.search_result);
        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                mapview.getMap().setNightModeEnabled(true);
                break;
        }
        mapview.getMap().getLogo().setAlignment(new Alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
        mapview.getMap().getMapObjects().clear();
        ClusterizedPlacemarkCollection clusterizedCollection = mapview.getMap().getMapObjects().addClusterizedPlacemarkCollection(this);
        List<Point> points = createPoints(objects);


        List<Point> geometry = new ArrayList<>();
        for (int i = 0; i < objectAdapter.getItemCount(); ++i) {
            Object object = (Object)objectAdapter.getItem(i);
            String[] pointsObj = null;
            pointsObj = object.getLocation().split(",");
            PlacemarkMapObject mark = clusterizedCollection.addPlacemark(new Point(Double.valueOf(pointsObj[0]),Double.valueOf(pointsObj[1])), imageProvider, new IconStyle());
            mark.setUserData(new ObjectMapObjectUserData(i, object));
            mark.addTapListener(objectMapObjectTapListener);
            geometry.add(new Point(Double.valueOf(pointsObj[0]),Double.valueOf(pointsObj[1])));
        }
        clusterizedCollection.clusterPlacemarks(60, 15);

        BoundingBox box = BoundingBoxHelper.getBounds(new Polyline(geometry));
        CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);


       // CameraPosition cp = new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom(), 0, 0);

       // double latitude = cp.getTarget().getLatitude() - 0.2;
       // double longitude = cp.getTarget().getLongitude() - 0.2;
       // mapview.getMap().move(new CameraPosition(new Point(latitude, longitude), boundingBoxPosition.getZoom(), 0, 0));

        mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.5F, 0, 0));

        adapter.close();
        return view;
    }

    public void setObjectFavorites(Object selectedObject,int position) {
        Log.d("setObjectFavorites","setObjectFavorites");
        //добавление либо удаление объекта из избранного
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

        boolean isAdded = favorites.add(String.valueOf(selectedObject.getId()));

        if (isAdded) {
            Toast.makeText(getActivity().getApplicationContext(), "Объект добавлен в избранное", Toast.LENGTH_SHORT).show();
        } else {
            favorites.remove(String.valueOf(selectedObject.getId()));
            Toast.makeText(getActivity(), "Объект удален из избранного", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences.Editor prefEditor = settings.edit();

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
        //objectAdapter.notifyDataSetChanged();
        objectAdapter.notifyItemChanged(position);
        prefEditor.remove(PREF_FAVORITES);
        prefEditor.commit();
        prefEditor.putStringSet(PREF_FAVORITES, favorites);
        prefEditor.commit();

    }
    public void setFavorite() {
        //добавление либо удаление объекта из избранного во фрагменте с объектом
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

        boolean isAdded = favorites.add(String.valueOf(objectSelected.getId()));

        ImageView imgFavorite = (ImageView) getView().findViewById(R.id.imgFavorite);

     //   ImageButton imageButtonFavorite = (ImageButton) getView().findViewById(R.id.imageButtonFavorite);

        if (isAdded) {
            Toast.makeText(getActivity().getApplicationContext(), "Объект добавлен в избранное", Toast.LENGTH_SHORT).show();
            imgFavorite.setImageResource(R.drawable.favorite);
            imgFavorite.setColorFilter(Color.RED);
        } else {
            favorites.remove(String.valueOf(objectSelected.getId()));
            Toast.makeText(getActivity(), "Объект удален из избранного", Toast.LENGTH_SHORT).show();
            imgFavorite.setImageResource(R.drawable.favorite_border);
            imgFavorite.setColorFilter(getActivity().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        }

        SharedPreferences.Editor prefEditor = settings.edit();

        prefEditor.remove(PREF_FAVORITES);
        prefEditor.commit();
        prefEditor.putStringSet(PREF_FAVORITES, favorites);
        prefEditor.commit();

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
        objectAdapter.notifyItemChanged(positionObjectSelected);

    }

    public void setClusterized(boolean changeCamera) {
        MapView mapview = (MapView) getView().findViewById(R.id.mapview);
        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);

        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();


        ImageProvider imageProvider = ImageProvider.fromResource(getContext(),R.drawable.search_result);
        mapview.getMap().getMapObjects().clear();
        ClusterizedPlacemarkCollection clusterizedCollection = mapview.getMap().getMapObjects().addClusterizedPlacemarkCollection(this);
        List<Point> points = createPoints(objects);


        List<Point> geometry = new ArrayList<>();
        for (int i = 0; i < objectAdapter.getItemCount(); ++i) {
            Object object = (Object)objectAdapter.getItem(i);
            String[] pointsObj = null;
            pointsObj = object.getLocation().split(",");
            PlacemarkMapObject mark = clusterizedCollection.addPlacemark(new Point(Double.valueOf(pointsObj[0]),Double.valueOf(pointsObj[1])), imageProvider, new IconStyle());
            mark.setUserData(new ObjectMapObjectUserData(i, object));
            mark.addTapListener(objectMapObjectTapListener);
            geometry.add(new Point(Double.valueOf(pointsObj[0]),Double.valueOf(pointsObj[1])));
        }
        clusterizedCollection.clusterPlacemarks(60, 15);

        BoundingBox box = BoundingBoxHelper.getBounds(new Polyline(geometry));
        CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);

        if (changeCamera) mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.5F, 0, 0));

        // CameraPosition cp = new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom(), 0, 0);

        // double latitude = cp.getTarget().getLatitude() - 0.2;
        // double longitude = cp.getTarget().getLongitude() - 0.2;
        // mapview.getMap().move(new CameraPosition(new Point(latitude, longitude), boundingBoxPosition.getZoom(), 0, 0));

       // mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.5F, 0, 0));

    }


   public void setDataObject(Object object,int position) {
       NestedScrollView nestedScroll = (NestedScrollView) getView().findViewById(R.id.bottom_sheet_scroll);
       nestedScroll.fullScroll(NestedScrollView.FOCUS_UP);

       ImageView imageObject = (ImageView) getView().findViewById(R.id.imageObject);

       TextView textName = (TextView) getView().findViewById(R.id.textName);
       TextView textAddress = (TextView) getView().findViewById(R.id.textAddress);
       TextView textDescription = (TextView) getView().findViewById(R.id.textDescription);

       textDescription.setText(object.getDescription());
       textName.setText(object.getName());
       textAddress.setText(object.getAddress());

       ImageView imgAccess = (ImageView) getView().findViewById(R.id.imgAccessibility);

       if(object.getEnviron() == 1) {
           imgAccess.setVisibility(View.VISIBLE);
       } else {
           imgAccess.setVisibility(View.GONE);
       }

       RelativeLayout relativeLayoutCall = (RelativeLayout) getView().findViewById(R.id.relativeLayoutCall);

       if(object.getPhone().isEmpty()) {
           relativeLayoutCall.setVisibility(View.GONE);
       } else {
           if (relativeLayoutCall.getVisibility() == View.GONE) {
               relativeLayoutCall.setVisibility(View.VISIBLE);
           }
       }

       RelativeLayout relativeLayoutWebsite = (RelativeLayout) getView().findViewById(R.id.relativeLayoutWebsite);


       if(object.getWebsite().isEmpty() || !ObjectFragment.urlValidator(object.getWebsite())) {
           relativeLayoutWebsite.setVisibility(View.GONE);
       } else {
           if (relativeLayoutWebsite.getVisibility() == View.GONE) {
               relativeLayoutWebsite.setVisibility(View.VISIBLE);
           }
       }

       ViewOutlineProvider provider = new ViewOutlineProvider() {
           @Override
           public void getOutline(View view, Outline outline) {
               int curveRadius = 24;
               outline.setRoundRect(0, 0, view.getWidth(), (view.getHeight()), curveRadius);
           }
       };

       try (InputStream inputStream = getContext().getAssets().open(object.getImgUrl())) {
           Drawable drawable = Drawable.createFromStream(inputStream, null);
           imageObject.setImageDrawable(drawable);

           imageObject.setOutlineProvider(provider);
           imageObject.setClipToOutline(true);
       } catch (IOException e){e.printStackTrace();}


       imgAccess.setOutlineProvider(provider);
       imgAccess.setClipToOutline(true);

       boolean isContains = favorites.contains(String.valueOf(object.getId()));

       ImageView imgFavorite = getView().findViewById(R.id.imgFavorite);
       /*if (isContains){
           imgFavorite.setImageResource(R.drawable.favorite);
       }*/

       if (isContains){
           imgFavorite.setImageResource(R.drawable.favorite);
           imgFavorite.setColorFilter(Color.RED);
       } else {
           imgFavorite.setImageResource(R.drawable.favorite_border);
           imgFavorite.setColorFilter(getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
       }

       imgFavorite.setOutlineProvider(provider);
       imgFavorite.setClipToOutline(true);


       String[] points = null;
       points = object.getLocation().split(",");
       Point pointObject = new Point(Double.valueOf(points[0]),Double.valueOf(points[1]));
       ROUTE_END_LOCATION = pointObject;

       MapView mapview = (MapView) getView().findViewById(R.id.mapview);
       mapview.getMap().getMapObjects().clear();
       mapObjects = null;

       IconStyle istyle= new IconStyle();
       istyle.setAnchor(new PointF(0.5f,1.0f));
       PlacemarkMapObject mark = mapview.getMap().getMapObjects().addPlacemark(pointObject, ImageProvider.fromResource(getContext(), R.drawable.lable));
       mark.setIconStyle(istyle);

       if (ROUTE_START_LOCATION != null) {
           markUserObject = mapview.getMap().getMapObjects().addPlacemark(ROUTE_START_LOCATION, ImageProvider.fromResource(getContext(), R.drawable.user_arrow));
           markUserObject.setIconStyle(istyle);
           markUserObject.setZIndex(5);
       }

       mapview.getMap().move(
               new CameraPosition(pointObject, 16.0f, 0.0f, 0.0f),
               new Animation(Animation.Type.SMOOTH, 1),
               null);

   }

    private MapObjectTapListener objectMapObjectTapListener = new MapObjectTapListener() {
        @Override
        public boolean onMapObjectTap(MapObject mapObject, Point point) {
            Log.d("onMapObjectTap11","onMapObjectTap11");
            if (mapObject instanceof PlacemarkMapObject) {
                PlacemarkMapObject objectMap = (PlacemarkMapObject)mapObject;


                java.lang.Object userData = objectMap.getUserData();
                if (userData instanceof ObjectMapObjectUserData) {
                    ObjectMapObjectUserData objectMapUserData = (ObjectMapObjectUserData)userData;
                    Log.d("onMapObjectTap33",objectMapUserData.object.getName());

                    //!!!!!!!!!!!
                    objectSelected = objectMapUserData.object;
                    positionObjectSelected = objectMapUserData.position;
                    ViewFlipper viewFlipper = (ViewFlipper) getView().findViewById(R.id.viewFlipper);

                    LinearLayout llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet_search);
                    BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

                    if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)) {
                        fabRoute.animate().scaleX(1).scaleY(1).setDuration(300).start();
                        fabRoute.setClickable(true);
                        fabRouteVisibility = true;
                    }

                    setDataObject(objectSelected,positionObjectSelected);
                    //viewFlipper.setDisplayedChild(0);
                    if (viewFlipper.getDisplayedChild() == 1) {
                        viewFlipper.setInAnimation(getContext(), R.anim.slide_in_right);
                        viewFlipper.setOutAnimation(getContext(), R.anim.slide_out_left);
                        viewFlipper.showPrevious();
                    }
                    FloatingActionButton fabBack = (FloatingActionButton) getView().findViewById(R.id.fabBack);
                    fabBack.setVisibility(View.VISIBLE);



                }

                Log.d("onMapObjectTap44","onMapObjectTap");
            }
            return true;
        }
    };

    private class ObjectMapObjectUserData {
        final int position;
        final Object object;

        ObjectMapObjectUserData(int position, Object object) {
            this.position = position;
            this.object = object;
        }
    }

        private List<Point> createPoints(List<Object>  objects) {
        ArrayList<Point> points = new ArrayList<Point>();

        for(Object object : objects) {

            String[] pointsObj = null;
            pointsObj = object.getLocation().split(",");
            points.add(new Point(Double.valueOf(pointsObj[0]),Double.valueOf(pointsObj[1])));

        }
/*
        for (int i = 0; i < count; ++i) {
            Point clusterCenter = CLUSTER_CENTERS.get(random.nextInt(CLUSTER_CENTERS.size()));
            double latitude = clusterCenter.getLatitude() + Math.random() - 0.5;
            double longitude = clusterCenter.getLongitude() + Math.random() - 0.5;

            points.add(new Point(latitude, longitude));
        }
*/
        return points;
    }


    @Override
    public void onClusterAdded(Cluster cluster) {
        // We setup cluster appearance and tap handler in this method
        cluster.getAppearance().setIcon(
                new TextImageProvider(Integer.toString(cluster.getSize())));
        cluster.addClusterTapListener(this);
    }

    @Override
    public boolean onClusterTap(Cluster cluster) {
       /* Toast.makeText(
                getApplicationContext(),
                String.format(getString(R.string.cluster_tap_message), cluster.getSize()),
                Toast.LENGTH_SHORT).show();
*/
        Log.d("onClusterTap","onClusterTap");
        // We return true to notify map that the tap was handled and shouldn't be
        // propagated further.
        return true;
    }

    private int getHeightOfView(View contentview) {
        contentview.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //contentview.getMeasuredWidth();
        return contentview.getMeasuredHeight();
    }
    public void setDriving() {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
        coordinatorLayout.animate().translationY(getHeightOfView(coordinatorLayout)).setInterpolator(new AccelerateInterpolator(2)).start();

        if (ROUTE_START_LOCATION != null) {
            MapView mapview = getView().findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }

            drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequest();
        }
    }

    public void setPedestrian() {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
        coordinatorLayout.animate().translationY(getHeightOfView(coordinatorLayout)).setInterpolator(new AccelerateInterpolator(2)).start();

        if (ROUTE_START_LOCATION != null) {
            isMasstransitRouter = false;
            MapView mapview = getView().findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }

            //ROUTE_START_LOCATION = userLocationLayer.cameraPosition().getTarget();
            pedestrianRouter = TransportFactory.getInstance().createPedestrianRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequestPedestrian();
        }
    }

    public void setMasstransit() {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
        coordinatorLayout.animate().translationY(getHeightOfView(coordinatorLayout)).setInterpolator(new AccelerateInterpolator(2)).start();

        if (ROUTE_START_LOCATION != null) {
            isMasstransitRouter = true;
            MapView mapview = getView().findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }

            //ROUTE_START_LOCATION = userLocationLayer.cameraPosition().getTarget();
            masstransitRouter = TransportFactory.getInstance().createMasstransitRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequestMasstransit();
        }
    }


    @Override
    public void onDrivingRoutes(List<DrivingRoute> routes) {
       /* for (DrivingRoute route : routes) {
            mapObjects.addPolyline(route.getGeometry());
        }
        */
        ProgressBar progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        if (routes != null && !routes.isEmpty()) {
            mapObjects.addPolyline(routes.get(0).getGeometry());

            TextView textDistance = getView().findViewById(R.id.textDistance);
            textDistance.setVisibility(View.VISIBLE);
            String textDistanceStr = "Расстояние: " + routes.get(0).getMetadata().getWeight().getDistance().getText();
            textDistance.setText(textDistanceStr);

            TextView textTime = getView().findViewById(R.id.textTime);
            String textTimeStr= "Время: " + routes.get(0).getMetadata().getWeight().getTime().getText();
            textTime.setText(textTimeStr);

            CardView viewRoute = getView().findViewById(R.id.viewRoute);
            viewRoute.setVisibility(View.VISIBLE);

            CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();
            tParams.setMargins(20, 20, 20, 20);
            viewRoute.requestLayout();
            viewRoute.setAlpha(1.0f);


            MapView mapview = getView().findViewById(R.id.mapview);
            BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
            CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);
           // mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0));


            mapview.getMap().move(
                    new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0),
                    new Animation(Animation.Type.SMOOTH, 1),
                    null);

            /*BoundingBox boundingBox= new BoundingBox(ROUTE_START_LOCATION,ROUTE_END_LOCATION);
            CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(boundingBox);

            mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0));
*/
        }else {
            FloatingActionButton fabDriving = getView().findViewById(R.id.fabDriving);
            fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
            CardView viewRoute = getView().findViewById(R.id.viewRoute);
            viewRoute.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDrivingRoutesError(Error error) {
        Log.d("Error onDrivingRoutesError","Error onDrivingRoutesError");

        CardView viewRoute = (CardView)getView().findViewById(R.id.viewRoute);
        viewRoute.setVisibility(View.GONE);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
        coordinatorLayout.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2)).start();

        FloatingActionButton fabDriving = getView().findViewById(R.id.fabDriving);
        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

    /*    String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

      */
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
            polylineMapObject.setStrokeColor(0xFF09692b);  // Black
            polylineMapObject.setDashLength(10.0F);
            polylineMapObject.setGapLength(10.0F);
        }
    }
    @Override
    public void onMasstransitRoutes(List<Route> routes) {

        ProgressBar progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        if (isMasstransitRouter) {
            if (routes.size() > 0) {
                for (Section section : routes.get(0).getSections()) {
                    drawSection(
                            section.getMetadata().getData(),
                            SubpolylineHelper.subpolyline(
                                    routes.get(0).getGeometry(), section.getGeometry()));
                }

                MapView mapview = getView().findViewById(R.id.mapview);
                BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
                CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);

                mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null);

                TextView textDistance = getView().findViewById(R.id.textDistance);
                textDistance.setVisibility(View.GONE);
                //String textDistanceStr = "Расстояние: " + routes.get(0).getMetadata().getWeight().getWalkingDistance().getText();
                //textDistance.setText(textDistanceStr);

                TextView textTime = getView().findViewById(R.id.textTime);
                String textTimeStr = "Время: " + routes.get(0).getMetadata().getWeight().getTime().getText();
                textTime.setText(textTimeStr);

                CardView viewRoute = getView().findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.VISIBLE);

                CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();
                tParams.setMargins(20, 20, 20, 20);
                viewRoute.requestLayout();
                viewRoute.setAlpha(1.0f);

            } else {
                FloatingActionButton fabMasstransit = getView().findViewById(R.id.fabMasstransit);
                fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

                CardView viewRoute = getView().findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.GONE);
            }
        } else {

            if (routes != null && !routes.isEmpty()) {
                //PolylineMapObject polylineMapObject = routes.get(0).getWayPoints()getGeometry();
                // mapObjects.addPolyline(routes.get(0).getWayPoints());

                PolylineMapObject polylineMapObject = mapObjects.addPolyline(routes.get(0).getGeometry());
                polylineMapObject.setStrokeColor(0xFF09692b);
                polylineMapObject.setDashLength(10.0F);
                polylineMapObject.setGapLength(10.0F);

                //mapObjects.addPolyline(routes.get(0).getGeometry());
                TextView textDistance = getView().findViewById(R.id.textDistance);
                textDistance.setVisibility(View.VISIBLE);
                String textDistanceStr = "Расстояние: " + routes.get(0).getMetadata().getWeight().getWalkingDistance().getText();
                textDistance.setText(textDistanceStr);

                TextView textTime = getView().findViewById(R.id.textTime);
                String textTimeStr = "Время: " + routes.get(0).getMetadata().getWeight().getTime().getText();
                textTime.setText(textTimeStr);

                CardView viewRoute = getView().findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.VISIBLE);

                CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) viewRoute.getLayoutParams();
                tParams.setMargins(20, 20, 20, 20);
                viewRoute.requestLayout();
                viewRoute.setAlpha(1.0f);


                MapView mapview = getView().findViewById(R.id.mapview);
                BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
                CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);
                mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null);
            } else {
                FloatingActionButton fabPedestrian = getView().findViewById(R.id.fabPedestrian);
                fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
                CardView viewRoute = getView().findViewById(R.id.viewRoute);
                viewRoute.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onMasstransitRoutesError(Error error) {

        ProgressBar progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        FloatingActionButton fabMasstransit = getView().findViewById(R.id.fabMasstransit);
        FloatingActionButton fabPedestrian = getView().findViewById(R.id.fabPedestrian);
        FloatingActionButton fabDriving = getView().findViewById(R.id.fabDriving);


        fabDriving.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
        fabPedestrian.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));
        fabMasstransit.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(), R.color.fab_not_selected));

    }
    private void submitRequest() {
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