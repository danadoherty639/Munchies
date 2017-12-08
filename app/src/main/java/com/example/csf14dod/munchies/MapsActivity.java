package com.example.csf14dod.munchies;

import android.*;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.csf14dod.munchies.R.id.bt_share_loc;
import static com.example.csf14dod.munchies.R.id.buttonViewMap;
import static com.example.csf14dod.munchies.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,

            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, RoutingListener{



        private GoogleMap mMap;
        private GoogleApiClient client;
        private LocationRequest locationRequest;
        private Location lastlocation;
        private Marker currentLocationmMarker;
        private Marker lastDestinationMarker;
        public static final int REQUEST_LOCATION_CODE = 99;
        int PROXIMITY_RADIUS = 10000;
        double latitude,longitude;  //holds current location coordinates
        private Button bt_back;
        //double lat, lng;            //hold current location
        double end_lat, end_lng;  //hold destination
        private List<Polyline> polylines;
        private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
        private Button bt_share_loc;
        private LatLng latLng;



    public MapsActivity() {
    }

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                checkLocationPermission();

            }
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(map);
            mapFragment.getMapAsync(this);

            polylines = new ArrayList<>();

    }




    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch(requestCode)
            {
                case REQUEST_LOCATION_CODE:
                    if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED)
                        {
                            if(client == null)
                            {
                                bulidGoogleApiClient();
                            }
                            mMap.setMyLocationEnabled(true);
                        }
                    }
                    else
                    {
                        Toast.makeText(this,"Permission Denied" , Toast.LENGTH_LONG).show();
                    }

                }

        }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                bulidGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
             mMap.setOnMarkerClickListener(this);
            mMap.setOnMarkerDragListener(this);
        }


        protected synchronized void bulidGoogleApiClient() {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            client.connect();

        }

        @Override
        public void onLocationChanged(Location location) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();
            lastlocation = location;
            if(currentLocationmMarker != null)
            {
                currentLocationmMarker.remove();

            }
            Log.d("lat = ",""+latitude);
            latLng = new LatLng(location.getLatitude() , location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentLocationmMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomBy(8));

            if(client != null)
            {
                LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
            }
        }

        public void onClick(View v)
        {
            Object dataTransfer[] = new Object[2];
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

            switch(v.getId())
            {
                case R.id.bt_search:
                    EditText tf_location = (EditText) findViewById(R.id.tf_location);
                    String location = tf_location.getText().toString();
                    List<android.location.Address> addressList;


                    if(!location.equals(""))
                    {
                        Geocoder geocoder = new Geocoder(this);

                        try {
                            addressList = geocoder.getFromLocationName(location, 5);

                            if(addressList != null)
                            {
                                for(int i = 0;i<addressList.size();i++)
                                {
                                    LatLng latLng = new LatLng(addressList.get(i).getLatitude() , addressList.get(i).getLongitude());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title(location);
                                    mMap.addMarker(markerOptions);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.bt_restaurant:
                    mMap.clear();
                    String resturant = "restuarant";
                    String url = getUrl(latitude, longitude, resturant);
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;

                    getNearbyPlacesData.execute(dataTransfer);
                    Toast.makeText(MapsActivity.this, "Showing Nearby Restaurants", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_distance:
                    mMap.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(end_lat, end_lng));
                    markerOptions.title("Destination");
                    markerOptions.draggable(true);

                    float results[] = new float[10];                             //stores distance
                    Location.distanceBetween(latitude, longitude, end_lat, end_lng, results);
                    markerOptions.snippet("Distance = "+results[0]);
                    lastDestinationMarker = mMap.addMarker(markerOptions);
                    break;
                case R.id.bt_share_loc:
                    Bundle args = new Bundle();
                    args.putParcelable("position", latLng);
                    Intent i = new Intent(this,ShareLocationActivity.class);
                    i.putExtra("bundle", args);
                    startActivity(i);
                    break;
                case R.id.bt_back:
                    startActivity(new Intent(this, ProfileActivity.class));

            }
        }


        private String getUrl(double latitude , double longitude , String nearbyPlace)
        {
            StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlaceUrl.append("location="+latitude+","+longitude);
            googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
            googlePlaceUrl.append("&type="+nearbyPlace);
            googlePlaceUrl.append("&sensor=true");
            googlePlaceUrl.append("&key="+"");

            Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

            return googlePlaceUrl.toString();
        }


        @Override
        public void onConnected(@Nullable Bundle bundle) {

            locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
            }
        }


        public boolean checkLocationPermission()
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
            {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
                }
                else
                {
                    ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
                }
                return false;

            }
            else
                return true;
        }


        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        end_lat = marker.getPosition().latitude;
        end_lng = marker.getPosition().longitude;
    }

    private void getDirection(){                 //gets directions and calls routing functionality to set route to endpoint

        LatLng start = new LatLng(latitude, longitude);
        LatLng end = new LatLng(end_lat, end_lng);
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(start, end)
                .build();
        routing.execute();

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocationmMarker.getPosition()));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(5f));
        Toast.makeText(MapsActivity.this, "Directions Received", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Whoops, Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int n) {

        mMap.clear();
        LatLng start = new LatLng(currentLocationmMarker.getPosition().latitude, currentLocationmMarker.getPosition().longitude);
        LatLng end = new LatLng(end_lat, end_lng);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(end);
        LatLngBounds bounds = builder.build();

        int padding = 10;
        CameraUpdate camUp = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.moveCamera(camUp);
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.title("current location");
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.title(lastDestinationMarker.getTitle());
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {

    }





}
