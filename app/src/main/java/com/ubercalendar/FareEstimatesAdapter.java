package com.ubercalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ubercalendar.model.PriceEstimate;

/**
 * Created by amoi on 6/23/15.
 */
public class FareEstimatesAdapter extends ArrayAdapter<PriceEstimate> {

  public FareEstimatesAdapter(Context context) {
    super(context, R.layout.fare_estimate_item);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final LayoutInflater inflater = LayoutInflater.from(getContext());
    final RelativeLayout layout;
    if (convertView == null) {
      layout = (RelativeLayout) inflater.inflate(R.layout.fare_estimate_item, null);
    } else {
      layout = (RelativeLayout) convertView;
    }
    PriceEstimate priceEstimate = getItem(position);
    TextView type = (TextView) layout.findViewById(R.id.type);
    type.setText(priceEstimate.getDisplayName());
    TextView estimate = (TextView) layout.findViewById(R.id.estimate);
    estimate.setText(priceEstimate.getEstimate());
    return layout;
  }
}
