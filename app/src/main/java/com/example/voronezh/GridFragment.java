package com.example.voronezh;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;

//import android.app.Fragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;


import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

public class GridFragment extends Fragment {
    interface OnFragmentSendDataGridListener {
        void onSendDataGrid(TypeObject data);
    }
    private OnFragmentSendDataGridListener fragmentSendDataGridListener;
    ArrayList<TypeObject> objects = new ArrayList<TypeObject>();
    String[] city = { "Воронежская область", "Воронеж", "Богучар", "Борисоглебск", "Бобров"};
    private static final String TAG_LOG = "myLogs";

    public GridFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    /*
    public static GridFragment newInstance(String param1, String param2) {
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putSerializable(TypeObject.class.getSimpleName(),typeObj);
        fragment.setArguments(args);
        return fragment;
    }
    */

    @Override
    public void onAttach(Context context) {
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
/*
        Spinner spinnerCity = (Spinner) view.findViewById(R.id.spinnerCity);
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, city);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinnerCity.setAdapter(adapter);
*/
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
        //GridLayoutManager staggeredGridLayoutManager = new GridLayoutManager(getContext(),2);
        //objectsGrid.setLayoutManager(staggeredGridLayoutManager);

       // int pixels = (int)ObjectTypeAdapter.convertDpToPixel(10,getContext());
      //  objectsGrid.addItemDecoration(new EqualSpacingItemDecoration(pixels));


        ObjectTypeAdapter.OnTypeObjectClickListener objectClickListener = new ObjectTypeAdapter.OnTypeObjectClickListener() {
            @Override
            public void onTypeObjecClick(TypeObject object, int position) {
                fragmentSendDataGridListener.onSendDataGrid(object);
                Log.d(TAG_LOG, object.getName());
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