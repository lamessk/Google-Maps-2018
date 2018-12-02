package com.codingwithmitch.googlemaps2018.models;

public class ParkingSpot {

    private String pAddress;
    private String pPrice;
    private String pAvailability;

    public String getpAddress() {
        return pAddress;
    }

    public void setpAddress(String pAddress) {
        this.pAddress = pAddress;
    }

    public String getpPrice() {
        return pPrice;
    }

    public void setpPrice(String pPrice) {
        this.pPrice = pPrice;
    }

    public String getpAvailability() {
        return pAvailability;
    }

    public void setpAvailability(String pAvailability) {
        this.pAvailability = pAvailability;
    }

    public String getpUserID() {
        return pUserID;
    }

    public void setpUserID(String pUserID) {
        this.pUserID = pUserID;
    }

    private String pUserID;

    public ParkingSpot(String pAddress, String pPrice, String pAvailability, String pUserID) {
        this.pAddress = pAddress;
        this.pPrice = pPrice;
        this.pAvailability = pAvailability;
        this.pUserID = pUserID;
    }

    public ParkingSpot() {}


}
