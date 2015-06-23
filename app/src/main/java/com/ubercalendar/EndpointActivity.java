package com.ubercalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ubercalendar.api.UberAPIClient;
import com.ubercalendar.api.UberCallback;
import com.ubercalendar.model.PriceEstimateList;
import com.ubercalendar.model.ProductList;
import com.ubercalendar.model.Profile;
import com.ubercalendar.model.TimeEstimateList;
import com.ubercalendar.model.UserActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;


public class EndpointActivity extends ActionBarActivity {

  private static final String POSITION = "position";

  public static void start(Context context, int position, String accessToken, String tokenType,
      double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
    Intent intent = new Intent(context, EndpointActivity.class);
    intent.putExtra(POSITION, position);
    intent.putExtra(Constants.ACCESS_TOKEN, accessToken);
    intent.putExtra(Constants.TOKEN_TYPE, tokenType);
    intent.putExtra(Constants.START_LAT, startLatitude);
    intent.putExtra(Constants.START_LON, startLongitude);
    intent.putExtra(Constants.END_LAT, endLatitude);
    intent.putExtra(Constants.END_LON, endLongitude);
    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    final Intent intent = getIntent();
    final double startLat = intent.getDoubleExtra(Constants.START_LAT, 0);
    final double startLon = intent.getDoubleExtra(Constants.START_LON, 0);
    final double endLat = intent.getDoubleExtra(Constants.END_LAT, 0);
    final double endLon = intent.getDoubleExtra(Constants.END_LON, 0);

    int position = getIntent().getIntExtra(POSITION, 0);
    switch (position) {
      case 1:
        UberAPIClient.getUberV1APIClient().getProducts(getAccessToken(),
            startLat,
            startLon,
            new UberCallback<ProductList>() {
              @Override
              public void success(ProductList productList, Response response) {
                setupListAdapter("products", productList.toString());
              }
            });
        break;
      case 2:
        UberAPIClient.getUberV1APIClient().getTimeEstimates(getAccessToken(),
            startLat,
            startLon,
            new UberCallback<TimeEstimateList>() {
              @Override
              public void success(TimeEstimateList timeEstimateList, Response response) {
                setupListAdapter("time", timeEstimateList.toString());
              }
            });
        break;
      case 3:
        UberAPIClient.getUberV1APIClient().getPriceEstimates(getAccessToken(),
            startLat,
            startLon,
            endLat,
            endLon,
            new UberCallback<PriceEstimateList>() {
              @Override
              public void success(PriceEstimateList priceEstimateList, Response response) {
                setupListAdapter("price", priceEstimateList.toString());
              }
            });
        break;
      case 4:
        UberAPIClient.getUberV1APIClient().getUserActivity(getAccessToken(),
            0,
            5,
            new UberCallback<UserActivity>() {
              @Override
              public void success(UserActivity userActivity, Response response) {
                setupListAdapter("history (v1)", userActivity.toString());
              }
            });
        break;
      case 5:
        UberAPIClient.getUberV1_1APIClient().getUserActivity(getAccessToken(),
            0,
            5,
            new UberCallback<UserActivity>() {
              @Override
              public void success(UserActivity userActivity, Response response) {
                setupListAdapter("history (v1.1)", userActivity.toString());
              }
            });
        break;
      case 6:
        UberAPIClient.getUberV1APIClient().getProfile(getAccessToken(),
            new UberCallback<Profile>() {
              @Override
              public void success(Profile profile, Response response) {
                setupListAdapter("me", profile.toString());
              }
            });
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupListAdapter(String endpoint, String response) {
    List<String> options = new ArrayList<String>();
    options.add(getString(R.string.endpoint_list_header_text, endpoint));
    options.add(getString(R.string.endpoint_list_result_text, endpoint));
    options.add(response);

    ListView listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
        android.R.id.text1, options));
  }


  private String getAccessToken() {
    return getIntent().getStringExtra(Constants.TOKEN_TYPE) + " "
        + getIntent().getStringExtra(Constants.ACCESS_TOKEN);
  }
}
