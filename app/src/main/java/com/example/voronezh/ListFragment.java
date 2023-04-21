package com.example.voronezh;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

//import android.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ListFragment extends Fragment {

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    TypeObject typeObject;
   // TextView text;
  //  ListView objectsList;
   // EditText userFilter;
  //  SwitchCompat switchAccessibility;
  ///  Button buttonBack;
    ArrayAdapter<Object> arrayAdapter;
    String filterText ="";
    boolean isAccessebility;
    Set<String> favorites;

    LinearLayout filterContainer;
    int containerHeight;

    private static final String PREFS_FILE = "Account";
    private static final String PREF_ACCESS = "Accessibility";
    private static final String PREF_FAVORITES = "Favorites";

    SharedPreferences settings;

    interface OnFragmentSendDataListListener {
        void onSendDataListBack();
        void onSendDataListObject(Object data,int position);
        TypeObject onGetDataTypeObject();
    }

    private ListFragment.OnFragmentSendDataListListener fragmentSendDataListListener;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListFragment() {
        // Required empty public constructor
    }

    public void listFragmentSetDataFilter(String filter, boolean isAccess) {
        Log.d("listFragmentSetDataFilter","listFragmentSetDataFilter");
        Log.d("filter",filter);
        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();

        if (typeObject == null) {
            Log.d("filter","typeObject is null");
        }
        List<Object> objects = adapter.getObjectsFilter(typeObject.getIdType(),filter,isAccess);

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

        TextView text = (TextView) getView().findViewById(R.id.textFragment);
        text.setText(typeObject.getName().toUpperCase());

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);

        int spanCount = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);

        objectsList.setLayoutManager(gridLayoutManager);
       // int pixels = (int)ObjectTypeAdapter.convertDpToPixel(4,getContext());
       // objectsList.addItemDecoration(new EqualSpacingItemDecoration(pixels));

        ObjectAdapter.OnObjectClickListener objectClickListener = new ObjectAdapter.OnObjectClickListener() {
            @Override
            public void onObjectClick(Object object, int position) {
                fragmentSendDataListListener.onSendDataListObject(object,position);
            }

            @Override
            public void onObjectFavoriteClick(Object object, int position) {
                setObjectFavorites(object,position);
            }
        };

        ObjectAdapter objectAdapter = new ObjectAdapter(getContext(), objects,objectClickListener,false);

        // устанавливаем адаптер
        objectsList.setAdapter(objectAdapter);

        //закрытие адаптера базы
        adapter.close();
    }
    public void listFragmentSetData() {
        Log.d("listFragmentSetData","listFragmentSetData");

        EditText userFilter = (EditText)getView().findViewById(R.id.objectFilter);
        userFilter.getText().clear();
        if (getArguments() != null) {
            typeObject = (TypeObject) getArguments().getSerializable(TypeObject.class.getSimpleName());
        }

        TextView text = (TextView) getView().findViewById(R.id.textFragment);
        text.setText(typeObject.getName().toUpperCase());
        Log.d("TAG_LIST", typeObject.getName());

        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();

        List<Object> objects = adapter.getObjects(typeObject.getIdType(),isAccessebility);

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

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);


        int spanCount = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);

        objectsList.setLayoutManager(gridLayoutManager);

       // int pixels = (int)ObjectTypeAdapter.convertDpToPixel(4,getContext());
      //  objectsList.addItemDecoration(new EqualSpacingItemDecoration(pixels));
        //objectsList.setHasFixedSize(true);


        ObjectAdapter.OnObjectClickListener objectClickListener = new ObjectAdapter.OnObjectClickListener() {
            @Override
            public void onObjectClick(Object object, int position) {
                Log.d("onObjectClick", object.getName());
                fragmentSendDataListListener.onSendDataListObject(object,position);
            }

            @Override
            public void onObjectFavoriteClick(Object object, int position) {
                //Log.d("onObjectClick", object.getName());
                //fragmentSendDataListListener.onSendDataListObject(object,position);
                setObjectFavorites(object,position);
            }
        };

        ObjectAdapter objectAdapter = new ObjectAdapter(getContext(), objects,objectClickListener,false);

        objectsList.setAdapter(objectAdapter);

        //закрытие адаптера базы
        adapter.close();
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
   // updateListObjects
   public void updateListObjects(int position) {
       RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);
       ObjectAdapter objectAdapter = (ObjectAdapter)objectsList.getAdapter();
       objectAdapter.notifyItemChanged(position);
   }

    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(TypeObject typeObj) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putSerializable(TypeObject.class.getSimpleName(),typeObj);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataListListener = (ListFragment.OnFragmentSendDataListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TypeObject.class.getSimpleName(), typeObject);
        outState.putString("filter", filterText);
//        Log.d("LOG_TAG", "onSaveInstanceState");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("ListFragment onCreate","ListFragment onCreate");
        if (savedInstanceState != null) {
            typeObject = (TypeObject) savedInstanceState.getSerializable(TypeObject.class.getSimpleName());
            filterText = savedInstanceState.getString("filter");
        }
        super.onCreate(savedInstanceState);

        settings = getContext().getSharedPreferences(PREFS_FILE, getContext().MODE_PRIVATE);
        isAccessebility = settings.getBoolean(PREF_ACCESS,false);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        Log.d("ListFragment onViewStateRestored","ListFragment onViewStateRestored");

        typeObject = fragmentSendDataListListener.onGetDataTypeObject();

        if (filterText.isEmpty()) {
            listFragmentSetData();
        } else {
            listFragmentSetDataFilter(filterText, isAccessebility);
        }


     /*   LinearLayout filterContainer = (LinearLayout) getView().findViewById(R.id.filter_container);
        filterContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int containerHeight = filterContainer.getMeasuredHeight();
        Log.d("containerHeight", String.valueOf(containerHeight));

      */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_list, container, false);
//------------
//------------
//------------

        EditText userFilter = (EditText)view.findViewById(R.id.objectFilter);

        SwitchCompat switchAccessibility = (SwitchCompat)view.findViewById(R.id.switchAccessibility);
        switchAccessibility.setText("Доступная среда");

        filterText = "";
       // isAccessebility = true;
        switchAccessibility.setChecked(isAccessebility);

        switchAccessibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Accessibility","AccessibilityON");
                    listFragmentSetDataFilter(filterText, true);
                    isAccessebility = true;
                    SharedPreferences.Editor prefEditor = settings.edit();
                    prefEditor.putBoolean(PREF_ACCESS, isAccessebility);
                    prefEditor.apply();
                } else {
                    Log.d("Accessibility","AccessibilityOFF");
                    listFragmentSetDataFilter(filterText, false);
                    isAccessebility = false;
                    SharedPreferences.Editor prefEditor = settings.edit();
                    prefEditor.putBoolean(PREF_ACCESS, isAccessebility);
                    prefEditor.apply();
                }
            }
        });

        // установка слушателя изменения текста
        userFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            // при изменении текста выполняем фильтрацию
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterText = s.toString();
                Log.d("ListFragment","onTextChanged");
                listFragmentSetDataFilter(filterText,isAccessebility);
            }
        });

        //скрытие клавиатуры после нажатия enter
        userFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT) {
                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        //скрытие клавиатуры еcли TextEdit теряет фокус
        userFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });


        Button buttonBack = (Button) view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                fragmentSendDataListListener.onSendDataListBack();
            }
        });

//-------

        filterContainer = (LinearLayout) view.findViewById(R.id.filter_container);
        RecyclerView objectsList = (RecyclerView) view.findViewById(R.id.objectsList);

        containerHeight = getHeightOfView(filterContainer);
        Log.d("containerHeight", String.valueOf(containerHeight));

        objectsList.setPadding(objectsList.getPaddingLeft(), containerHeight,
                objectsList.getPaddingRight(), objectsList.getPaddingBottom());

        //objectsList.setHasFixedSize(true);


        objectsList.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                filterContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                filterContainer.animate().translationY(-containerHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });





        return view;
    }

    private int getHeightOfView(View contentview) {
        contentview.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //contentview.getMeasuredWidth();
        return contentview.getMeasuredHeight();
    }

}