package com.example.voronezh;

import android.os.Bundle;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yandex.mapkit.MapKitFactory;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GridFragment.OnFragmentSendDataGridListener, ListFragment.OnFragmentSendDataListListener, ObjectFragment.OnFragmentSendDataObjectListener,FavoritesFragment.OnFragmentSendDataFavoriteListener {

    TypeObject typeObjectActivity;
    Object objectActivity;
    int positionListObject;

    final static String TAG_GRID = "FRAGMENT_GRID";
    final static String TAG_LIST = "FRAGMENT_LIST";
    final static String TAG_OBJECT = "FRAGMENT_OBJECT";
    final static String TAG_SEARCH = "FRAGMENT_SEARCH";

    final static String TAG_FAVORITES = "FRAGMENT_FAVORITES";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // сохраняем данные перед уничтожением Activity
        outState.putSerializable(TypeObject.class.getSimpleName(), typeObjectActivity);
        outState.putSerializable(Object.class.getSimpleName(), objectActivity);
        outState.putInt("positionListObject", positionListObject);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            //получаем сохраненные данные из ранее созданной Activity
            typeObjectActivity = (TypeObject) savedInstanceState.getSerializable(TypeObject.class.getSimpleName());
            objectActivity = (Object) savedInstanceState.getSerializable(Object.class.getSimpleName());
            positionListObject = (int) savedInstanceState.getInt("positionListObject");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            // при первом запуске программы
            // создаем фрагмент со списком типов объектов(музеи,театры)
            changeFragment(TAG_GRID);
        }

        MapKitFactory.initialize(this);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        // определяет на какую кнопку нажали в BottomNavigationView
        int itemId = item.getItemId();
        if (itemId == R.id.home) {
            changeFragment(TAG_GRID);
        } else if (itemId == R.id.search) {
            changeFragment(TAG_SEARCH);
        } else if (itemId == R.id.favorite) {
            changeFragment(TAG_FAVORITES);
        }
        return true;
    };

    public void changeFragment(String neededToShowFragmentTag) {
        // создает необходимый фрагмент
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<Fragment> existingFragments = fragmentManager.getFragments();
        Fragment neededToShowFragment = null;

        Fragment currentShownFragment = null;

        if (existingFragments != null) {
            for (Fragment fragment : existingFragments) {
                if (fragment.isVisible()) {
                    currentShownFragment = fragment;
                    break;
                }
            }
        }
        if (currentShownFragment == null || !currentShownFragment.getTag().equals(neededToShowFragmentTag)) {

            neededToShowFragment = fragmentManager.findFragmentByTag(neededToShowFragmentTag);

            if (neededToShowFragment == null) {
                switch (neededToShowFragmentTag) {
                    case TAG_GRID:
                        neededToShowFragment = new GridFragment();
                        break;
                    case TAG_LIST:
                        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                        neededToShowFragment = new ListFragment();
                        break;
                    case TAG_OBJECT:
                        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                        neededToShowFragment = new ObjectFragment();
                        break;
                    case TAG_SEARCH:
                        neededToShowFragment = new SearchFragment();
                        break;
                    case TAG_FAVORITES:
                        neededToShowFragment = new FavoritesFragment();
                        break;

                }
                fragmentTransaction.add(R.id.container, neededToShowFragment, neededToShowFragmentTag);
            }

            if (neededToShowFragmentTag == TAG_GRID || neededToShowFragmentTag == TAG_SEARCH || neededToShowFragmentTag == TAG_FAVORITES) {
                clearBackStack(fragmentManager);
                fragmentTransaction.replace(R.id.container, neededToShowFragment, neededToShowFragmentTag);
            } else {
                if (currentShownFragment != null) {
                    fragmentTransaction.hide(currentShownFragment);
                }
                fragmentTransaction.show(neededToShowFragment);
                fragmentTransaction.addToBackStack(null);
            }

            fragmentTransaction.commit();

        }
    }

    private void clearBackStack(FragmentManager fragmentManager) {
        //очищает  back stack
        FragmentManager mFragmentManager = getSupportFragmentManager();
        for(int i = 0; i < mFragmentManager.getBackStackEntryCount(); i++) {
            mFragmentManager.popBackStack();
        }
    }

    @Override
    public void onSendDataGrid(TypeObject selectedObjectType) {
        //создает или показывает фрагмент со списком объектов одного типа, например музеи

        typeObjectActivity = selectedObjectType;
        changeFragment(TAG_LIST);

    }

    @Override
    public void onSendDataListBack() {
        //при нажатии кнопки BACK показывает фрагмент со списком типов объектов (музеи, театры)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ListFragment fragmentList = (ListFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        GridFragment fragmentGrid = (GridFragment) getSupportFragmentManager().findFragmentByTag(TAG_GRID);
        clearBackStack(fragmentManager);
        fragmentTransaction.hide(fragmentList);
        fragmentTransaction.show(fragmentGrid).commit();
    }

    @Override
    public void onSendDataObjectBack() {
        //при нажатии кнопки BACK показывает фрагмент со списком объектов одного типа (список музеев)
        Log.d("MainActivity onSendDataObjectBack","onSendDataObjectBack");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ListFragment fragmentList = (ListFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragmentList != null) {
            ObjectFragment fragmentObj = (ObjectFragment) getSupportFragmentManager().findFragmentByTag(TAG_OBJECT);
            fragmentManager.popBackStack();
            fragmentTransaction.hide(fragmentObj);
            fragmentTransaction.show(fragmentList).commit();
        } else {
            FavoritesFragment fragmentFavorites = (FavoritesFragment) getSupportFragmentManager().findFragmentByTag(TAG_FAVORITES);
            ObjectFragment fragmentObj = (ObjectFragment) getSupportFragmentManager().findFragmentByTag(TAG_OBJECT);
            fragmentManager.popBackStack();
            fragmentTransaction.hide(fragmentObj);
            fragmentTransaction.show(fragmentFavorites).commit();
        }
    }

    @Override
    public void onSendDataListObject(Object selectedObject, int position) {
        // создает или показывает фрагмент с данными конкретного объекта
        objectActivity = selectedObject;
        positionListObject = position;
        changeFragment(TAG_OBJECT);
    }

    @Override
    public void onSendDataFavoriteObject(Object selectedObject, int position) {
        // создает или показывает фрагмент с данными конкретного объекта для избранного

        objectActivity = selectedObject;
        positionListObject = position;
        changeFragment(TAG_OBJECT);
    }

    @Override
    public TypeObject onGetDataTypeObject() {
        //возвращает последний выбранный тип объектов
        return typeObjectActivity;
    }

    @Override
    public Object onGetDataObject() {
        //возвращает последний выбранный объект
        return objectActivity;
    }
    @Override
    public void onUpdateListObjects() {
        //обновляет список объектов
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ListFragment fragmentList = (ListFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragmentList != null)  {
            fragmentList.updateListObjects(positionListObject);
        }
    }

    @Override
    public void onAddObjectFavorite(Object object) {
        //добавляет объект в избранное
        FavoritesFragment fragmentFavorites = (FavoritesFragment) getSupportFragmentManager().findFragmentByTag(TAG_FAVORITES);
        if (fragmentFavorites != null)  {
            fragmentFavorites.addObjectFavorite(object, positionListObject);
        }
    }
    @Override
    public void onRemoveObjectFavorite() {
        //удаляет объект из избранного
        FavoritesFragment fragmentFavorites = (FavoritesFragment) getSupportFragmentManager().findFragmentByTag(TAG_FAVORITES);
        if (fragmentFavorites != null)  {
            fragmentFavorites.removeObjectFavorite(positionListObject);
        }
    }
}