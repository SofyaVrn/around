package com.example.voronezh;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Outline;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewOutlineProvider;
import android.widget.ImageView;


import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

public class GridFragment extends Fragment {
    interface OnFragmentSendDataGridListener {
        //сообщает MainActivity о нажатии тип объекта
        void onSendDataGrid(TypeObject data);
    }
    private OnFragmentSendDataGridListener fragmentSendDataGridListener;
    ArrayList<TypeObject> objects = new ArrayList<>();

    public GridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataGridListener = (OnFragmentSendDataGridListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // начальная инициализация списка
        setInitialData();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

        ImageView imgBanner = (ImageView) view.findViewById(R.id.imageBanner1);
        // Делаем округлым изображение верхнего баннера на главном экране
        ViewOutlineProvider provider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int curveRadius = 24;
                outline.setRoundRect(0, 0, view.getWidth(), (view.getHeight()), curveRadius);
            }
        };

        imgBanner.setOutlineProvider(provider);
        imgBanner.setClipToOutline(true);

        RecyclerView objectsGrid = (RecyclerView) view.findViewById(R.id.gridviewTypeObject);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        objectsGrid.setLayoutManager(staggeredGridLayoutManager);


        ObjectTypeAdapter.OnTypeObjectClickListener objectClickListener = new ObjectTypeAdapter.OnTypeObjectClickListener() {
            @Override
            public void onTypeObjecClick(TypeObject object, int position) {
                fragmentSendDataGridListener.onSendDataGrid(object);
            }
        };

        // создаем адаптер для типов объектов
        ObjectTypeAdapter objectAdapter = new ObjectTypeAdapter(getContext(), objects,objectClickListener);
        // устанавливаем адаптер, GridView заполняется данными из адаптера
        objectsGrid.setAdapter(objectAdapter);
        return view;
    }

    private void setInitialData(){
        // заполняем ArrayList<TypeObject> данными
        XmlPullParser xpp = getResources().getXml(R.xml.type_object);
        TypeObjectResourceParser parser = new TypeObjectResourceParser();
        if(parser.parse(xpp))
        {
            objects = parser.getTypeObjects();
        }
    }
}