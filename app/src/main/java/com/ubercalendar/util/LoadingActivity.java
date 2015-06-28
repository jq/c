package com.ubercalendar.util;

import android.app.Activity;
import android.widget.ProgressBar;

import com.ubercalendar.R;

import butterknife.InjectView;

/**
 * Created by julian on 6/27/15.
 */
public abstract class LoadingActivity extends Activity {
    @InjectView(R.id.progressbar)
    ProgressBar progressBar;
}
