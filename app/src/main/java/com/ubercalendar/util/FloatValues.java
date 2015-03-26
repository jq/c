package com.ubercalendar.util;

import android.content.SharedPreferences;
import android.location.Location;

/**
 * Created by julian on 3/26/15.
 */
public enum FloatValues {
    LAST_LAT("latlast"),
    LAST_LNG("lnglast");
    private final String key;
    private float value;
    private static SharedPreferences pref;
    FloatValues(String key) {
        this.key = key;
    }
    public void set(float value) {
        this.value = value;
        pref.edit().putFloat(key, value).apply();
    }
    public void load() {
        value = pref.getFloat(key, 0);
    }
    public float get() {
        return value;
    }

    public static void init(SharedPreferences pref) {
        FloatValues.pref = pref;
        LAST_LAT.load();
        LAST_LNG.load();
    }
    public static boolean locationChanged(Location last, Location now) {
        if (Math.abs(last.getLatitude() - now.getLatitude()) > 0.0002) return true;
        if (Math.abs(last.getLongitude() - now.getLongitude()) > 0.002) return true;
        return false;
    }
    public static boolean updateLocation(Location location) {
        if ((Math.abs(location.getLatitude() - LAST_LAT.get()) < 0.0003) &&
            (Math.abs(location.getLongitude() - LAST_LNG.get()) < 0.0003)) return false;
        LAST_LAT.set((float)location.getLatitude());
        LAST_LNG.set((float)location.getLongitude());
        return true;
    }
}
