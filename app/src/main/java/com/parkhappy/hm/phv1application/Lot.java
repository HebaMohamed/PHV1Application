package com.parkhappy.hm.phv1application;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by dell on 12/2/2016.
 */
public class Lot {
    public int id;
    public int number;
    public String name;
    public double lat;
    public double lng;
    public String description;
    public ArrayList<LotPriceBreakDownList> pricelist = new ArrayList<>();
    public double priceBasic;

    public String address1;
    public String address2;

    public String city;
    public String country;

    public double distance;

    public ArrayList<String> lotFeatures = new ArrayList<String>();

    public Lot(int id, String name, String address1, String city, String country, double lat, double lng, double priceBasic){
        this.id=id;
        this.name=name;
        this.address1=address1;
        this.city=city;
        this.country=country;
        this.lat=lat;
        this.lng=lng;
        this.priceBasic=priceBasic;
        Location l1 = new Location("");
        l1.setLatitude(MainActivity.lat);
        l1.setLongitude(MainActivity.lng);
        Location l2 = new Location("");
        l2.setLatitude(lat);
        l2.setLongitude(lng);
        double dist =(l1.distanceTo(l2))/1000;//km
        this.distance=round(dist,2);
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}

