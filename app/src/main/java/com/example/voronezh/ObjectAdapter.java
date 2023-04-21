package com.example.voronezh;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;


public class ObjectAdapter extends  RecyclerView.Adapter<ObjectAdapter.ViewHolder>{
    private LayoutInflater inflater;
 //   private int layout;
    private List<Object> objects;
    private boolean isVisibilityType;
    private ArrayList<TypeObject> objectsType;

    Context context;

    private static final String PREFS_FILE = "Account";
    private static final String PREF_FAVORITES = "Favorites";
    Set<String> favorites;
    SharedPreferences settings;

    interface OnObjectClickListener{
        void onObjectClick(Object object, int position);
        void onObjectFavoriteClick(Object object, int position);
    }
    private final ObjectAdapter.OnObjectClickListener onClickObjectListener;

    public ObjectAdapter(Context context, List<Object> objects, ObjectAdapter.OnObjectClickListener onClickObjectListener,boolean isVisibilityType) {
        this.objects = objects;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.onClickObjectListener = onClickObjectListener;

        this.isVisibilityType = isVisibilityType;
        if ( this.isVisibilityType) {
            XmlPullParser xpp = context.getResources().getXml(R.xml.type_object);
            TypeObjectResourceParser parser = new TypeObjectResourceParser();
            if (parser.parse(xpp)) {
                objectsType = parser.getTypeObjects();
            }
        }

    }


    @Override
    public ObjectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_list, parent, false);
        return new ObjectAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ObjectAdapter.ViewHolder holder, int position) {

        Object object = objects.get(position);

        holder.nameView.setText(object.getName());
        holder.addressView.setText(object.getAddress());

        ViewOutlineProvider provider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int curveRadius = 24;
                outline.setRoundRect(0, 0, view.getWidth(), (view.getHeight()), curveRadius);
            }
        };

        try (InputStream inputStream = context.getAssets().open(object.getImgUrl())) {
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            holder.imgView.setImageDrawable(drawable);

            holder.imgView.setOutlineProvider(provider);
            holder.imgView.setClipToOutline(true);

        } catch (IOException e){e.printStackTrace();}

       // String imgUrl = "https://static-maps.yandex.ru/1.x/?l=map&"+pointsUrl+",pm2rdl&z=14&" + sizeUrl;
       /* String imgUrl = "https://placesvrn.sourceforge.io/images/" + object.getId() +".png";

        Log.d("imgUrl :: ", imgUrl);
        Picasso.with(context).load(imgUrl).centerCrop().fit().into(holder.imgView);
*/

        settings = context.getSharedPreferences(PREFS_FILE, context.MODE_PRIVATE);
        favorites = settings.getStringSet(PREF_FAVORITES, new HashSet<String>());
        boolean isContains = favorites.contains(String.valueOf(object.getId()));

        if (isContains){
            holder.imgFavorite.setImageResource(R.drawable.favorite);
            holder.imgFavorite.setColorFilter(Color.RED);
        } else {
            holder.imgFavorite.setImageResource(R.drawable.favorite_border);
            holder.imgFavorite.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        holder.imgFavorite.setOutlineProvider(provider);
        holder.imgFavorite.setClipToOutline(true);

        if(object.getEnviron() == 1) {
            holder.imgAccess.setVisibility(View.VISIBLE);
            holder.imgAccess.setImageResource(R.drawable.accessibility);
        } else {
            holder.imgAccess.setVisibility(View.INVISIBLE);
        }

        holder.imgAccess.setOutlineProvider(provider);
        holder.imgAccess.setClipToOutline(true);

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickObjectListener.onObjectClick(object, holder.getBindingAdapterPosition());
            }
        });

        holder.imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(activity, "clicked on " +position, Toast.LENGTH_SHORT).show();
                onClickObjectListener.onObjectFavoriteClick(object, holder.getBindingAdapterPosition());
            }
        });

        if (this.isVisibilityType) {
            for (TypeObject objectType : objectsType) {
                if(object.getType() == objectType.getIdType()) {
                    holder.typeView.setText(objectType.getName().toUpperCase());
                    break;
                }
            }
            holder.typeView.setVisibility(View.VISIBLE);
        } else {
            holder.typeView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return objects.size();
    }
    public Object getItem(int position) {
        return objects.get(position);
    }
    public void restoreItem(Object object, int position) {
        objects.add(position, object);
        notifyItemInserted(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgView;
        final ImageView imgFavorite;
        final ImageView imgAccess;
        final TextView nameView;
        final TextView addressView;
        final TextView typeView;
        ViewHolder(View view){
            super(view);
            nameView = view.findViewById(R.id.name);
            addressView = view.findViewById(R.id.address);
            typeView = view.findViewById(R.id.type);
            imgView = view.findViewById(R.id.imgObj);
            imgFavorite =  view.findViewById(R.id.imgFavorite);
            imgAccess = view.findViewById(R.id.imgAccessibility);
        }
    }

}
