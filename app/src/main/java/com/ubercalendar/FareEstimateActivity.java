package com.ubercalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import com.ubercalendar.api.UberAPIClient;
import com.ubercalendar.api.UberCallback;
import com.ubercalendar.model.PriceEstimateList;

import retrofit.client.Response;

/**
 * Created by amoi on 6/23/15.
 */
public class FareEstimateActivity extends FragmentActivity {

  public static void start(Context context, String accessToken, String tokenType,
      double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
    context.startActivity(newIntent(context, accessToken, tokenType, startLatitude,
        startLongitude, endLatitude, endLongitude));
  }

  public static Intent newIntent(Context context, String accessToken, String tokenType,
      double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
    Intent intent = new Intent(context, FareEstimateActivity.class);
    intent.putExtra(Constants.ACCESS_TOKEN, accessToken);
    intent.putExtra(Constants.TOKEN_TYPE, tokenType);
    intent.putExtra(Constants.START_LAT, startLatitude);
    intent.putExtra(Constants.START_LON, startLongitude);
    intent.putExtra(Constants.END_LAT, endLatitude);
    intent.putExtra(Constants.END_LON, endLongitude);
    return intent;
  }

  FareEstimatesAdapter estimatesAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fare_estimate);

    ListView estimatesView = (ListView) findViewById(R.id.fare_estimates);
    estimatesAdapter = new FareEstimatesAdapter(this);
    estimatesView.setAdapter(estimatesAdapter);

    final Intent intent = getIntent();
    UberAPIClient.getUberV1APIClient().getPriceEstimates(
        getAccessToken(intent),
        intent.getDoubleExtra(Constants.START_LAT, 0),
        intent.getDoubleExtra(Constants.START_LON, 0),
        intent.getDoubleExtra(Constants.END_LAT, 0),
        intent.getDoubleExtra(Constants.END_LON, 0),
        new UberCallback<PriceEstimateList>() {
          @Override //TODO handle error case
          public void success(PriceEstimateList priceEstimateList, Response response) {
            estimatesAdapter.addAll(priceEstimateList.getPrices());
          }
        });
  }

  static String getAccessToken(Intent intent) {
    return intent.getStringExtra(Constants.TOKEN_TYPE) + " "
        + intent.getStringExtra(Constants.ACCESS_TOKEN);
  }
}
