package com.ubercalendar;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ubercalendar.api.UberCallback;
import com.ubercalendar.model.PriceEstimateList;
import com.ubercalendar.swiperefresh.SwipeRefreshListFragment;


import retrofit.client.Response;

/**
 * Created by amoi on 6/28/15.
 */
public class FareEstimateFragment extends SwipeRefreshListFragment {

  private static final String LOG_TAG = FareEstimateFragment.class.getSimpleName();

  private int counter = 0;
  private FareEstimatesAdapter estimatesAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    estimatesAdapter = new FareEstimatesAdapter(getActivity());
    setListAdapter(estimatesAdapter);

    setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
        refresh();
      }
    });

    final ListView lv = getListView();
    lv.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
      }

      @Override
      public void onScroll(AbsListView absListView, int firstVisibleItem,
          int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount != totalItemCount) {
          return;
        }
        Log.i(LOG_TAG, "FareEstimateFragment onScroll");
        setRefreshing(true);
        ((FareEstimateActivity) getActivity()).load(new UberCallback<PriceEstimateList>() {
          @Override
          public void success(PriceEstimateList priceEstimateList, Response response) {
            setRefreshing(false);
            estimatesAdapter.addAll(priceEstimateList.getPrices());
          }
        });
      }
    });
  }

  private void refresh() {
    Log.i(LOG_TAG, "refresh");
    setRefreshing(true);
    ((FareEstimateActivity) getActivity()).load(new UberCallback<PriceEstimateList>() {
      @Override
      public void success(PriceEstimateList priceEstimateList, Response response) {
        setRefreshing(false);
          estimatesAdapter.setCounter(++counter);
      }
    });
  }
}
