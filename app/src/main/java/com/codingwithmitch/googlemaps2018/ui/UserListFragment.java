package com.codingwithmitch.googlemaps2018.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codingwithmitch.googlemaps2018.R;
import com.codingwithmitch.googlemaps2018.adapters.CustomInfoWindowAdapter;
import com.codingwithmitch.googlemaps2018.models.HistoryItem;
import com.codingwithmitch.googlemaps2018.models.MyItem;
import com.codingwithmitch.googlemaps2018.models.ParkingSpot;
import com.codingwithmitch.googlemaps2018.models.PolylineData;
import com.codingwithmitch.googlemaps2018.models.User;
import com.codingwithmitch.googlemaps2018.models.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.codingwithmitch.googlemaps2018.Constants.MAPVIEW_BUNDLE_KEY;

public class UserListFragment extends Fragment implements
        OnMapReadyCallback, View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener {

    private static final String TAG = "UserListFragment";
    private static final float DEFAULT_ZOOM = 15f;

    //widgets
    private MapView mMapView;
    private EditText mSearchText;


    //vars
    private FirebaseFirestore mDb;
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<ParkingSpot> mParkingSpots = new ArrayList<>();
    private ArrayList<UserLocation> mUserPos = new ArrayList<>();
    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;
    private UserLocation mUserPosition;
    private ClusterManager<MyItem> mClusterManager;
    private ArrayList<MyItem> mClusterMarkers = new ArrayList<>();
    private GeoApiContext mGeoApiContext = null;
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            mUserList = getArguments().getParcelableArrayList(getString(R.string.intent_user_list));
            mParkingSpots = getArguments().getParcelableArrayList(getString(R.string.intent_user_locations));
            mUserPos = getArguments().getParcelableArrayList("intent_user_location");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        mMapView = (MapView) view.findViewById(R.id.user_list_map);
        view.findViewById(R.id.btn_reset_map).setOnClickListener(this);
        view.findViewById(R.id.ic_gps).setOnClickListener(this);
        mSearchText = (EditText) view.findViewById(R.id.input_search);

        initGoogleMap(savedInstanceState);
        setUserPosition();
        return view;
    }
    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolylinesData.size() > 0 ){
                    for(PolylineData polylineData : mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }

                double duration = 9999;
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }
                    mSelectedMarker.setVisible(false);
                }
            }
        });
    }

    private void addHistory(){

        DocumentReference newHistoryDoc = mDb
                .collection("History")
                .document();


        Marker mMarker = mTripMarkers.get(0);
        HistoryItem historyItem = new HistoryItem();

        historyItem.setAddress(mMarker.getTitle());
        historyItem.setPrice(mMarker.getSnippet());
        historyItem.setTimestamp(null);


        newHistoryDoc.set(historyItem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    resetSelectedMarker();
                }else{
                    View parentLayout = mMapView.findViewById(android.R.id.content);
                    Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeTripMarker(){
        for(Marker marker : mTripMarkers){
            marker.remove();
        }
    }

    private void resetSelectedMarker(){
        if(mSelectedMarker != null){
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarker();
        }
    }

    private void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        //For showing all possible routes instead of only most efficient
        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserPosition.getGeo_point().getLatitude(),
                        mUserPosition.getGeo_point().getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    private void resetMap(){
        if(mGoogleMap != null) {
            mGoogleMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolylinesData.size() > 0){
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    private void addMapMarkers() {
        if (mGoogleMap != null) {

            resetMap();
            mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getContext()));

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<MyItem>(getActivity().getApplicationContext(), mGoogleMap);

                mGoogleMap.setOnCameraIdleListener(mClusterManager);
            }

            for (ParkingSpot parkingSpot : mParkingSpots) {

                Log.d(TAG, "addMapMarkers: location: " + parkingSpot.getGeo_point().toString());
                try {

                    String title = parkingSpot.getpUserID().getUsername() + "'s Parking Stall";
                    String snippet = "$" + parkingSpot.getpPrice() + " /day" + "\n"
                            + "Availability: " + parkingSpot.getpAvailability();

                    MyItem newClusterMarker = new MyItem(
                            parkingSpot.getGeo_point().getLatitude(), parkingSpot.getGeo_point().getLongitude(),
                            title,
                            snippet
                    );

                    mClusterManager.addItem(newClusterMarker);
                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();

            setCameraView();
        }
    }

    private void setCameraView() {

        //Define top right corner and bottom left corner for boundary
        //Overall map view window takes up area of 0.2 x 0.2 = 0.4 latLng
        double bottomBoundary = mUserPosition.getGeo_point().getLatitude() - 0.1;
        double leftBoundary = mUserPosition.getGeo_point().getLongitude() - 0.1;

        double topBoundary = mUserPosition.getGeo_point().getLatitude() + 0.1;
        double rightBoundary = mUserPosition.getGeo_point().getLongitude() + 0.1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));

    }

    private void setUserPosition() {
        mUserPosition = mUserPos.get(0);
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        ||keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    geoLocate();
                }
                return false;
            }
        });

        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.maps_api_key))
                    .build();
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getContext());

        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }
    
    private void moveCamera(LatLng latLng, float zoom, String title){
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mGoogleMap.addMarker(options);
        
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        mGoogleMap = map;
        addMapMarkers();
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.setOnPolylineClickListener(this);
        mGoogleMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_reset_map: {
                addMapMarkers();
                break;
            }
            case R.id.ic_gps: {
                moveCamera(new LatLng(mUserPosition.getGeo_point().getLatitude(),
                        mUserPosition.getGeo_point().getLongitude()), DEFAULT_ZOOM,
                        "Your Location");
                break;
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {

        if(marker.getSnippet().contains("Duration")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = "Would you like to Park At " + marker.getTitle() + "?" + "\n" +
            "(Choosing park now will begin your session)";
            builder.setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton("Park Now", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            resetSelectedMarker();
                            mTripMarkers.add(marker);
                            addHistory();
                            giveDirections(marker);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void giveDirections(final Marker marker){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message = "Do you want to navigate to " + marker.getTitle() + "?";
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        resetSelectedMarker();
                        mSelectedMarker = marker;
                        calculateDirections(marker);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        for(PolylineData polylineData: mPolylinesData){
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(endLocation)
                .title(polylineData.getLeg().endAddress.toString())
                .snippet("Duration: " + polylineData.getLeg().duration));

                mTripMarkers.add(marker);
                marker.showInfoWindow();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

}