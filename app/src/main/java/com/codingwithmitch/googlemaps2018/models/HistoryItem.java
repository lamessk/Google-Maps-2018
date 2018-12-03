package com.codingwithmitch.googlemaps2018.models;


import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class HistoryItem {

    private String address;
    private String price;
    private @ServerTimestamp
    Date timestamp;

    public HistoryItem(String address, String price,  Date timestamp) {
        this.address = address;
        this.price = price;
        this.timestamp = timestamp;
    }

    public HistoryItem() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
