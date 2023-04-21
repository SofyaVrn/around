package com.example.voronezh;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ObjectTypeAdapter extends RecyclerView.Adapter<ObjectTypeAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<TypeObject> objects;
    Context context;

    interface OnTypeObjectClickListener{
        void onTypeObjecClick(TypeObject object, int position);
    }
    private final OnTypeObjectClickListener onClickTypeObjectListener;

    public ObjectTypeAdapter(Context context, List<TypeObject> objects,OnTypeObjectClickListener onClickTypeObjectListener) {
        this.objects = objects;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.onClickTypeObjectListener = onClickTypeObjectListener;
    }

    @Override
    public ObjectTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.cell_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ObjectTypeAdapter.ViewHolder holder, int position) {
        TypeObject object = objects.get(position);

        try (InputStream inputStream = context.getAssets().open(object.getImgResource())) {
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            holder.imageView.setImageDrawable(drawable);
        } catch (IOException e){e.printStackTrace();}

        // Устанавливаем округлые углы у картинки
        ViewOutlineProvider provider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int curveRadius = 24;
                outline.setRoundRect(0, 0, view.getWidth(), (view.getHeight()), curveRadius);
            }
        };
        holder.imageView.setOutlineProvider(provider);
        holder.imageView.setClipToOutline(true);

      //  float heightPixel = (float)object.getHeight();
        holder.imageView.getLayoutParams().height = (int)convertDpToPixel(object.getHeight(),this.context);
        holder.nameView.setText(object.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickTypeObjectListener.onTypeObjecClick(object, holder.getBindingAdapterPosition());
            }
        });
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView nameView;
        ViewHolder(View view){
            super(view);
            nameView = view.findViewById(R.id.textObject);
            imageView = view.findViewById(R.id.imageObject);
        }
    }
}
