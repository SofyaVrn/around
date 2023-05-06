package com.example.voronezh;

public class SlideItemsModelClass {

    private String featuredImage;

    public SlideItemsModelClass(String urlImg) {
        this.featuredImage = urlImg;
    }

    public String getFeatured_image() {
        return featuredImage;
    }


    public void setFeatured_image(String featured_image) {
        this.featuredImage = featured_image;
    }

}
