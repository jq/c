package com.ubercalendar;

import android.content.Context;

import com.ubercalendar.model.PriceEstimate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Created by amoi on 6/27/15.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class FareEstimatesAdapterTest {

  private FareEstimatesAdapter adatper;

  @Before
  public void setUp() {
    Context context = RuntimeEnvironment.application.getApplicationContext();
    adatper = new FareEstimatesAdapter(context);
  }

  @Test
  public void simple() {
    adatper.add(new PriceEstimate());
    adatper.add(new PriceEstimate());
    adatper.add(new PriceEstimate());
    assertThat(adatper.getCount(), is(3));
  }
}
