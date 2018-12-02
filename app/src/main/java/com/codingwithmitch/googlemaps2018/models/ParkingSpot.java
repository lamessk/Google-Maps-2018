package com.codingwithmitch.googlemaps2018.models;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

public class ParkingSpot implements Parcelable {

    private String pPrice;
    private String pAddress;
    private GeoPoint geo_point;
    private String pAvailability;
    private User pUserID;

    public ParkingSpot(String pAddress, String pPrice, String pAvailability, User pUserID, GeoPoint geo_point) {
        this.pAddress = pAddress;
        this.pPrice = pPrice;
        this.pAvailability = pAvailability;
        this.pUserID = pUserID;
        this.geo_point = geo_point;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public ParkingSpot() {}

    protected ParkingSpot(Parcel in) {
        pAddress = in.readString();
        pAddress = in.readParcelable(Address.class.getClassLoader());
        pPrice = in.readString();
        pAvailability = in.readString();
        pUserID = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<ParkingSpot> CREATOR = new Creator<ParkingSpot>() {
        @Override
        public ParkingSpot createFromParcel(Parcel in) {
            return new ParkingSpot(in);
        }

        @Override
        public ParkingSpot[] newArray(int size) {
            return new ParkingSpot[size];
        }
    };

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

    public User getpUserID() {
        return pUserID;
    }

    public void setpUserID(User pUserID) {
        this.pUserID = pUserID;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(pUserID, i);
    }
}
