package com.ubercalendar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.ubercalendar.api.UberAPIClient;
import com.ubercalendar.model.PriceEstimate;
import com.ubercalendar.model.PriceEstimateList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Header;
import retrofit.client.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by amoi on 6/27/15.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/TestManifest.xml", sdk = Build.VERSION_CODES.LOLLIPOP)
public class FareEstimateActivityTest {

  private static final String TOKEN = "token";
  private static final String TYPE = "type";
  private static final double START_LAT = 0;
  private static final double START_LON = 0;
  private static final double END_LAT = 1;
  private static final double END_LON = 1;
  private static final List<PriceEstimate> ESTIMATES =
      ImmutableList.of(new PriceEstimate(), new PriceEstimate(), new PriceEstimate());
  private static final Response RESPONSE =
      new Response("url", 400, "reason", ImmutableList.<Header>of(), null);

  @Mock UberAPIClient.UberAPIInterface apiInterface;
  ArgumentCaptor<Callback<PriceEstimateList>> priceEsitmateCallback;

  private ActivityController<FareEstimateActivity> controller;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    UberAPIClient.testAPIService = apiInterface;
    priceEsitmateCallback = (ArgumentCaptor<Callback<PriceEstimateList>>)
        ((Object)ArgumentCaptor.forClass(Callback.class));

    Context context = RuntimeEnvironment.application.getApplicationContext();
    controller = Robolectric.buildActivity(FareEstimateActivity.class)
        .withIntent(FareEstimateActivity.newIntent(context, TOKEN, TYPE, START_LAT, START_LON,
            END_LAT, END_LON));
  }

  @Test
  public void simple() {
    FareEstimateActivity activity = controller.create(new Bundle()).get();
    verify(apiInterface).getPriceEstimates(any(String.class), eq(START_LAT),
        eq(START_LON), eq(END_LAT), eq(END_LON), priceEsitmateCallback.capture());
    priceEsitmateCallback.getValue().success(new TestEstimates(), RESPONSE);
    assertThat(activity.estimatesAdapter.getCount(), is(3));
  }

  private static class TestEstimates extends PriceEstimateList {

    @Override
    public List<PriceEstimate> getPrices() {
      return ESTIMATES;
    }
  }
}
