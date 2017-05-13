package com.parkhappy.hm.phv1application;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;

    public static double lat, lng;
    public static double lastlat=0, lastlng=0;
    public static ArrayList<Lot> myLots = new ArrayList<Lot>();

    Button closestbtn, cheapestbtn;

    public static int selectedLotID;

    ImageButton listbtn, menubtn;

    LinearLayout btnsLayout;
    RelativeLayout srchLayout;

    public static int closecheapF;//0 cheap 1 close

    DrawerLayout drawer;
    int selectedLoti;

    int loadedFlag = 0;//0 no 1 yes

    Location currentLoc;

    int dialogFlag = 0;

    String sflag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().hide();

        closestbtn = (Button) findViewById(R.id.closestbtn);
        cheapestbtn = (Button) findViewById(R.id.cheapestbtn);
        listbtn = (ImageButton) findViewById(R.id.listbtn);
        menubtn = (ImageButton) findViewById(R.id.menubtn);

        btnsLayout = (LinearLayout) findViewById(R.id.btnsLayout);
        srchLayout = (RelativeLayout) findViewById(R.id.srchLayout);

        closestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayclosestBtn();
                getnearest();
                addMarkers();
                closecheapF = 1;
            }
        });
        cheapestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaycheapestBtn();
                getcheapest();
                addMarkers();
                closecheapF = 0;
            }
        });
        listbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loadedFlag == 1)
                    startActivity(new Intent(MainActivity.this, ListActivity.class));
                else
                    Toast.makeText(MainActivity.this, "Please wait will reloading", Toast.LENGTH_SHORT).show();
            }
        });
        init_lotdialog();

        PlaceAutocompleteFragment placesfragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        placesfragment.setHint("Find spots near..");


        placesfragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(com.google.android.gms.location.places.Place place) {
                LatLng ll = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                //go cheapest
                cheapestbtn.setBackgroundColor(Color.WHITE);
                cheapestbtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                closestbtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                closestbtn.setTextColor(Color.WHITE);
                lat = place.getLatLng().latitude;
                lng = place.getLatLng().longitude;
                sflag=place.getName().toString();//for search marker
                getlots();
                displaycheapestBtn();
                getcheapest();
                addMarkers();

                //move
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

            @Override
            public void onError(Status status) { // Handle the error
                Toast.makeText(MainActivity.this, "Something went wrong! please try again ", Toast.LENGTH_SHORT).show();
            }
        });






//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);






        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });


    }

    void displayclosestBtn(){
        closestbtn.setBackgroundColor(Color.WHITE);
        closestbtn.setTextColor(getResources().getColor(R.color.colorPrimary));
        cheapestbtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        cheapestbtn.setTextColor(Color.WHITE);
    }
    void displaycheapestBtn(){
        cheapestbtn.setBackgroundColor(Color.WHITE);
        cheapestbtn.setTextColor(getResources().getColor(R.color.colorPrimary));
        closestbtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        closestbtn.setTextColor(Color.WHITE);
    }


    private void setUpMap() {
        LatLng latLng;
//        Location myLocation = getLastKnownLocation();
        currentLoc=getLastKnownLocation();
            if (currentLoc != null) {
                double latitude = currentLoc.getLatitude();
                double longitude = currentLoc.getLongitude();
                //latLng = new LatLng(43.641895, -79.374236);//;
                latLng = new LatLng(latitude, longitude);

                lat = latLng.latitude;
                lng = latLng.longitude;

                setoldlatlng();//3shn lw gy mn last yrg3 le nfs l pos

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            } else {
                latLng = null;// new LatLng(43.641895, -79.374236);
                lat = 0;
                lng = 0;
                //latLng = new LatLng(43.641895, -79.374236);

                setoldlatlng();//3shn lw gy mn last yrg3 le nfs l pos
            }





//
//        lat=latitude;
////        lng=longitude;
//
//        lat=latLng.latitude;
//        lng=latLng.longitude;
///////////////////////////////////
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return;
//        mMap.setMyLocationEnabled(true);
//
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String provider = locationManager.getBestProvider(criteria, true);
//        Location myLocation = locationManager.getLastKnownLocation(provider);
////        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//
//        lat = myLocation.getLatitude();
//        lng = myLocation.getLongitude();
//        LatLng latLng = new LatLng(lat, lng);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//////////////////////////////////////////////////////////////////////
//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String provider = locationManager.getBestProvider(criteria, true);
//        currentLoc = locationManager.getLastKnownLocation(provider);
//        int x=0;
    }

    void setoldlatlng(){
        if(lastlat!=0) {
            lat = lastlat;
            lng = lastlng;
        }
    }


    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return null;
        LocationManager mLocationManager;
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }


    void getlots(){
        loadedFlag=1;
//        myLots.clear();
        StringRequest sr = new StringRequest(Request.Method.GET, "http://159.203.61.141:8080/parkhappy/api/getLotsList", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    myLots.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        int id = obj.getInt("id");
                        int number = obj.getInt("lotNumber");
                        String name = obj.getString("lotName");
                        double latitude = obj.getDouble("latitude");
                        double longitude = obj.getDouble("longitude");

                        JSONArray prices = obj.getJSONArray("lotPriceBreakDownList");
                        for (int j = 0; j < prices.length(); j++) {
                            JSONObject price = prices.getJSONObject(j);
                            double amount = price.getDouble("amount");
                            String amountText = price.getString("amountText");
                            double duration = price.getDouble("duration");
                            JSONObject lotPriceDurationGroup = price.getJSONObject("lotPriceDurationGroup");
                            String label = lotPriceDurationGroup.getString("label");
                        }

                        JSONArray lotFeatures = obj.getJSONArray("lotFeatures");
                        for (int j = 0; j < lotFeatures.length(); j++) {
                            String label = lotFeatures.getJSONObject(j).getString("label");
                        }

                        JSONObject addr = obj.getJSONObject("lotAddress");
                        String address1 = addr.getString("address1");
                        String address2 = addr.getString("address2");
                        String city = addr.getString("city");
                        String country = addr.getString("country");

                        JSONObject lotPriceBasic = obj.getJSONObject("lotPriceBasic");
                        double basicprice = lotPriceBasic.optDouble("priceTotalAmount");

                        Lot myLot =new Lot(id, name, address1, city, country, latitude, longitude, basicprice);
                        if(basicprice>=0)
                            myLots.add(myLot);
                    }

                    displaycheapestBtn();
                    getcheapest();//as default
                    addMarkers();

                    if(arr.length()==0){
                        Toast.makeText(MainActivity.this, "No Parking Lots Found!", Toast.LENGTH_LONG).show();
                    }


                    } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MyApplication.getAppContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(isInternetConnected(MainActivity.this)&&isGPSEnabled(MainActivity.this))//3shn ys2l lw  fy prob fl internet bs hwa mtwsl
                    Toast.makeText(MyApplication.getAppContext(), "There is a trouble with internet connection! ,reloading", Toast.LENGTH_SHORT).show();

                error.printStackTrace();
                getlots();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
//                params.put("Content-Type", "text/html");
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lng));
//                params.put("latitude", "43.641895");
//                params.put("longitude", "-79.374236");
                return params;
            }
        };

        //to retry
        sr.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the queue
        Volley.newRequestQueue(MyApplication.getAppContext()).add(sr);
    }

    public static int minID;
    public static int minI;
    public static void getnearest(){
        double minDistance=-1;
        minID=-1;
        int f = 0;
        for (int i=0; i<myLots.size(); i++){
            if(f==0){
                minDistance = myLots.get(0).distance;
                minID = myLots.get(0).id;
                minI=0;
            }
            if(myLots.get(i).distance<minDistance){
                minDistance = myLots.get(i).distance;
                minID = myLots.get(i).id;
                minI=i;
            }
            f=1;
        }
    }
    public static void getcheapest(){
        double minPrice=-1;
        minID=-1;
        int f = 0;
        for (int i=0; i<myLots.size(); i++){
            if(f==0){
                minPrice = myLots.get(0).priceBasic;
                minID = myLots.get(0).id;
                minI=0;
            }
            if(myLots.get(i).priceBasic<minPrice){
                minPrice = myLots.get(i).priceBasic;
                minID = myLots.get(i).id;
                minI=i;
            }
            f=1;
        }
    }



    void addMarkers(){
        mMap.clear();
        for (int i=0; i<myLots.size(); i++){
            if(myLots.get(i).id!=minID){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(myLots.get(i).lat, myLots.get(i).lng))
                        .snippet(String.valueOf(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView("$"+String.valueOf(Math.round(myLots.get(i).priceBasic))+" "))));

            }
            else
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(myLots.get(i).lat, myLots.get(i).lng))
                        .snippet(String.valueOf(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView2("$"+String.valueOf(Math.round(myLots.get(i).priceBasic))+" "))));
            }

        }
        if(sflag != null){
            //location marker
            mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .snippet(sflag));
            //sflag=null;
        }

    }

    private Bitmap getMarkerBitmapFromView(String price) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        TextView markpricetxt = (TextView) customMarkerView.findViewById(R.id.markpricetxt);
        markpricetxt.setText(price);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
    private Bitmap getMarkerBitmapFromView2(String price) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker2, null);
        TextView markpricetxt = (TextView) customMarkerView.findViewById(R.id.markpricetxt);
        markpricetxt.setText(price);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    Dialog lotdialog;
    TextView pricetxt, pricetxt2, lotnametxt, lotaddrtxt, distancetxt;
    Button detbtn;
    ImageButton navbtn;

    void init_lotdialog() {
        lotdialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        lotdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lotdialog.setCancelable(false);
        lotdialog.setContentView(R.layout.lotlayout);
        pricetxt2 = (TextView) lotdialog.findViewById(R.id.pricetxt2);
        pricetxt = (TextView) lotdialog.findViewById(R.id.pricetxt);
        lotnametxt = (TextView) lotdialog.findViewById(R.id.lotnametxt);
        lotaddrtxt = (TextView) lotdialog.findViewById(R.id.lotaddrtxt);
        distancetxt = (TextView) lotdialog.findViewById(R.id.distancetxt);
        detbtn = (Button) lotdialog.findViewById(R.id.detbtn);
        navbtn = (ImageButton) lotdialog.findViewById(R.id.navbtn);

        LinearLayout lay2 = (LinearLayout) lotdialog.findViewById(R.id.lay2);
        RelativeLayout lay1 = (RelativeLayout) lotdialog.findViewById(R.id.lay1);

        lay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarkers();
                lotdialog.hide();
                //show them
                btnsLayout.setVisibility(View.VISIBLE);
                srchLayout.setVisibility(View.VISIBLE);
                dialogFlag=0;
            }
        });
        lay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarkers();
                lotdialog.hide();
                //show them
                btnsLayout.setVisibility(View.VISIBLE);
                srchLayout.setVisibility(View.VISIBLE);
                dialogFlag=0;
            }
        });
        detbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastlat = lat;
                lastlng = lng;
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
            }
        });
        navbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Location myLocation = getLastKnownLocation();
                Lot s = myLots.get(selectedLoti);


                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr="+myLocation.getLatitude()+","+myLocation.getLongitude()+"&daddr="+s.lat+","+s.lng));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_LAUNCHER );
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
    }
    void fill_lotdialog(int index) {
//        mMap.clear();
        Lot l = myLots.get(index);
        pricetxt.setText(String.valueOf("$"+Math.round(l.priceBasic)));
        pricetxt2.setText(String.valueOf("$"+Math.round(l.priceBasic)));
        lotnametxt.setText(l.name);
        lotaddrtxt.setText(String.valueOf(l.address1+", "+l.city+", "+l.country));
        distancetxt.setText(String.valueOf(l.distance+"Km"));
        selectedLotID=l.id;
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contactus) {
            startActivity(new Intent(MainActivity.this, NavHelpActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(MainActivity.this, NavAboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //before ay 7aga check 3shn l fn d bttcale 2bl l ba2y
        /////////////////////////////////////////////////////////////////////////////////
        //check internet connection
        if (!isInternetConnected(this)) {
            Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
//            finish();
            askWifiData();
        }
        //check gps
        if (!isGPSEnabled(this)) {
            Toast.makeText(MainActivity.this, "Please enable GPS", Toast.LENGTH_LONG).show();
//            finish();
//            GoogleApiClient mGoogleApiClient = new GoogleApiClient
//                    .Builder(this)
//                    .enableAutoManage(this, 34992, this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(MainActivity.this)
//                    .addOnConnectionFailedListener(this)
//                    .build();
//
//            locationChecker(mGoogleApiClient, MainActivity.this);
            askGPS();
        }
        /////////////////////////////////////////////////////////////////////////////////





        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
        setUpMap();
        getlots();


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                dialogFlag=1;//3shn ymn3 l markers tany
                selectedLoti = Integer.parseInt(marker.getSnippet());
                fill_lotdialog(selectedLoti);
                marker.hideInfoWindow();
//                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myLots.get(selectedLoti).lat,myLots.get(selectedLoti).lng)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLots.get(selectedLoti).lat,myLots.get(selectedLoti).lng)));
                lotdialog.show();

                //hide them
                btnsLayout.setVisibility(View.INVISIBLE);
                srchLayout.setVisibility(View.INVISIBLE);

                marker.remove();//mMap.clear();////we hnrg3ha m3 l hide

                //return false;
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });


        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(dialogFlag==0) {
                    Log.d("Locationnnn", "lat: " + mMap.getCameraPosition().target.latitude + " Lng: " + mMap.getCameraPosition().target.longitude);
                    lat = mMap.getCameraPosition().target.latitude;
                    lng = mMap.getCameraPosition().target.longitude;
                    getlots();
                }

            }
        });
    }


    public static boolean isInternetConnected(Context ctx) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        if (wifi != null) {
            if (wifi.isConnected()) {
                return true;
            }
        }
        if (mobile != null) {
            if (mobile.isConnected()) {
                return true;
            }
        }
        return false;
    }
    public boolean isGPSEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    public static void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    void askGPS(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Please Enable GPS to use te app";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                               startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                                finish();
                            }
                        });
        builder.create().show();
    }
    void askWifiData(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String action = Settings.ACTION_WIRELESS_SETTINGS;
        final String message = "Please enable internet connection to use te app";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                                finish();
                            }
                        });
        builder.create().show();
    }

    void showAlert(String msg){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.dismiss();
                            }
                        });
        builder.create().show();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

//    @Override
//    public void onResume(){ //fired at first and after comming back
//        super.onResume();
//        if(lastlat!=0){
//            lat = lastlat;
//            lng = lastlng;
//        }
//    }
}
