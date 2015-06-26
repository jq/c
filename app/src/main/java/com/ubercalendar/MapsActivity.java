package com.ubercalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import com.ubercalendar.util.FloatValues;
import com.ubercalendar.util.Ln;
import com.ubercalendar.util.Util;

import hugo.weaving.DebugLog;

public class MapsActivity extends AbstractMapActivity implements
    OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnMyLocationChangeListener,GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks   {
  public static final String TAG = "MapsActivity";
  // TODO replace with SF bounds
  private static final LatLngBounds SF = new LatLngBounds(
      new LatLng(37.768099, -122.418755), new LatLng(37.78416, -122.398906));

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  protected GoogleApiClient mGoogleApiClient;

  private PlaceAutocompleteAdapter startLocationAdatper;
  private PlaceAutocompleteAdapter endLocationAdatper;
  private AutoCompleteTextView startLocationTextView;
  private AutoCompleteTextView endLocationTextView;
  private PlaceLikelihoodBuffer currentLikelyPlaceBuffer;
  private LatLng startLatLng = null;
  private LatLng endLatLng = null;
  private Button clearStartLocationButton;

  private Location lastLocation;

  public static void start(Context context, String accessToken, String tokenType) {
    Intent intent = new Intent(context, MapsActivity.class);
    intent.putExtra(Constants.ACCESS_TOKEN, accessToken);
    intent.putExtra(Constants.TOKEN_TYPE, tokenType);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
    FloatValues.init(pref);
    if (mGoogleApiClient == null) {
        rebuildGoogleApiClient();
    }
    setContentView(R.layout.activity_maps);

    startLocationTextView = (AutoCompleteTextView) findViewById(R.id.start_location);
    startLocationAdatper = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
            SF, null);
    startLocationTextView.setAdapter(startLocationAdatper);
    startLocationTextView.setOnItemClickListener(startLocationSelectedListener);

    clearStartLocationButton = (Button) findViewById(R.id.clear_start_location);
    clearStartLocationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startLocationTextView.setText("");
        startLocationTextView.setHint(R.string.current_location);
        startLatLng = null;
      }
    });

    endLocationTextView = (AutoCompleteTextView) findViewById(R.id.end_location);
    endLocationAdatper = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
            SF, null);
    endLocationTextView.setAdapter(endLocationAdatper);
    // Register a listener that receives callbacks when a suggestion has been selected
    endLocationTextView.setOnItemClickListener(endLocationSelectedListener);

    Button clearEndLocation = (Button) findViewById(R.id.clear_end_location);
    clearEndLocation.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        endLocationTextView.setText("");
      }
    });
    endLocationTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_GO) {
          if (endLatLng == null) {
            Toast.makeText(MapsActivity.this, "Must set destination", Toast.LENGTH_LONG).show();
            return false;
          }

          if (startLatLng != null) {
            estimateFare(startLatLng, endLatLng);
          } else {
            estimateFareFromCurrentPlace(endLatLng);
          }
        }
        return false;
      }

    });
    setUpMapIfNeeded();
    loadCurrentPlace(null);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setUpMapIfNeeded();
    startLatLng = null;
    clearStartLocationButton.setVisibility(View.GONE);
    loadCurrentPlace(null);
    startLocationTextView.setText("");
    startLocationTextView.setHint(R.string.current_location);

    endLocationTextView.selectAll();
    setIME(endLocationTextView, MapsActivity.this);
  }

  @Override
  protected void onPause() {
    hideIME(startLocationTextView, this);
    hideIME(endLocationTextView, this);
    super.onPause();
  }
  /**
   -   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   -   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   -   * call setUpMap()} once when {@link #mMap} is not null.
   -   * <p/>
   -   * If it isn't installed {@link SupportMapFragment} (and
   -   * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
   -   * install/update the Google Play services APK on their device.
   -   * <p/>
   -   * A user can return to this FragmentActivity after following the prompt and correctly
   -   * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
   -   * have been completely destroyed during this process (it is likely that it would only be
   -   * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
   -   * method in {@link #onResume()} to guarantee that it will be called.
   -   */
    private void setUpMapIfNeeded() {
      // Do a null check to confirm that we have not already instantiated the map.
      if (mMap == null) {
        // Try to obtain the map from the SupportMapFragment.
        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
          mMap = mapFrag.getMap();
          if(mMap != null) {
              Ln.e("has map in resume");
              setupMap(mMap);
          } else {
              mapFrag.getMapAsync(this);
          }
      }
    }
  private void loadCurrentPlace(final ResultCallback<PlaceLikelihoodBuffer> callback) {
    PendingResult<PlaceLikelihoodBuffer> currentPlaceResult =
        Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
    currentPlaceResult.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
      @Override
      public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
        if (placeLikelihoods.getCount() == 0) {
          Log.e(TAG, "Cannot find current place");
          return;
        }
        final Place currentPlace = placeLikelihoods.get(0).getPlace();
        final LatLng currentLatLng = currentPlace.getLatLng();
        for (int i = 0; i < placeLikelihoods.getCount(); ++i) {
          Log.i(TAG,
              "Likely place " + i + " is " + placeLikelihoods.get(i).getPlace().getAddress());
        }
        currentLikelyPlaceBuffer = placeLikelihoods;
        if (callback != null) {
          callback.onResult(currentLikelyPlaceBuffer);
        }
      }
    });
  }

    private void setupMap(final GoogleMap map) {
        if (FloatValues.LAST_LAT.get() != 0) {
            move(FloatValues.LAST_LAT.get(), FloatValues.LAST_LNG.get());
        }
        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);
        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(this);
    }
    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        setupMap(map);
        move(map.getMyLocation());
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMyLocationChange(Location lastKnownLocation) {
        if (mMap == null) return;
        move(lastKnownLocation);
/*        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;
        mAdapter.setBounds(curScreen); R.drawable.ub__pin_pickup*/
    }
    protected void move(double lat, double lng) {
        LatLng latlng=
                new LatLng(lat, lng);
        move(latlng);
    }
    protected void move(LatLng latLng) {
        CameraUpdate cu=CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.moveCamera(cu);
        //mMap.animateCamera(cu);
    }
    protected void move(Location location) {
        if (location == null) return;
        if (FloatValues.updateLocation(location)) {
            LatLng latlng =
                    new LatLng(location.getLatitude(), location.getLongitude());
            move(latlng);
        }
    }
    public void onUpdateMapAfterUserInteraction() {
        LatLng latLng = mMap.getProjection()
                .getVisibleRegion().latLngBounds.getCenter();
        Log.d(getClass().getSimpleName(),
                String.format("%f:%f", latLng.latitude, latLng.longitude));
    }
    private void addMarker(GoogleMap map, double lat, double lon, int iconRes) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(iconRes);
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .draggable(true).icon(icon).flat(true));
    }
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }


    @Override
    protected void onPostExecute(List<Address> addresses) {

      if(addresses==null || addresses.size()==0){
        Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
      }

      // Clears all the existing markers on the map
      mMap.clear();

      // Adding Markers on Google Map for each matching address
      for(int i=0;i<addresses.size();i++){

        Address address = (Address) addresses.get(i);

                /* Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                mMap.addMarker(markerOptions);

                // Locate the first location
                if(i==0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));*/
      }
    }
  }

  private AdapterView.OnItemClickListener startLocationSelectedListener
      = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      clearStartLocationButton.setVisibility(View.VISIBLE);
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
      final PlaceAutocompleteAdapter.PlaceAutocomplete item =
          startLocationAdatper.getItem(position);
      final String placeId = String.valueOf(item.placeId);
      Log.i(TAG, "Start Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
      PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
          .getPlaceById(mGoogleApiClient, placeId);
      placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
          if (!places.getStatus().isSuccess()) {
            // Request did not complete successfully
            Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());

            return;
          }
          // Get the Place object from the buffer.
          final Place place = places.get(0);
          startLatLng = place.getLatLng();
        }
      });
    }
  };

  private AdapterView.OnItemClickListener endLocationSelectedListener
      = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
      final PlaceAutocompleteAdapter.PlaceAutocomplete item = endLocationAdatper.getItem(position);
      final String placeId = String.valueOf(item.placeId);
      Log.i(TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
      PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
          .getPlaceById(mGoogleApiClient, placeId);
      placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
      endLocationTextView.clearListSelection();
      hideIME(endLocationTextView, MapsActivity.this);
      Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
    }
  };

  /**
   * Callback for results from a Places Geo Data API query that shows the first place result in
   * the details view on screen.
   */
  private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
      = new ResultCallback<PlaceBuffer>() {
    @Override
    public void onResult(PlaceBuffer places) {
      if (!places.getStatus().isSuccess()) {
        // Request did not complete successfully
        Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());

        return;
      }
      // Get the Place object from the buffer.
      final Place place = places.get(0);
      endLatLng = place.getLatLng();
      if (startLatLng != null) {
        estimateFare(startLatLng, endLatLng);
      } else {
        estimateFareFromCurrentPlace(endLatLng);
      }
    }
  };

  private void estimateFareFromCurrentPlace(final LatLng destination) {
    if (currentLikelyPlaceBuffer != null && currentLikelyPlaceBuffer.getCount() > 0) {
      Place currentPlace = currentLikelyPlaceBuffer.get(0).getPlace();
      estimateFare(currentPlace.getLatLng(), destination);
    } else {
      loadCurrentPlace(new ResultCallback<PlaceLikelihoodBuffer>() {
        @Override
        public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
          if (currentLikelyPlaceBuffer != null && currentLikelyPlaceBuffer.getCount() > 0) {
            Place currentPlace = currentLikelyPlaceBuffer.get(0).getPlace();
            estimateFare(currentPlace.getLatLng(), destination);
          }
        }
      });
    }
  }

  private void estimateFare(LatLng start, LatLng end) {
    FareEstimateActivity.start(MapsActivity.this,
        getIntent().getStringExtra(Constants.ACCESS_TOKEN),
        getIntent().getStringExtra(Constants.TOKEN_TYPE),
        start.latitude,
        start.longitude,
        end.latitude,
        end.longitude);
  }

  private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
      CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
    // Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
    //         websiteUri));
    return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
        websiteUri));

  }
  /**
   * Construct a GoogleApiClient for the {@link com.google.android.gms.location.places.Places#GEO_DATA_API} using AutoManage
   * functionality.
   * This automatically sets up the API client to handle Activity lifecycle events.
   */
  protected synchronized void rebuildGoogleApiClient() {
    // When we build the GoogleApiClient we specify where connected and connection failed
    // callbacks should be returned, which Google APIs our app uses and which OAuth 2.0
    // scopes our app requests.
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, 0 /* clientId */, this)
        .addConnectionCallbacks(this)
        .addApi(Places.GEO_DATA_API)
        .addApi(Places.PLACE_DETECTION_API)
        .build();
  }

  /**
   * Called when the Activity could not connect to Google Play services and the auto manager
   * could resolve the error automatically.
   * In this case the API is not available and notify the user.
   *
   * @param connectionResult can be inspected to determine the cause of the failure
   */
  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {

    Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
        + connectionResult.getErrorCode());

    // TODO(Developer): Check error code and notify the user of error state and resolution.
    Toast.makeText(this,
        "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
        Toast.LENGTH_SHORT).show();

    // Disable API access in the adapter because the client was not initialised correctly.
    startLocationAdatper.setGoogleApiClient(null);
    endLocationAdatper.setGoogleApiClient(null);

  }


  @Override
  public void onConnected(Bundle bundle) {
    // Successfully connected to the API client. Pass it to the adapter to enable API access.
    startLocationAdatper.setGoogleApiClient(mGoogleApiClient);
    endLocationAdatper.setGoogleApiClient(mGoogleApiClient);
    Log.i(TAG, "GoogleApiClient connected.");

  }

  @Override
  public void onConnectionSuspended(int i) {
    // Connection to the API client has been suspended. Disable API access in the client.
    startLocationAdatper.setGoogleApiClient(null);
    endLocationAdatper.setGoogleApiClient(null);
    Log.e(TAG, "GoogleApiClient connection suspended.");
  }

}
