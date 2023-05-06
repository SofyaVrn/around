package com.example.voronezh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import java.net.IDN;
import java.util.ArrayList;


import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObjectFragment extends Fragment  {
    private Object object;
    Set<String> favorites;
    SharedPreferences settings;
    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";

    ViewPager page ;
    TabLayout tabLayout;
    List<SlideItemsModelClass> listItems;

    java.util.Timer timer = null;

    APIRetrofitInterface apiInterface;
    interface OnFragmentSendDataObjectListener {
        //сообщает что пользователь нажал кнопку назад
        void onSendDataObjectBack();
        //получает данные по объекту из MainActivity
        Object onGetDataObject();
        //сообщает что нужно обновить список объектов в ListFragment
        void onUpdateListObjects();
        //сообщает MainActivity о добавлении объекта в избранное
        void onAddObjectFavorite(Object object);
        //сообщает MainActivity об удалении объекта из избранного
        void onRemoveObjectFavorite();
    }

    private ObjectFragment.OnFragmentSendDataObjectListener fragmentSendDataObjectListener;

    public ObjectFragment() {
        // Required empty public constructor
    }

    public static boolean urlValidator(String url)
    {
        //проверяет что url рабочий
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

       // ImageView imageObject = (ImageView) getView().findViewById(R.id.imageObject);

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

        ViewOutlineProvider provider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int curveRadius = 24;
                outline.setRoundRect(0, 0, view.getWidth(), (view.getHeight()), curveRadius);
            }
        };


        String imgUrl = "https://around.sourceforge.io/imagesproject/" + object.getId() +".png";

       // Picasso.with(getContext()).load(imgUrl).centerCrop().fit().placeholder(R.drawable.progress_animation ).error(R.drawable.image_not_found).memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE).networkPolicy(NetworkPolicy.NO_CACHE).into(imageObject);

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

        //устанавливаем максимальную высоту нижнего экрана

      //  BottomNavigationView bottom_navigation = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        int pixels_bottom_navigation = (int)ObjectTypeAdapter.convertDpToPixel(56,getContext());
        int maxHeight = height - lp.height - pixels_bottom_navigation + imageBaseline.getDrawable().getIntrinsicHeight();
        bottomSheetBehavior.setMaxHeight(maxHeight);

        //установка высоты галлереи
        int pixels = (int)ObjectTypeAdapter.convertDpToPixel(350,getContext());
        //устанавливаем минимальную высоту нижнего экрана
        int minHeight = height -lp.height - pixels_bottom_navigation - pixels + imageBaseline.getDrawable().getIntrinsicHeight();

        bottomSheetBehavior.setPeekHeight(minHeight);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        llBottomSheet.setMinimumHeight(maxHeight);

        TextView textNameAppBar = (TextView) getView().findViewById(R.id.textNameAppBar);
        textNameAppBar.setText(object.getName());
        textNameAppBar.setAlpha(0);


        //отображает превью карты
        ImageView imageMapObject = (ImageView) getView().findViewById(R.id.imageMapObject);

        String sizeUrl = "size=" + String.valueOf(500) + "," + String.valueOf(300);

        String[] points = null;
        points = object.getLocation().split(",");

        String pointsUrl= "pt="+ points[1].trim() + "," + points[0].trim();

        String imgUrlMap = "https://static-maps.yandex.ru/1.x/?l=map&"+pointsUrl+",pm2rdl&z=14&" + sizeUrl;

        Picasso.with(getContext()).load(imgUrlMap).into(imageMapObject);

        imageMapObject.setOutlineProvider(provider);
        imageMapObject.setClipToOutline(true);

    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        // сообщаем MainActivity что ObjectFragment готов получить данные об объекте для отображения
        // на данном этапе ObjectFragment полностью создан
        object = fragmentSendDataObjectListener.onGetDataObject();
        objectFragmentSetData();
        sendRequestImageGallery();
    }

    public void sendRequestImageGallery(){
        //отправляет запрос серверу для получение списка фотографий объекта
        apiInterface = APIRetrofitClient.getClient().create(APIRetrofitInterface.class);
        Call<ImageObjectList> call = apiInterface.getImageObjectList(object.getId());

        Log.d("CALL",call.request().headers().toString()+"");

        Log.d("APIRetrofitClient13","APIRetrofitClient13");
        call.enqueue(new Callback<ImageObjectList>() {
            @Override
            public void onResponse(Call<ImageObjectList> call, Response<ImageObjectList> response) {

                Log.d("onResponse","onResponse");
                Log.d("TAGCODE",response.code()+"");

                Log.d("TAG",response.raw().protocol()+"");

                //String displayResponse = "";
                if (response.code() == 200) {
                    ImageObjectList resource = response.body();

                    List<ImageObjectList.ImagesList> imageList = resource.images;


                    String[] imagesUrl = new String[imageList.size()];
                    int i = 0;
                    for (ImageObjectList.ImagesList image : imageList) {
                        imagesUrl[i] = image.path;
                        i++;
                        Log.d("image", image.path);
                        Log.d("id", String.valueOf(image.id));
                    }
                    Log.d("SIZE", String.valueOf(imagesUrl.length));
                    setImagesOnSlider(imagesUrl);
                }

            }

            @Override
            public void onFailure(Call<ImageObjectList> call, Throwable t) {
                Log.d("onFailure","onFailure");
                call.cancel();

                if (getView()!= null) {
                    ImageView imageObject = (ImageView) getView().findViewById(R.id.imageObject);
                    imageObject.setImageResource(R.drawable.image_not_found);
                }
            }
        });
    }

    public void setImagesOnSlider(String[] imagesUrl) {
        //устанавливает полученные фотографии для просмотра в ViewPager
        Log.d("setImagesOnSlider","setImagesOnSlider");

        if (getView()!= null) {

            RelativeLayout relativeLayoutPager = (RelativeLayout) getView().findViewById(R.id.relativeLayoutPager);
            ImageView imageObject = (ImageView) getView().findViewById(R.id.imageObject);

            relativeLayoutPager.setVisibility(View.VISIBLE);
            imageObject.setVisibility(View.GONE);

            page = getView().findViewById(R.id.my_pager);
            tabLayout = getView().findViewById(R.id.my_tablayout);


            listItems = new ArrayList<>();
            for (String url : imagesUrl) {
                listItems.add(new SlideItemsModelClass(url));

            }

            if (listItems.size() < 2) tabLayout.setVisibility(View.INVISIBLE);
            SlideItemsPagerAdapter itemsPager_adapter = new SlideItemsPagerAdapter(getContext(), listItems);
            page.setAdapter(itemsPager_adapter);

            // timer = new java.util.Timer();
            // timer.scheduleAtFixedRate(new SlideTimer(),4000,8000);
            tabLayout.setupWithViewPager(page, true);
        }
    }

    public class SlideTimer extends TimerTask {
        @Override
        public void run() {

            ObjectFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (page.getCurrentItem()< listItems.size()-1) {
                        page.setCurrentItem(page.getCurrentItem()+1);
                    }
                    else
                        page.setCurrentItem(0);
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null )timer.cancel();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
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
       // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (getArguments() != null) {
            //получаем данные об объекте
            object = (Object) getArguments().getSerializable(Object.class.getSimpleName());
        }
        settings = getContext().getSharedPreferences(PREFS_FILE, getContext().MODE_PRIVATE);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

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
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //
        View view = inflater.inflate(R.layout.fragment_object, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        ImageView imageMapObject = (ImageView) view.findViewById(R.id.imageMapObject);
        imageMapObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapObjectActivity.class);
                intent.putExtra(Object.class.getSimpleName(), object);
                startActivity(intent);
            }
        });

        return view;
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