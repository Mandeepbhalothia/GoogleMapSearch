package com.mandeep.testfgservice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.mandeep.testfgservice.adapter.PlacesAutoCompleteAdapter;
import com.mandeep.testfgservice.adapter.SavedPlacesAdapter;
import com.mandeep.testfgservice.db.PlaceDao;
import com.mandeep.testfgservice.db.PlaceDatabase;

import java.lang.ref.WeakReference;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        PlacesAutoCompleteAdapter.ClickListener, SavedPlacesAdapter.setClickListener, LocationListener {

    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;

    private ImageButton fetchDataBtn;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private SavedPlacesAdapter savedPlacesAdapter;
    private RecyclerView recyclerView, savedPlacesRv;
    PlaceDao placeDao;
    private boolean isLocationSetted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(this, getResources().getString(R.string.place_api_key));

        fetchDataBtn = findViewById(R.id.fetchBtn);
        recyclerView = (RecyclerView) findViewById(R.id.places_recycler_view);
        savedPlacesRv = (RecyclerView) findViewById(R.id.saved_places_rv);
        ((EditText) findViewById(R.id.place_search)).addTextChangedListener(filterTextWatcher);

        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this);
        savedPlacesAdapter = new SavedPlacesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedPlacesRv.setLayoutManager(new LinearLayoutManager(this));
        mAutoCompleteAdapter.setClickListener(this);
        savedPlacesAdapter.setSetClickListener(this);
        recyclerView.setAdapter(mAutoCompleteAdapter);
        savedPlacesRv.setAdapter(savedPlacesAdapter);
        mAutoCompleteAdapter.notifyDataSetChanged();
        savedPlacesAdapter.notifyDataSetChanged();

        // should use viewModel
        initDb();

        fetchDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                savedPlacesRv.setVisibility(View.VISIBLE);
                getAllPlaces();
            }
        });

    }

    private void initDb() {
        PlaceDatabase placeDatabase = PlaceDatabase.getInstance(this);
        placeDao = placeDatabase.getPlaceDao();
    }

    private void getAllPlaces() {
        if (placeDao != null) {
            recyclerView.setVisibility(View.GONE);
            savedPlacesRv.setVisibility(View.VISIBLE);
            new GetPlaceAsyncTask(this, placeDao).execute();
        } else {
            Toast.makeText(this, "Can't fetch data", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPlace(com.mandeep.testfgservice.db.Places place) {
        if (placeDao != null) {
            new InsertPlaceAsyncTask(placeDao).execute(place);
        } else {
            Toast.makeText(this, "Can't save data", Toast.LENGTH_SHORT).show();
        }
    }

    private static class InsertPlaceAsyncTask extends AsyncTask<com.mandeep.testfgservice.db.Places, Void, Void> {

        private PlaceDao placeDao;

        InsertPlaceAsyncTask(PlaceDao placeDao) {
            this.placeDao = placeDao;
        }

        @Override
        protected Void doInBackground(com.mandeep.testfgservice.db.Places... places) {
            placeDao.insert(places[0]);
            return null;
        }
    }

    private static class GetPlaceAsyncTask extends AsyncTask<Void, Void, List<com.mandeep.testfgservice.db.Places>> {

        private PlaceDao placeDao;
        private WeakReference<MapsActivity> weakActivity;

        GetPlaceAsyncTask(MapsActivity mapsActivity, PlaceDao placeDao) {
            weakActivity = new WeakReference<>(mapsActivity);
            this.placeDao = placeDao;
        }

        @Override
        protected List<com.mandeep.testfgservice.db.Places> doInBackground(Void... voids) {
            return placeDao.getAllPlaces();
        }

        @Override
        protected void onPostExecute(List<com.mandeep.testfgservice.db.Places> places) {
            super.onPostExecute(places);
            MapsActivity mapsActivity = weakActivity.get();
            if (places != null && mapsActivity != null) {
                mapsActivity.savedPlacesAdapter.setPlacesList(places);
                mapsActivity.savedPlacesAdapter.notifyDataSetChanged();
            }
        }
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals("")) {
                mAutoCompleteAdapter.getFilter().filter(s.toString());
                if (recyclerView.getVisibility() == View.GONE) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                if (recyclerView.getVisibility() == View.VISIBLE) {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    @Override
    public void click(Place place) {
        if (place != null) {
            Toast.makeText(this, place.getAddress() + ", " + place.getLatLng().latitude + place.getLatLng().longitude, Toast.LENGTH_SHORT).show();
            com.mandeep.testfgservice.db.Places places = new com.mandeep.testfgservice.db.Places(
                    place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude
            );
            addPlace(places);
            addMarkerToMap(place.getLatLng());
        }
    }

    @Override
    public void savedItemClicked(com.mandeep.testfgservice.db.Places places) {
        // hide saved recyclerView
        savedPlacesRv.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        addMarkerToMap(new LatLng(places.getLatitude(), places.getLongitude()));

    }

    private void fakePlaceClick() {
        com.mandeep.testfgservice.db.Places places = new com.mandeep.testfgservice.db.Places(
                "test", "test address", 13, 121);
        addPlace(places);
        addMarkerToMap(new LatLng(1, 11));
    }

    private void addMarkerToMap(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in here"));
        mMap.setMinZoomPreference(12f);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is not given", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        fakePlaceClick();

        setCurrentLocation();
    }

    private void setCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            if (provider != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location Permission is not given", Toast.LENGTH_SHORT).show();
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(provider, 400, 1, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && !isLocationSetted) {
            addMarkerToMap(new LatLng(location.getLatitude(), location.getLongitude()));
            isLocationSetted = true;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}