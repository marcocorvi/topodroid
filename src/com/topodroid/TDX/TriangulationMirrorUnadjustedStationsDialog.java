package com.topodroid.TDX;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.topodroid.ui.MyDialog;

import java.util.ArrayList;

public class TriangulationMirrorUnadjustedStationsDialog extends MyDialog
{
  final private String mStationName;
  final private boolean mIsMirror;
  final private ArrayList<String> mUnadjustedStations;
  final private Context mContext;
  final private DrawingWindow mParent;
  final private TopoDroidApp mApp;
  final private TriangulationMirrorUnadjustedStationsDialogListener mListener;


  /**
   * cstr
   *
   * @param context       context
   * @param station_name  station name
   *                      (the station that is being mirrored or unmirrored)
   * @param is_mirror     true if mirroring, false if unmirroring
   *                      (used to determine the action to be taken)
   * @param unadjusted_stations  list of unadjusted stations
   *                             (stations that are no more adjusted after action)
   *
   */
  public TriangulationMirrorUnadjustedStationsDialog(
      Context context,
      DrawingWindow parent,
      TopoDroidApp app,
      TriangulationMirrorUnadjustedStationsDialogListener listener,
      String station_name,
      boolean is_mirror,
      ArrayList<String> unadjusted_stations
  ) {
    super(context, null, R.string.TriangulationMirrorUnadjustedStationsDialog);
    mContext = context;
    mParent = parent;
    mApp = app;
    mListener = listener;
    mStationName = station_name;
    mIsMirror = is_mirror;
    mUnadjustedStations = unadjusted_stations;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String title = mContext.getResources().getString(R.string.triangulation_unadjusted_stations_title);
    initLayout(R.layout.triangulation_mirrror_unadjusted_stations_dialog, title);

    String action = mContext.getResources().getString(mIsMirror ?
        R.string.triangulation_unmirroring :
        R.string.triangulation_mirroring
    );

    TextView mExplanation = (TextView) findViewById(R.id.triangulation_unadjusted_stations_explanation);
    RecyclerView mStationList = (RecyclerView) findViewById(R.id.unadjusted_stations_list);
    TextView mKeepMirroring = (TextView) findViewById(R.id.triangulation_keep_mirroring);
    Button mCancel = (Button) findViewById(R.id.button_cancel);
    Button mOk = (Button) findViewById(R.id.button_ok);

    mExplanation.setText(String.format(
        mContext.getResources().getString(R.string.triangulation_unadjusted_stations_explanation),
        action, mStationName
    ));

    mStationList.setLayoutManager(new LinearLayoutManager(mContext));
    mStationList.setAdapter(new StationListAdapter(mContext, mUnadjustedStations));

    mKeepMirroring.setText(String.format(
        mContext.getResources().getString(R.string.triangulation_keep_mirroring),
        action,
        mStationName
    ));

    mCancel.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mListener.onTriangulationMirrorUnadjustedStationsDialogClosed("Cancel", mStationName, !mIsMirror);
        dismiss();
      }
    } );
    mOk.setOnClickListener( new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mListener.onTriangulationMirrorUnadjustedStationsDialogClosed("OK", mStationName, !mIsMirror);
        dismiss();
      }
    } );
  }
}
