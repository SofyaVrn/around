package com.example.voronezh;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";
    Set<String> favorites;
    SharedPreferences settings;
    APIRetrofitInterface apiInterface;
    List<Object> objects;
    interface OnFragmentSendDataFavoriteListener {
        //сообщаем MainActivity о нажатии на объект в избранном
        void onSendDataFavoriteObject(Object data,int position);
    }

    private FavoritesFragment.OnFragmentSendDataFavoriteListener fragmentSendDataFavoriteListener;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        //получаем список избранных из SharedPreferences
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
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
        sendRequestPreviewObjects(objects);

        ObjectAdapter objectAdapter = new ObjectAdapter(getContext(), objects,objectClickListener,true);

        // устанавливаем адаптер
        objectsList.setAdapter(objectAdapter);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // callback for drag-n-drop, false to skip this feature
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // удаляем объект из избранного если пользовалель смахнул его
                removeObjectFavorites(objectAdapter.getItem(viewHolder.getBindingAdapterPosition()),viewHolder.getBindingAdapterPosition());
                setEmptyView(view);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // делаем прозрачным при смахивании элемента в RecyclerView
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

    public void sendRequestPreviewObjects(List<Object> objects){
        //отправляет запрос серверу для получение списка preview объектов
        apiInterface = APIRetrofitClient.getClient().create(APIRetrofitInterface.class);

        ArrayList<String> arr_id = new ArrayList<>();
        for (Object object : objects) {

            arr_id.add(String.valueOf(object.getId()));
        }

        Call<PreviewObjects> call = apiInterface.getPreviewObjects(arr_id);

        Log.d("CALL",call.request().headers().toString()+"");

        call.enqueue(new Callback<PreviewObjects>() {
            @Override
            public void onResponse(@NonNull Call<PreviewObjects> call, @NonNull Response<PreviewObjects> response) {

                Log.d("TAGCODE",response.code()+"");
                Log.d("TAG",response.raw().protocol()+"");

                if (response.code() == 200) {
                    PreviewObjects resource = response.body();

                    List<PreviewObjects.PreviewList> previewList = resource.objects;

                    setPreviewObjects(previewList);
                }

            }

            @Override
            public void onFailure(@NonNull Call<PreviewObjects> call, @NonNull Throwable t) {
                Log.d("onFailure","onFailure");
                call.cancel();

            }
        });
    }

    public void setPreviewObjects(List<PreviewObjects.PreviewList> previewList) {
        //устанавливает preview объектам
        if (getView() != null) {
            RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
            ObjectAdapter objectAdapter = (ObjectAdapter) objectsList.getAdapter();

            for (PreviewObjects.PreviewList preview : previewList) {
                for (int i = 0; i < objectAdapter.getItemCount(); ++i) {
                    Object object = (Object) objectAdapter.getItem(i);
                    if (preview.id == object.getId()) {
                        object.setImgUrl(preview.path);
                        objectAdapter.notifyItemChanged(i);
                        break;
                    }

                }
            }
        }
    }

    public void removeObjectFavorites(Object selectedObject,int position) {
        //удаление объекта из избранного

        favorites.remove(String.valueOf(selectedObject.getId()));
        SharedPreferences.Editor prefEditor = settings.edit();

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();

        objects.remove(position);
        objectAdapter.notifyItemRemoved(position);
        prefEditor.remove(PREF_FAVORITES);
        prefEditor.apply();
        prefEditor.putStringSet(PREF_FAVORITES, favorites);
        prefEditor.apply();

        // используем Snackbar для отмены удаления объекта из избранного
        Snackbar snackbar = Snackbar.make(getView(), "Объект был удален из избранного.", Snackbar.LENGTH_LONG);
        BottomNavigationView bottom_navigation = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        snackbar.setAnchorView(bottom_navigation);
        snackbar.setAction("Отменить", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor prefEditor = settings.edit();
                favorites.add(String.valueOf(selectedObject.getId()));
                prefEditor.remove(PREF_FAVORITES);
                prefEditor.apply();
                prefEditor.putStringSet(PREF_FAVORITES, favorites);
                prefEditor.apply();

                objectAdapter.restoreItem(selectedObject, position);
                objectsList.scrollToPosition(position);
                setEmptyView(getView());
            }
        });

        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    public void removeObjectFavorite(int position) {
        //удаляет объект из RecyclerView избранного
        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
        objects.remove(position);
        objectAdapter.notifyItemRemoved(position);
        setEmptyView(getView());
    }

    public void addObjectFavorite(Object selectedObject, int position) {
        //добавляет объект в RecyclerView избранного
        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
        ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
        objectAdapter.restoreItem(selectedObject, position);
        objectsList.scrollToPosition(position);
        setEmptyView(getView());
    }

    public void setEmptyView(View view) {
        //устанавливает надпись если список избанного пуст
        RecyclerView objectsList = (RecyclerView) view.findViewById(R.id.objectsList);
        TextView emptyView = (TextView) view.findViewById(R.id.emptyView);
        if (objects.isEmpty()) {
            objectsList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            objectsList.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}