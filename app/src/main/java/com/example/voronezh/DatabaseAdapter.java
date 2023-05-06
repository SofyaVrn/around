package com.example.voronezh;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DatabaseAdapter {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";
    Set<String> favorites;
    SharedPreferences settings;
    Context context;

    public DatabaseAdapter(Context context_){
        dbHelper = new DatabaseHelper(context_.getApplicationContext());
        dbHelper.create_db();
        context = context_;
    }

    public DatabaseAdapter open(){
        database = dbHelper.open();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public List<Object> getGlobalObjectsFilter(String filter){
        // возвращает список объектов соотвествующих фильтру filter, поиск идет по всей базе
        ArrayList<Object> objects = new ArrayList<>();

        String[] strParam = null;
        String strQuery = "SELECT * FROM %s WHERE";
        String[] filters = null;
        String query = String.format(strQuery,DatabaseHelper.TABLE);

        filter = filter.trim();
        filter = filter.replaceAll("\\s+", " ");
        filters = filter.split(" ");

        if(!filter.isEmpty() && !filter.trim().isEmpty()) {
            strParam = new String[filters.length];
            for (int i = 0; i < filters.length; i++) {
                strParam[i] = "%" + filters[i] + "%";
                if (i == 0) {
                    query += " %s LIKE ?";
                    query = String.format(query, DatabaseHelper.COLUMN_NAME);
                } else {
                    query += " AND %s LIKE ?";
                    query = String.format(query, DatabaseHelper.COLUMN_NAME);
                }
            }

            Cursor cursor = database.rawQuery(query, strParam);
            while (cursor.moveToNext()){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEBSITE));
                int environ = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENVIRON));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
                objects.add(new Object(id,name,address,description,environ,location,type,phone,email,website));
            }
            cursor.close();
        }
        return  objects;
    }

    public List<Object> getObjectsFilter(int typeObject,String filter,boolean isAccess){
        // возвращает список объектов соотвествующих фильтру filter, поиск идет с учетом типа объекта typeObject
        // и наличия доступной среды isAccess
        ArrayList<Object> objects = new ArrayList<>();

        //массив параметров передающихся в запрос
        String[] strParam;
        String strQuery = "SELECT * FROM %s WHERE %s=?";
        String[] filters = null;
        String query = String.format(strQuery,DatabaseHelper.TABLE, DatabaseHelper.COLUMN_TYPE);

        filter = filter.trim();
        filter = filter.replaceAll("\\s+", " ");
        filters = filter.split(" ");

        if(!filter.isEmpty() && !filter.trim().isEmpty()) {
            if(isAccess) { strParam = new String[filters.length + 2];} else {
                strParam = new String[filters.length + 1];
            }
            strParam[0] = String.valueOf(typeObject);
            for (int i = 0; i < filters.length; i++) {
                strParam[i+1] = "%" + filters[i] + "%";
                if (i == 0) {
                    query += " AND (%s LIKE ?";
                    query = String.format(query, DatabaseHelper.COLUMN_NAME);
                } else {
                    query += " AND %s LIKE ?";
                    query = String.format(query, DatabaseHelper.COLUMN_NAME);
                }
            }
            query += ")";
        } else {
            if(isAccess) {strParam = new String[2];} else {
                strParam = new String[1];
            }
            strParam[0] = String.valueOf(typeObject);
        }

        if(isAccess) {
            query += " AND %s=?";
            query = String.format(query, DatabaseHelper.COLUMN_ENVIRON);
            strParam[strParam.length-1] = "1";
        }

        Cursor cursor = database.rawQuery(query, strParam);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
            String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEBSITE));
            int environ = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENVIRON));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
            objects.add(new Object(id,name,address,description,environ,location,type,phone,email,website));
        }
        cursor.close();
        return  objects;
    }

    public List<Object> getObjects(int typeObject,boolean isAccess){
        // возвращает список объектов с учетом типа объекта typeObject
        // и наличия доступной среды isAccess
        ArrayList<Object> objects = new ArrayList<>();
        Cursor cursor;
        if(!isAccess) {
            String query = String.format("SELECT * FROM %s WHERE %s=?", DatabaseHelper.TABLE, DatabaseHelper.COLUMN_TYPE);
            cursor = database.rawQuery(query, new String[]{String.valueOf(typeObject)});
        } else{
            String query = String.format("SELECT * FROM %s WHERE %s=? AND %s=?", DatabaseHelper.TABLE, DatabaseHelper.COLUMN_TYPE, DatabaseHelper.COLUMN_ENVIRON);
            cursor = database.rawQuery(query, new String[]{String.valueOf(typeObject),"1"});
        }
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
            String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEBSITE));
            int environ = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENVIRON));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
           // Log.d("name",name);
            objects.add(new Object(id,name,address,description,environ,location,type,phone,email,website));
        }
        cursor.close();
        return  objects;
    }

    public List<Object> getObjectsFavorite(){
        // возвращает список объектов которые находятся в избранном
        ArrayList<Object> objects = new ArrayList<>();

        settings = context.getSharedPreferences(PREFS_FILE, context.MODE_PRIVATE);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());

        String[] favotitesArray = {};
        favotitesArray = favorites.toArray(new String[favorites.size()]);
        String str_q = "";
        for(int i=0; i<favotitesArray.length;i++) {
            if (i==0) {
                str_q = "?";
            } else {
                str_q += ", ?";
            }
        }
        Cursor cursor;
        String strQuery = "SELECT * FROM %s WHERE %s in (" + str_q +")";
        String query = String.format(strQuery, DatabaseHelper.TABLE, DatabaseHelper.COLUMN_ID);
        cursor = database.rawQuery(query, favotitesArray);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
            String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEBSITE));
            int environ = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENVIRON));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
            objects.add(new Object(id,name,address,description,environ,location,type,phone,email,website));
        }

        return  objects;
    }

    public List<Object> getObjectsSearch(){
        // возвращает список всех объектов в базе
        ArrayList<Object> objects = new ArrayList<>();
        Cursor cursor;

        String query = String.format("SELECT * FROM %s", DatabaseHelper.TABLE);
        cursor = database.rawQuery(query, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
            String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEBSITE));
            int environ = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ENVIRON));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
            // Log.d("name",name);
            objects.add(new Object(id,name,address,description,environ,location,type,phone,email,website));
        }
        cursor.close();
        return  objects;
    }

}
