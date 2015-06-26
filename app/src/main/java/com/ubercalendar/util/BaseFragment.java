package com.ubercalendar.util;

import android.app.Fragment;

import com.squareup.leakcanary.RefWatcher;
import com.ubercalendar.UberApplication;

/**
 * Created by julian on 6/26/15.
 */
public abstract class BaseFragment extends Fragment {
    @Override public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = UberApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
