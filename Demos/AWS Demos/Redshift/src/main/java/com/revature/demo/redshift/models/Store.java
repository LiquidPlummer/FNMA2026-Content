package com.revature.demo.redshift.models;

public class Store {
    private int storeId;
    private String city;
    private String region;

    public Store(int storeId, String city, String region) {
        this.storeId = storeId;
        this.city = city;
        this.region = region;
    }

    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    @Override
    public String toString() {
        return "Store{storeId=" + storeId + ", city='" + city + "', region='" + region + "'}";
    }
}