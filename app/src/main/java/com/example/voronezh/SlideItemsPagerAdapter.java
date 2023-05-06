package com.example.voronezh;



import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SlideItemsPagerAdapter extends PagerAdapter {

    private Context Mcontext;
    private List<SlideItemsModelClass> theSlideItemsModelClassList;


    public SlideItemsPagerAdapter(Context Mcontext, List<SlideItemsModelClass> theSlideItemsModelClassList) {
        this.Mcontext = Mcontext;
        this.theSlideItemsModelClassList = theSlideItemsModelClassList;
    }


    @NonNull
    @Override
    public java.lang.Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) Mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View sliderLayout = inflater.inflate(R.layout.the_items_layout,null);
        ImageView featured_image = sliderLayout.findViewById(R.id.my_featured_image);
        if (MainApplication.IMAGE_CACHING) {
            Picasso.with(container.getContext()).load(theSlideItemsModelClassList.get(position).getFeatured_image()).centerCrop().fit().placeholder(R.drawable.progress_animation).error(R.drawable.image_not_found).into(featured_image);
        } else {
            Picasso.with(container.getContext()).load(theSlideItemsModelClassList.get(position).getFeatured_image()).centerCrop().fit().placeholder(R.drawable.progress_animation).error(R.drawable.image_not_found).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).networkPolicy(NetworkPolicy.NO_CACHE).into(featured_image);
        }
        container.addView(sliderLayout);
        return sliderLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull java.lang.Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return theSlideItemsModelClassList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull java.lang.Object o) {
        return view == o;
    }
}
