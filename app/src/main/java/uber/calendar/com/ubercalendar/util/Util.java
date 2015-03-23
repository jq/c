package uber.calendar.com.ubercalendar.util;

import android.location.Location;

/**
 * Created by julian on 3/23/15.
 */
public class Util {
    public static boolean locationChanged(Location last, Location now) {
        if (Math.abs(last.getLatitude() - now.getLatitude()) > 0.00008) return true;
        if (Math.abs(last.getLongitude() - now.getLongitude()) > 0.0008) return true;
        return false;
    }
}
