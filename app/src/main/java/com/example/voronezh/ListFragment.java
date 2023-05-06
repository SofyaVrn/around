package com.example.voronezh;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ListFragment extends Fragment {

    TypeObject typeObject;
    String filterText ="";
    boolean isAccessebility;
    Set<String> favorites;

    LinearLayout filterContainer;
    int containerHeight;
    APIRetrofitInterface apiInterface;


    private static final String PREFS_FILE = "Account";
    private static final String PREF_ACCESS = "Accessibility";
    private static final String PREF_FAVORITES = "Favorites";

    SharedPreferences settings;

    interface OnFragmentSendDataListListener {
        // сообщает MainActivity что пользователь кликнул на кнопку назад
        void onSendDataListBack();
        // сообщает MainActivity что пользователь кликнул по объекту
        void onSendDataListObject(Object data,int position);
        //получает для ListFragment  данные по объекты из MainActivity
        TypeObject onGetDataTypeObject();
    }

    private ListFragment.OnFragmentSendDataListListener fragmentSendDataListListener;

    public ListFragment() {
        // Required empty public constructor
    }

    public void listFragmentSetDataFilter(String filter, boolean isAccess) {
        //заполняет RecyclerView данными об объектах выбранных по фильтру filter
        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();

        //получаем из базы список объектов
        List<Object> objects = adapter.getObjectsFilter(typeObject.getIdType(),filter,isAccess);

        sendRequestPreviewObjects(objects);

        TextView text = (TextView) getView().findViewById(R.id.textFragment);
        text.setText(typeObject.getName().toUpperCase());

        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);

        //зависимо от ориентации устройства выставляем сколько колонок отображать в RecyclerView
        int spanCount = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);

        objectsList.setLayoutManager(gridLayoutManager);

        ObjectAdapter.OnObjectClickListener objectClickListener = new ObjectAdapter.OnObjectClickListener() {
            @Override
            public void onObjectClick(Object object, int position) {
                //пользователь кликнул по объекту, сообщаем это MainActivity
                fragmentSendDataListListener.onSendDataListObject(object,position);
            }

            @Override
            public void onObjectFavoriteClick(Object object, int position) {
                //пользователь кликнул по иконке избранного в объекте
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
        //заполняет RecyclerView данными об объектах без фильтра

        EditText userFilter = (EditText)getView().findViewById(R.id.objectFilter);
        userFilter.getText().clear();
        if (getArguments() != null) {
            typeObject = (TypeObject) getArguments().getSerializable(TypeObject.class.getSimpleName());
        }

        TextView text = (TextView) getView().findViewById(R.id.textFragment);
        text.setText(typeObject.getName().toUpperCase());

        DatabaseAdapter adapter = new DatabaseAdapter(getContext());
        adapter.open();

        //получаем из базы список объектов
        List<Object> objects = adapter.getObjects(typeObject.getIdType(),isAccessebility);

        sendRequestPreviewObjects(objects);
        RecyclerView objectsList = (RecyclerView) getView().findViewById(R.id.objectsList);

        //зависимо от ориентации устройства выставляем сколько колонок отображать в RecyclerView
        int spanCount = 1;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);

        objectsList.setLayoutManager(gridLayoutManager);


        ObjectAdapter.OnObjectClickListener objectClickListener = new ObjectAdapter.OnObjectClickListener() {
            @Override
            public void onObjectClick(Object object, int position) {
                //пользователь кликнул по объекту, сообщаем это MainActivity
                fragmentSendDataListListener.onSendDataListObject(object,position);
            }

            @Override
            public void onObjectFavoriteClick(Object object, int position) {
                //пользователь кликнул по иконке избранного в объекте
                setObjectFavorites(object,position);
            }
        };

        ObjectAdapter objectAdapter = new ObjectAdapter(getContext(), objects,objectClickListener,false);

        objectsList.setAdapter(objectAdapter);

        //закрытие адаптера базы
        adapter.close();
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
            public void onResponse(Call<PreviewObjects> call, Response<PreviewObjects> response) {

                Log.d("TAGCODE",response.code()+"");
                Log.d("TAG",response.raw().protocol()+"");

                if (response.code() == 200) {
                    PreviewObjects resource = response.body();

                    List<PreviewObjects.PreviewList> previewList = resource.objects;

                    setPreviewObjects(previewList);
                }

            }

            @Override
            public void onFailure(Call<PreviewObjects> call, Throwable t) {
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

    public void setObjectFavorites(Object selectedObject,int position) {
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
        objectAdapter.notifyItemChanged(position);
        prefEditor.remove(PREF_FAVORITES);
        prefEditor.commit();
        prefEditor.putStringSet(PREF_FAVORITES, favorites);
        prefEditor.commit();

    }

   public void updateListObjects(int position) {
        // обновляет RecyclerView по позиции в списке
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            fragmentSendDataListListener = (ListFragment.OnFragmentSendDataListListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //сохраняем данные перед уничтожением Activity
        outState.putSerializable(TypeObject.class.getSimpleName(), typeObject);
        outState.putString("filter", filterText);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //получаем сохраненные данные из ранее созданной Activity
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
        // сообщаем MainActivity что ListFragment готов получить данные об объектах для отображения
        // на данном этапе ListFragment полностью создан
        typeObject = fragmentSendDataListListener.onGetDataTypeObject();

        if (filterText.isEmpty()) {
            listFragmentSetData();
        } else {
            listFragmentSetDataFilter(filterText, isAccessebility);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);


        EditText userFilter = (EditText)view.findViewById(R.id.objectFilter);

        SwitchCompat switchAccessibility = (SwitchCompat)view.findViewById(R.id.switchAccessibility);
        switchAccessibility.setText("Доступная среда");

        filterText = "";
        switchAccessibility.setChecked(isAccessebility);

        switchAccessibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // пользователь включил доступную среду
                    listFragmentSetDataFilter(filterText, true);
                    isAccessebility = true;
                    SharedPreferences.Editor prefEditor = settings.edit();
                    prefEditor.putBoolean(PREF_ACCESS, isAccessebility);
                    prefEditor.apply();
                } else {
                    //пользователь выключил доступную среду
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


        //кнопка назад (по дефолту скрыта)
        Button buttonBack = (Button) view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                fragmentSendDataListListener.onSendDataListBack();
            }
        });

        // контейнер с фильтром для поиска, будет скрываться при движении вверх и показываться при движенри вниз
        filterContainer = (LinearLayout) view.findViewById(R.id.filter_container);
        RecyclerView objectsList = (RecyclerView) view.findViewById(R.id.objectsList);

        containerHeight = getHeightOfView(filterContainer);

        objectsList.setPadding(objectsList.getPaddingLeft(), containerHeight,
                objectsList.getPaddingRight(), objectsList.getPaddingBottom());


        objectsList.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                //показываем контейнер с фильтром
                filterContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                // скрываем контейнер с фильтром
                filterContainer.animate().translationY(-containerHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });

        return view;
    }

    private int getHeightOfView(View contentview) {
        //получает высоту требуемого View
        contentview.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return contentview.getMeasuredHeight();
    }

}