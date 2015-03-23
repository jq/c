package uber.calendar.com.ubercalendar;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AbstractMapActivity implements
        OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMyLocationChangeListener  {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            MapFragment mapFrag=
                    (MapFragment)getFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(final GoogleMap map) {
        if (mMap == null) {
            CameraUpdate center=
                    CameraUpdateFactory.newLatLng(new LatLng(40.76793169992044,
                            -73.98180484771729));
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

            map.moveCamera(center);
            map.animateCamera(zoom);
        }

        //addMarker(map, 40.748963847316034, -73.96807193756104,
        //        R.string.un, R.string.united_nations);
        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);

        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(this);
        mMap = map;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMyLocationChange(Location lastKnownLocation) {
        Log.d(getClass().getSimpleName(),
                String.format("%f:%f", lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude()));
    }

    private void addMarker(GoogleMap map, double lat, double lon,
                           int title, int snippet) {
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title(getString(title))
                .snippet(getString(snippet)));
    }

}
