package com.digitechsolz.totorickshaw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    String[] country = { "1", "2", "3", "4"};

    AutocompleteSupportFragment source_address, destination_address;
    Spinner no_passenger;
    String nop, slat, slong, dlat, dlong, saddress, scity, sstate, scountry, spostalCode, daddress,
            dcity, dstate, dcountry, dpostalCode, apiKey;

    TextView disance, no_toto, fare, time_required;
    Button reserve_ride_btn, share_ride_btn, booking_btn;
    //Coordinatelayout for Snackbar
    ConstraintLayout constraintLayout;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    Marker mCurrLocationMarker1, mCurrLocationMarker2;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private final static String KEY_LOCATION = "location";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    LatLng slocation, dlocation;

    private GpsTracker gpsTracker;
    String dist, dist_value, dur;

    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        setContentView(R.layout.activity_main);

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);

        disance = (TextView) findViewById(R.id.disance);
        no_toto = (TextView) findViewById(R.id.no_toto);
        fare = (TextView) findViewById(R.id.fare);
        time_required = (TextView) findViewById(R.id.time_required);

        reserve_ride_btn = (Button) findViewById(R.id.reserve_ride_btn);
        share_ride_btn = (Button) findViewById(R.id.share_ride_btn);
        booking_btn = (Button) findViewById(R.id.booking_btn);

        apiKey = getString(R.string.google_maps_api_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        source_address = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.source_address);
        if (getIntent() != null && getIntent().getStringExtra("latitude") != null &&
                getIntent().getStringExtra("longitude") != null) {
            slat = getIntent().getStringExtra("latitude");
            slong = getIntent().getStringExtra("longitude");
            Toast.makeText(getApplicationContext(), "From location lat : " + slat + "& long : " + slong, Toast.LENGTH_LONG).show();
            getsAddress(slat, slong);
//            setSourceMarker(slat, slong);
        } else {
            gpsTracker = new GpsTracker(MainActivity.this);
            if(gpsTracker.canGetLocation()) {
                slat = Double.toString(gpsTracker.getLatitude());
                slong = Double.toString(gpsTracker.getLongitude());
                //getsAddress(slat, slong);
            } else {
                gpsTracker.showSettingsAlert();
            }
        }

        no_passenger = (Spinner) findViewById(R.id.no_passenger);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,country);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        no_passenger.setAdapter(aa);
        no_passenger.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nop = country[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        // Specify the types of place data to return.
        source_address.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        source_address.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Source fragment", "Place: " + place.getName() + ", " + place.getId());
                slocation = place.getLatLng();
                slat = Double.toString(slocation.latitude);
                slong = Double.toString(slocation.longitude);

                setSourceMarker(slat, slong);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Source error", "An error occurred: " + status);
            }
        });

        destination_address = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination_address);
        // Specify the types of place data to return.
        destination_address.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        // Set up a PlaceSelectionListener to handle the response.
        destination_address.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Destination fragment", "Place: " + place.getName() + ", " + place.getId());
                dlocation = place.getLatLng();
                dlat = Double.toString(dlocation.latitude);
                dlong = Double.toString(dlocation.longitude);

                setDestinationMarker(dlat, dlong);
                String url = getDirectionsUrl(slocation, dlocation);
                DownloadTask downloadTask = new DownloadTask();
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Destination error", "An error occurred: " + status);
            }
        });

        reserve_ride_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Latitude :", slat);
                if (slat == null && slong == null) {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Please select source location!", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                } else if (dlat == null && dlong == null) {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Please select destination location!", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                } else {
                    String fare_value = calculateFare("1", nop);
                    fare.setText("Rs. " + fare_value);
                }
            }
        });

        share_ride_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slat == null && slong == null) {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Please select source location!", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                } else if (dlat == null && dlong == null) {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Please select destination location!", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                } else if (Integer.parseInt(nop) > 2) {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Maximum allowed for 2 passengers!", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                } else {
                    String fare_value = calculateFare("2", nop);
                    fare.setText("Rs. " + fare_value);
                }
            }
        });

        booking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready

            //Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            MainActivityPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            MainActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

            LatLng latLng = new LatLng(Double.parseDouble(slat), Double.parseDouble(slong));
            slocation = latLng;

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mCurrLocationMarker1 = map.addMarker(markerOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.animateCamera(cameraUpdate);

        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Display the connection status
        if (mCurrentLocation != null) {
            if (mCurrLocationMarker1 != null) {
                mCurrLocationMarker1.remove();
            }

            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            slocation = latLng;

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mCurrLocationMarker1 = map.addMarker(markerOptions);

            slat = Double.toString(mCurrentLocation.getLatitude());
            slong = Double.toString(mCurrentLocation.getLongitude());
            getsAddress(slat, slong);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        MainActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }

        // Report to the UI that the location was updated
        mCurrentLocation = location;
        slat = Double.toString(location.getLatitude());
        slong = Double.toString(location.getLongitude());
        getsAddress(slat, slong);
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private void getsAddress(String s_lat, String s_long) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(Double.valueOf(s_lat), Double.valueOf(s_long), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            saddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            scity = addresses.get(0).getLocality();
            sstate = addresses.get(0).getAdminArea();
            scountry = addresses.get(0).getCountryName();
            spostalCode = addresses.get(0).getPostalCode();

//        sknownName = addresses.get(0).getFeatureName();
            source_address.setText(saddress + " " + scity + " " + sstate + " " + scountry + " " + spostalCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSourceMarker(String slat, String slong) {
        if (mCurrLocationMarker1 != null) {
            mCurrLocationMarker1.remove();
        }
        LatLng latLng = new LatLng(Double.parseDouble(slat), Double.parseDouble(slong));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker1 = map.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);
    }

    private void setDestinationMarker(String dlat, String dlong) {
        if (mCurrLocationMarker2 != null) {
            mCurrLocationMarker2.remove();
        }
        LatLng latLng = new LatLng(Double.parseDouble(dlat), Double.parseDouble(dlong));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker2 = map.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("json data", String.valueOf(jObject));
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            //polyline.remove();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                points.clear();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);
                Log.e("path : ", String.valueOf(path));

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);
                    double lat = Double.parseDouble(String.valueOf(point.get("lat")));
                    double lng = Double.parseDouble(String.valueOf(point.get("lng")));
                    dist = String.valueOf(point.get("dist"));
                    dist_value = String.valueOf(point.get("dist_val"));
                    dur = String.valueOf(point.get("dur"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
            disance.setText(dist);
            time_required.setText(dur);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Key Enable
        String str_key = "key=" + getString(R.string.google_maps_api_key);
        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + str_key + "&" + sensor + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String calculateFare(String cal_for, String np) {
        String tot_fare = "0";
        double disv = Double.parseDouble(dist_value) / 1000;
        int dv = (int) Math.ceil(Double.parseDouble(dist_value) / 1000);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        double fareV = 0.0;

        if (cal_for.equalsIgnoreCase("1")) {
            //Reserve
            if (timeOfDay >= 5 && timeOfDay < 22) {
                if (dv <= 2) {
                    fareV = Integer.parseInt(np) * 20.0;
                } else {
                    if (Integer.parseInt(np) <= 2) {
                        fareV = Integer.parseInt(np) * 20.0 + (disv - 2) * 6.0;
                    } else if (Integer.parseInt(np) > 2 && Integer.parseInt(np) <= 4) {
                        fareV = Integer.parseInt(np) * 20.0 + (disv - 2) * 7.0;
                    }
                }
            } else {
                if (dv <= 2) {
                    fareV = Integer.parseInt(np) * 30.0;
                } else {
                    if (Integer.parseInt(np) <= 2) {
                        fareV = Integer.parseInt(np) * 30.0 + (disv - 2) * 7.0;
                    } else if (Integer.parseInt(np) > 2 && Integer.parseInt(np) <= 4) {
                        fareV = Integer.parseInt(np) * 30.0 + (disv - 2) * 8.0;
                    }
                }
            }
        } else if (cal_for.equalsIgnoreCase("2")) {
            //Share
            if (timeOfDay >= 5 && timeOfDay < 22) {
                if (dv <= 2) {
                    fareV = Integer.parseInt(np) * 15.0;
                } else {
                    if (Integer.parseInt(np) == 1) {
                        fareV = Integer.parseInt(np) * 15.0 + (disv - 2) * 5.0;
                    } else if (Integer.parseInt(np) == 2) {
                        fareV = Integer.parseInt(np) * 15.0 + (disv - 2) * 6.0;
                    }
                }
            } else {
                if (dv <= 2) {
                    fareV = Integer.parseInt(np) * 25.0;
                } else {
                    if (Integer.parseInt(np) == 1) {
                        fareV = Integer.parseInt(np) * 25.0 + (disv - 2) * 7.0;
                    } else if (Integer.parseInt(np) == 2) {
                        fareV = Integer.parseInt(np) * 25.0 + (disv - 2) * 8.0;
                    }
                }
            }
        }
        tot_fare = String.valueOf((int) Math.round(fareV));

        return tot_fare;
    }
}