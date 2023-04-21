package com.example.voronezh;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";
    Set<String> favorites;
    SharedPreferences settings;
    List<Object> objects;
    interface OnFragmentSendDataFavoriteListener {
        //void onSendDataListBack();
        void onSendDataFavoriteObject(Object data,int position);
        //TypeObject onGetDataTypeObject();
    }

    private FavoritesFragment.OnFragmentSendDataFavoriteListener fragmentSendDataFavoriteListener;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataFavoriteListener = (FavoritesFragment.OnFragmentSendDataFavoriteListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getContext().getSharedPreferences(PREFS_FILE, getContext().MODE_PRIVATE);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_favorites, container, false);
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        RecyclerView objectsList = (RecyclerView) view.findViewById(R.id.objectsList);

        int spanCount = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);

        objectsList.setLayoutManager(gridLayoutManager);

        ObjectAdapter.OnObjectClickListener objectClickListener = new ObjectAdapter.OnObjectClickListener() {
            @Override
            public void onObjectClick(Object object, int position) {
                Log.d("onObjectClick", object.getName());
                fragmentSendDataFavoriteListener.onSendDataFavoriteObject(object,position);
            }

            @Override
            public void onObjectFavoriteClick(Object object, int position) {
                removeObjectFavorites(object,position);
            }
        };

        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();

        objects = adapter.getObjectsFavorite();

        for(Object object : objects){

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


        ObjectAdapter objectAdapter = new ObjectAdapter(getContext(), objects,objectClickListener,true);

        // устанавливаем адаптер
        objectsList.setAdapter(objectAdapter);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // callback for drag-n-drop, false to skip this feature
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                removeObjectFavorites(objectAdapter.getItem(viewHolder.getBindingAdapterPosition()),viewHolder.getBindingAdapterPosition());
                setEmptyView(view);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    float width = (float) viewHolder.itemView.getWidth();
                    float alpha = 1.0f - Math.abs(dX) / width;
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                            actionState, isCurrentlyActive);
                }
            }

            });
        swipeToDismissTouchHelper.attachToRecyclerView(objectsList);

        //закрытие адаптера базы
        adapter.close();
        setEmptyView(view);
        return view;
    }

    public void removeObjectFavorites(Object selectedObject,int position) {
        Log.d("setObjectFavorites","setObjectFavorites");
        //удаление объекта из избранного
        Log.d("selectedObject :: ", selectedObject.getName());
 //       settings = getContext().getSharedPreferences(PREFS_FILE, getContext().MODE_PRIVATE);
 //       favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

        favorites.remove(String.valueOf(selectedObject.getId()));
        SharedPreferences.Editor prefEditor = settings.edit();

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();

        objects.remove(position);
        objectAdapter.notifyItemRemoved(position);
        prefEditor.remove(PREF_FAVORITES);
        prefEditor.commit();
        prefEditor.putStringSet(PREF_FAVORITES, favorites);
        prefEditor.commit();

        Snackbar snackbar = Snackbar.make(getView(), "Объект был удален из избранного.", Snackbar.LENGTH_LONG);
        BottomNavigationView bottom_navigation = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        snackbar.setAnchorView(bottom_navigation);
        snackbar.setAction("Отменить", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor prefEditor = settings.edit();
                favorites.add(String.valueOf(selectedObject.getId()));
                prefEditor.remove(PREF_FAVORITES);
                prefEditor.commit();
                prefEditor.putStringSet(PREF_FAVORITES, favorites);
                prefEditor.commit();

                objectAdapter.restoreItem(selectedObject, position);
                objectsList.scrollToPosition(position);
                setEmptyView(getView());
            }
        });

        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    public void removeObjectFavorite(int position) {
        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
        objects.remove(position);
        objectAdapter.notifyItemRemoved(position);
        setEmptyView(getView());
    }

    public void addObjectFavorite(Object selectedObject, int position) {
        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
        objectAdapter.restoreItem(selectedObject, position);
        objectsList.scrollToPosition(position);
        setEmptyView(getView());
    }

    public void setEmptyView(View view) {
        RecyclerView objectsList = (RecyclerView) view.findViewById(R.id.objectsList);
        TextView emptyView = (TextView) view.findViewById(R.id.emptyView);
        if (objects.isEmpty()) {
            Log.d("emptyView","emptyView");
            objectsList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            Log.d("not emptyView","not emptyView");
            objectsList.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}