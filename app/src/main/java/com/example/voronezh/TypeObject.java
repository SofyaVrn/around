package com.example.voronezh;

import java.io.Serializable;

public class TypeObject implements Serializable {
    private String nameType; // название типа

    private String nameTypeUppercase; // название типа большими буквами
    private int idType;  // id типа
    private String imgResource; // изображение
    private int heightImg; // высота изображения

    public String getName() {
        return this.nameType;
    }

    public void setName(String name) {
        this.nameType = name;
    }

    public int getHeight() {
        return this.heightImg;
    }

    public void setHeight(int height) {
        this.heightImg = height;
    }

    public String getNameUppercase() {
        return this.nameTypeUppercase;
    }

    public void setNameUppercase(String name) {
        this.nameTypeUppercase = name;
    }

    public int getIdType() {
        return this.idType;
    }

    public void setIdType(int id) {
        this.idType = id;
    }

    public String getImgResource() {
        return this.imgResource;
    }

    public void setImgResource(String img) {
        this.imgResource = img;
    }

}
