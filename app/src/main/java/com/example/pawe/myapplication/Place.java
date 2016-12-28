package com.example.pawe.myapplication;

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

    public Place() {
    }

    public Place(String name, String imgName, String address,
                 String description, String latitude, String longitude) {
        this.name = name;
        this.imgName = imgName;
        this.address = address;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getimgName() {
        return imgName;
    }

    public void setimgName(String imgName) {
        this.imgName = imgName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
