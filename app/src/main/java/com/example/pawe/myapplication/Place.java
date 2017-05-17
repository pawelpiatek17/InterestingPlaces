package com.example.pawe.myapplication;

import java.util.ArrayList;

/**
 * Created by Paweł on 2016-12-27.
 */

public class Place {
    private String name;
    private String imgName;
    private String address;
    private String description;
    private String latitude;
    private String longitude;
    private ArrayList<String> listOfImages;

    public Place(String name, String imgName, String address,
                 String description, String latitude, String longitude, ArrayList<String> listOfImages) {
        this.name = name;
        this.imgName = imgName;
        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.listOfImages = listOfImages;
    }

    public String getName() {
        return name;
    }

    public String getimgName() {
        return imgName;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public ArrayList<String> getlistOfImages() {
        return listOfImages;
    }
}
