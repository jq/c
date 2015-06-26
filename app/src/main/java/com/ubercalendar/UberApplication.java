package com.ubercalendar;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by julian on 6/26/15.
 */
public class UberApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);
    }
    public static RefWatcher getRefWatcher(Context context) {
        UberApplication application = (UberApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

}
