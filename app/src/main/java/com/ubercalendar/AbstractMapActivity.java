/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package com.ubercalendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ubercalendar.util.BaseMapFragment;
import com.ubercalendar.util.TouchableWrapper;

public abstract class AbstractMapActivity extends FragmentActivity implements TouchableWrapper.UpdateMapAfterUserInteraction{
  protected static final String TAG_ERROR_DIALOG_FRAGMENT="errorDialog";

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);

    return(super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.setting) {
      return(true);
    }

    return super.onOptionsItemSelected(item);
  }

  protected boolean readyToGo() {
    int status=
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

    if (status == ConnectionResult.SUCCESS) {
      if (getVersionFromPackageManager(this) >= 2) {
        return(true);
      }
      else {
        Toast.makeText(this, R.string.no_maps, Toast.LENGTH_LONG).show();
        finish();
      }
    }
    else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
      ErrorDialogFragment.newInstance(status)
                         .show(getFragmentManager(),
                               TAG_ERROR_DIALOG_FRAGMENT);
    }
    else {
      Toast.makeText(this, R.string.no_maps, Toast.LENGTH_LONG).show();
      finish();
    }

    return(false);
  }

  public static class ErrorDialogFragment extends DialogFragment {
    static final String ARG_STATUS="status";

    static ErrorDialogFragment newInstance(int status) {
      Bundle args=new Bundle();

      args.putInt(ARG_STATUS, status);

      ErrorDialogFragment result=new ErrorDialogFragment();

      result.setArguments(args);

      return(result);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Bundle args=getArguments();

      return GooglePlayServicesUtil.getErrorDialog(args.getInt(ARG_STATUS),
              getActivity(), 0);
    }

    @Override
    public void onDismiss(DialogInterface dlg) {
      if (getActivity() != null) {
        getActivity().finish();
      }
    }
  }

  // following from
  // https://android.googlesource.com/platform/cts/+/master/tests/tests/graphics/src/android/opengl/cts/OpenGlEsVersionTest.java
  private static int getVersionFromPackageManager(Context context) {
    PackageManager packageManager=context.getPackageManager();
    FeatureInfo[] featureInfos=
        packageManager.getSystemAvailableFeatures();
    if (featureInfos != null && featureInfos.length > 0) {
      for (FeatureInfo featureInfo : featureInfos) {
        // Null feature name means this feature is the open
        // gl es version feature.
        if (featureInfo.name == null) {
          if (featureInfo.reqGlEsVersion != FeatureInfo.GL_ES_VERSION_UNDEFINED) {
            return getMajorVersion(featureInfo.reqGlEsVersion);
          }
          else {
            return 1; // Lack of property means OpenGL ES
                      // version 1
          }
        }
      }
    }
    return 1;
  }
    public static void setIME(final View v, final Activity activity) {
        v.requestFocus();
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
            }
        }, 100);
    }

    public static void hideIME(final View v, final Activity activity) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

  /** @see android.content.pm.FeatureInfo#getGlEsVersion() */
  private static int getMajorVersion(int glEsVersion) {
    return((glEsVersion & 0xffff0000) >> 16);
  }
}
