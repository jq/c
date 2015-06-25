package com.ubercalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import com.ubercalendar.api.UberAPIClient;
import com.ubercalendar.api.UberCallback;
import com.ubercalendar.model.PriceEstimateList;

import retrofit.client.Response;

/**
 * Created by amoi on 6/23/15.
 */
public class FareEstimateActivity extends ActionBarActivity {

  public static void start(Context context, String accessToken, String tokenType,
      double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
    Intent intent = new Intent(context, FareEstimateActivity.class);
    intent.putExtra(Constants.ACCESS_TOKEN, accessToken);
    intent.putExtra(Constants.TOKEN_TYPE, tokenType);
    intent.putExtra(Constants.START_LAT, startLatitude);
    intent.putExtra(Constants.START_LON, startLongitude);
    intent.putExtra(Constants.END_LAT, endLatitude);
    intent.putExtra(Constants.END_LON, endLongitude);
    context.startActivity(intent);
  }

  private FareEstimatesAdatper estimatesAdatper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fare_estimate);

    ListView estimatesView = (ListView) findViewById(R.id.fare_estimates);
    estimatesAdatper = new FareEstimatesAdatper(this);
    estimatesView.setAdapter(estimatesAdatper);

    final Intent intent = getIntent();
    UberAPIClient.getUberV1APIClient().getPriceEstimates(
        getAccessToken(intent),
        intent.getDoubleExtra(Constants.START_LAT, 0),
        intent.getDoubleExtra(Constants.START_LON, 0),
        intent.getDoubleExtra(Constants.END_LAT, 0),
        intent.getDoubleExtra(Constants.END_LON, 0),
        new UberCallback<PriceEstimateList>() {
          @Override
          public void success(PriceEstimateList priceEstimateList, Response response) {
            estimatesAdatper.addAll(priceEstimateList.getPrices());
          }
        });
  }
  private String getAccessToken(Intent intent) {
    return intent.getStringExtra(Constants.TOKEN_TYPE) + " "
        + intent.getStringExtra(Constants.ACCESS_TOKEN);
  }
}
