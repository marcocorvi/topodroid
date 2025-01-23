package com.topodroid.TDX;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.ViewHolder> {
  private final ArrayList<String> mStations;
  private final LayoutInflater mInflater;

  public StationListAdapter(Context context, ArrayList<String> stations) {
    this.mInflater = LayoutInflater.from(context);
    this.mStations = stations;
  }

  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = mInflater.inflate(R.layout.station_list_item, parent, false);
    return new ViewHolder(view);
  }

  public void onBindViewHolder(ViewHolder holder, int position) {
    String station = mStations.get(position);
    holder.mStationName.setText(station);
  }

  public int getItemCount() {
      return mStations.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView mStationName;

    ViewHolder(View itemView) {
      super(itemView);
      mStationName = itemView.findViewById(R.id.unadjusted_stations);
    }
  }
}