package com.ubercalendar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.ubercalendar.api.UberAPIClient;
import com.ubercalendar.model.PriceEstimate;
import com.ubercalendar.util.ItemListFragment;
import com.ubercalendar.util.Ln;
import com.ubercalendar.util.StringValues;
import com.ubercalendar.util.ThrowableLoader;

import java.util.List;

/**
 * Created by julian on 6/28/15.
 */
public class PriceEstimateList extends ItemListFragment<PriceEstimate> {
    public static PriceEstimateList newInstance(String token, String type,
            double startLatitude, double startLongitude,
            double endLatitude, double endLongitude) {

        Bundle args = new Bundle();
        args.putString(Constants.ACCESS_TOKEN, token);
        args.putString(Constants.TOKEN_TYPE, type);
        args.putDouble(Constants.START_LAT, startLatitude);
        args.putDouble(Constants.START_LON, startLongitude);
        args.putDouble(Constants.END_LAT, endLatitude);
        args.putDouble(Constants.END_LON, endLongitude);
        PriceEstimateList fragment = new PriceEstimateList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        Ln.e(exception.getCause() + exception.getMessage());
        return R.string.error;
    }
    double[] v;
    String token;
    String type;
    @Override protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        Bundle args = this.getArguments() ;
        if (args != null) {
            v = new double[]{args.getDouble(Constants.START_LAT),
                    args.getDouble(Constants.START_LON),args.getDouble(Constants.END_LAT),
                    args.getDouble(Constants.END_LON)};
            token = args.getString(Constants.ACCESS_TOKEN);
            type = args.getString(Constants.TOKEN_TYPE);
        }
        // TODO header footer
//        listView.setDividerHeight(0);
//        View view = activity.getLayoutInflater().inflate(R.layout.kba_header, null);
//        getListAdapter().addHeader(view);
//        ButterKnife.bind(this, view);
//        setHasOptionsMenu(true);
//        getActivity().getActionBar().setTitle("");

    }
    @Override public void onDestroyView() {
        setListAdapter(null);
        super.onDestroyView();
    }
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    private PriceEstimateListAdapter adapter;
    @Override
    protected SingleTypeAdapter<PriceEstimate> createAdapter(List<PriceEstimate> items) {
        adapter = new PriceEstimateListAdapter(getActivity().getLayoutInflater());
        adapter.setItems(items);
        return adapter;
    }

    @Override
    public Loader<List<PriceEstimate>> onCreateLoader(int id, Bundle args) {
        final List<PriceEstimate> initialItems = items;
        return new ThrowableLoader<List<PriceEstimate>>(getActivity(), items) {
            @Override
            public List<PriceEstimate> loadData() throws Exception {
                com.ubercalendar.model.PriceEstimateList pl = UberAPIClient.getUberV1APIClient().getPriceEstimates(
                        StringValues.TOKEN_TYPE.get() + " " +
                                StringValues.TOKEN.get(), v[0], v[1], v[2], v[3]);

                return pl.getPrices();
            }
        };
    }

    public class PriceEstimateListAdapter extends SingleTypeAdapter<PriceEstimate> {

        public PriceEstimateListAdapter(LayoutInflater inflater) {
            super(inflater, R.layout.fare_estimate_item);
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[]{R.id.type, R.id.estimate};
        }

        @Override
        protected void update(int i, PriceEstimate priceEstimate) {
            setText(0, priceEstimate.getProductId());
            setText(1, priceEstimate.getEstimate());
        }
    }
}
