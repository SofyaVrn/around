package com.example.voronezh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

//import android.app.Fragment;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
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
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.logo.Alignment;
import com.yandex.mapkit.logo.HorizontalAlignment;
import com.yandex.mapkit.logo.VerticalAlignment;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.util.ArrayList;


import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ObjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ObjectFragment extends Fragment implements  DrivingSession.DrivingRouteListener {
    private Object object;
    Set<String> favorites;
    SharedPreferences settings;
    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private MapObjectCollection mapObjects = null;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private  Point ROUTE_START_LOCATION = null;
    private  Point ROUTE_END_LOCATION;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    PlacemarkMapObject markUserObject = null;


    interface OnFragmentSendDataObjectListener {
        void onSendDataObjectBack();
        Object onGetDataObject();
        void onUpdateListObjects();
        void onAddObjectFavorite(Object object);
        void onRemoveObjectFavorite();
    }

    private ObjectFragment.OnFragmentSendDataObjectListener fragmentSendDataObjectListener;

    public ObjectFragment() {
        // Required empty public constructor
    }

    public static boolean urlValidator(String url)
    {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (URISyntaxException exception) {
            return false;
        }
        catch (MalformedURLException exception) {
            return false;
        }
    }
    public void objectFragmentSetData() {
        // заполняет фрагмент объекта

        if (getArguments() != null) {
            object = (Object) getArguments().getSerializable(Object.class.getSimpleName());
        }
        NestedScrollView nestedScroll = (NestedScrollView) getView().findViewById(R.id.bottom_sheet_scroll);
        nestedScroll.fullScroll(NestedScrollView.FOCUS_UP);

/*
        Log.d("LOG::",object.getDescription());
        String s1 = object.getDescription();
        char[] chars = new char[s1.length()];
        for (int i = 0; i < s1.length(); i++) {
            chars[i] = s1.charAt(i);
            int n = Character.getNumericValue(chars[i]);
            Log.d("Character:",String.valueOf((int)chars[i]));
            Log.d("Character:",String.valueOf(chars[i]));
        }
*/

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
        }

        RelativeLayout relativeLayoutWebsite = (RelativeLayout) getView().findViewById(R.id.relativeLayoutWebsite);


        if(object.getWebsite().isEmpty() || !urlValidator(object.getWebsite())) {
            relativeLayoutWebsite.setVisibility(View.GONE);
        }
/*
        ImageButton imageButtonEmail = (ImageButton) getView().findViewById(R.id.imageButtonEmail);

        if(object.getEmail().isEmpty()) {
            imageButtonEmail.setVisibility(View.GONE);
        } else {
            if (imageButtonEmail.getVisibility() == View.GONE) {
                imageButtonEmail.setVisibility(View.VISIBLE);
            }
        }
*/
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

            //imageObject.setOutlineProvider(provider);
            //imageObject.setClipToOutline(true);
        } catch (IOException e){e.printStackTrace();}


        imgAccess.setOutlineProvider(provider);
        imgAccess.setClipToOutline(true);

        boolean isContains = favorites.contains(String.valueOf(object.getId()));

        ImageButton imageButtonFavorite = (ImageButton) getView().findViewById(R.id.imageButtonFavorite);

        if (isContains){
            imageButtonFavorite.setImageResource(R.drawable.favorite);
            imageButtonFavorite.setColorFilter(Color.RED);
        }

        LinearLayout llBottomSheet = (LinearLayout) getView().findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        AppBarLayout appBar = (AppBarLayout) getView().findViewById(R.id.appBar);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams)appBar.getLayoutParams();


        ImageView imageBaseline = (ImageView) getView().findViewById(R.id.imageBaseline);


        //получаем высоту экрана
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;

        //устанавливаем высоту нижнего экрана

      //  BottomNavigationView bottom_navigation = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        int pixels_bottom_navigation = (int)ObjectTypeAdapter.convertDpToPixel(56,getContext());
        int maxHeight = height - lp.height - pixels_bottom_navigation + imageBaseline.getDrawable().getIntrinsicHeight();
        bottomSheetBehavior.setMaxHeight(maxHeight);

        int pixels = (int)ObjectTypeAdapter.convertDpToPixel(250,getContext());
        int minHeight = height -lp.height - pixels_bottom_navigation - pixels + imageBaseline.getDrawable().getIntrinsicHeight();

        bottomSheetBehavior.setPeekHeight(minHeight);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        llBottomSheet.setMinimumHeight(maxHeight);

        TextView textNameAppBar = (TextView) getView().findViewById(R.id.textNameAppBar);
        textNameAppBar.setText(object.getName());
        textNameAppBar.setAlpha(0);


        //-----
        ImageView imageMapObject = (ImageView) getView().findViewById(R.id.imageMapObject);

      //  String sizeUrl = "size=" + String.valueOf(300) + "," + String.valueOf(imageMapObject.getLayoutParams().height);
        String sizeUrl = "size=" + String.valueOf(500) + "," + String.valueOf(300);

        Log.d("sizeUrl", sizeUrl);

        String[] points = null;
        points = object.getLocation().split(",");
        //Point pointObject = new Point(Double.valueOf(points[0]),Double.valueOf(points[1]));
       // ROUTE_END_LOCATION = pointObject;

        String pointsUrl= "pt="+ points[1].trim() + "," + points[0].trim();

        String imgUrl = "https://static-maps.yandex.ru/1.x/?l=map&"+pointsUrl+",pm2rdl&z=14&" + sizeUrl;

        Log.d("imgUrl", imgUrl);
        Picasso.with(getContext()).load(imgUrl).into(imageMapObject);

        imageMapObject.setOutlineProvider(provider);
        imageMapObject.setClipToOutline(true);


        //-----

        /*
        //получение координат для отрисовки на карте из Object
        String[] points = null;
        points = object.getLocation().split(",");
        Point pointObject = new Point(Double.valueOf(points[0]),Double.valueOf(points[1]));
        ROUTE_END_LOCATION = pointObject;

        MapView mapview = (MapView) getView().findViewById(R.id.mapview);

        mapview.getMap().move(
                new CameraPosition(pointObject, 16.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);
        //удаление всех меток с карты
        mapview.getMap().getMapObjects().clear();
        //установка и позиционирование метки объекта относительно низа середины картинки
        IconStyle istyle= new IconStyle();
        istyle.setAnchor(new PointF(0.5f,1.0f));
        PlacemarkMapObject mark = mapview.getMap().getMapObjects().addPlacemark(pointObject, ImageProvider.fromResource(getContext(), R.drawable.lable));
        mark.setIconStyle(istyle);
        //установка логотипа яндекс в правый верхний угол
        mapview.getMap().getLogo().setAlignment(new Alignment(HorizontalAlignment.LEFT,VerticalAlignment.TOP));

        */

    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        Log.d("ListFragment onViewStateRestored","ListFragment onViewStateRestored");

        object = fragmentSendDataObjectListener.onGetDataObject();
        objectFragmentSetData();

    }


    // TODO: Rename and change types and number of parameters
    public static ObjectFragment newInstance(Object obj) {

        ObjectFragment fragment = new ObjectFragment();
        Bundle args = new Bundle();
        args.putSerializable(Object.class.getSimpleName(),obj);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            object = (Object) getArguments().getSerializable(Object.class.getSimpleName());
        }
        settings = getContext().getSharedPreferences(PREFS_FILE, getContext().MODE_PRIVATE);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());
        Log.d("ObjectFragment onCreate","onCreate");

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // method to get the location
       // getLastLocation();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataObjectListener = (ObjectFragment.OnFragmentSendDataObjectListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onStop() {
    //    MapView mapview = (MapView) getView().findViewById(R.id.mapview);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_object, container, false);

        NestedScrollView nestedScroll = (NestedScrollView) view.findViewById(R.id.bottom_sheet_scroll);
        TextView textName = (TextView) view.findViewById(R.id.textName);
        TextView textNameAppBar = (TextView) view.findViewById(R.id.textNameAppBar);


        nestedScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY > textName.getScrollY() + textName.getHeight()){
                    textNameAppBar.animate().alpha(1).setDuration(100).start();
                }
                else{
                    textNameAppBar.animate().alpha(0).setDuration(100).start();
                }
            }
        });

        FloatingActionButton fabUserLocation = (FloatingActionButton) view.findViewById(R.id.fabUserLocation);

        LinearLayout llBottomSheet = (LinearLayout) view.findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        //получаем высоту экрана

        Button buttonBackToList = (Button) view.findViewById(R.id.buttonBackToList);
        buttonBackToList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                fragmentSendDataObjectListener.onSendDataObjectBack();
            }
        });


        ImageButton imageButtonCall = (ImageButton) view.findViewById(R.id.imageButtonCall);
        imageButtonCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String[] phones = null;
                phones = object.getPhone().split("\n");

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
                String url = object.getWebsite();
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
       /* ImageButton imageButtonRoute = (ImageButton) view.findViewById(R.id.imageButtonRoute);

        imageButtonRoute.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                setDriving();


                String email = object.getEmail();
                Intent emailIntent= new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                // Кому
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { email });
                getActivity().startActivity(emailIntent);
            }
        });

        */

        ImageButton imageButtonShare = (ImageButton) view.findViewById(R.id.imageButtonShare);

        imageButtonShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = object.getName();
                String address = object.getAddress();
                String[] points = null;
                points = object.getLocation().split(",");
                String url = "https://yandex.ru/maps/?pt="+points[1].trim()+","+points[0].trim()+"&z=16&l=map";
                Log.d("url",url);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, name + "\n" + address + "\n" + url);
                getActivity().startActivity(sendIntent);
            }
        });

        ImageButton imageButtonBack = (ImageButton) view.findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fragmentSendDataObjectListener.onSendDataObjectBack();
            }
        });

        ImageButton imageButtonFavorite = (ImageButton) view.findViewById(R.id.imageButtonFavorite);
        imageButtonFavorite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setFavorite();
                fragmentSendDataObjectListener.onUpdateListObjects();
            }
        });

        /*
        ImageView imageObject = (ImageView) view.findViewById(R.id.imageObject);
        imageObject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                setFavorite();
                fragmentSendDataObjectListener.onUpdateListObjects();
                return true;
            }
        });

         */
        ImageView imageMapObject = (ImageView) view.findViewById(R.id.imageMapObject);
        imageMapObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MAP Activity","MAP Activity");

                Intent intent = new Intent(getActivity(), MapObjectActivity.class);
              //  intent.putExtra("name", name);
              //  intent.putExtra("company", company);
              //  intent.putExtra("age", age);
                intent.putExtra(Object.class.getSimpleName(), object);
                startActivity(intent);
            }
        });

        return view;
    }


    public void setDriving() {

        if (ROUTE_START_LOCATION != null) {
            MapView mapview = (MapView) getView().findViewById(R.id.mapview);

            if (mapObjects != null) {
                mapview.getMap().getMapObjects().remove(mapObjects);
            }

            //ROUTE_START_LOCATION = userLocationLayer.cameraPosition().getTarget();
            drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
            mapObjects = mapview.getMap().getMapObjects().addCollection();
            submitRequest();
        }
    }

    @Override
    public void onDrivingRoutes(List<DrivingRoute> routes) {
       /* for (DrivingRoute route : routes) {
            mapObjects.addPolyline(route.getGeometry());
        }
        */
        if (routes != null && !routes.isEmpty()) {
            mapObjects.addPolyline(routes.get(0).getGeometry());

            MapView mapview = (MapView) getView().findViewById(R.id.mapview);
            BoundingBox box = BoundingBoxHelper.getBounds(routes.get(0).getGeometry());
            CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(box);
            mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0));


            /*BoundingBox boundingBox= new BoundingBox(ROUTE_START_LOCATION,ROUTE_END_LOCATION);
            CameraPosition boundingBoxPosition = mapview.getMap().cameraPosition(boundingBox);

            mapview.getMap().move(new CameraPosition(boundingBoxPosition.getTarget(), boundingBoxPosition.getZoom() - 0.8F, 0, 0));
*/
        }
    }
    @Override
    public void onDrivingRoutesError(Error error) {
        Log.d("Error onDrivingRoutesError","Error onDrivingRoutesError");
    /*    String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

      */
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

    public void setFavorite() {
        //добавление либо удаление объекта из избранного во фрагменте с объектом
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

        boolean isAdded = favorites.add(String.valueOf(object.getId()));

        ImageButton imageButtonFavorite = (ImageButton) getView().findViewById(R.id.imageButtonFavorite);

        if (isAdded) {
            Toast.makeText(getActivity().getApplicationContext(), "Объект добавлен в избранное", Toast.LENGTH_SHORT).show();
            imageButtonFavorite.setImageResource(R.drawable.favorite);
            imageButtonFavorite.setColorFilter(Color.RED);
            fragmentSendDataObjectListener.onAddObjectFavorite(object);
        } else {
            favorites.remove(String.valueOf(object.getId()));
            Toast.makeText(getActivity(), "Объект удален из избранного", Toast.LENGTH_SHORT).show();
            imageButtonFavorite.setImageResource(R.drawable.favorite_border);
            imageButtonFavorite.setColorFilter(getActivity().getResources().getColor(R.color.base_text), PorterDuff.Mode.SRC_ATOP);
            fragmentSendDataObjectListener.onRemoveObjectFavorite();
        }

        SharedPreferences.Editor prefEditor = settings.edit();

        prefEditor.remove(PREF_FAVORITES);
        prefEditor.commit();
        prefEditor.putStringSet(PREF_FAVORITES, favorites);
        prefEditor.commit();

    }

}