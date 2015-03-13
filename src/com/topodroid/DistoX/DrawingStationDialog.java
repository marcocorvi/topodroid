/* @file DrawingStationDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a station point to the scrap
 *
 * for when station points are not automatically added
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;


public class DrawingStationDialog extends Dialog 
                                  implements View.OnClickListener
{
    private TextView mLabel;
    private TextView mBarrierLabel;
    private TextView mCoords;
    private Button mBtnOK;
    private Button mBtnSet;
    private Button mBtnBreak;
    // private Button mBtnCancel;

    private Context mContext;
    private DrawingActivity mActivity;
    private DrawingStationName mStation;

    private String mStationName;
    private boolean mIsBarrier;

    public DrawingStationDialog( Context context, DrawingActivity activity, DrawingStationName station,
                                 boolean is_barrier )
    {
      super(context);
      mContext  = context;
      mActivity = activity;
      mStation  = station;
      mStationName = mStation.mName;
      mIsBarrier = is_barrier; 
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.drawing_station_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

      mLabel     = (TextView) findViewById(R.id.station_text);
      mBarrierLabel = (TextView) findViewById(R.id.barrier_text);
      mCoords    = (TextView) findViewById(R.id.coords);
      mCoords.setText( mStation.getCoordsString() );

      mBtnBreak  = (Button) findViewById(R.id.btn_break );
      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      mBtnSet    = (Button) findViewById(R.id.btn_set);
      // mBtnCancel = (Button) findViewById(R.id.button_cancel);

      if ( TopoDroidSetting.mAutoStations ) {
        mBtnOK.setVisibility( View.GONE );
        mLabel.setVisibility( View.GONE );
      } else {
        mBtnOK.setOnClickListener( this );
      }
      mBtnSet.setOnClickListener( this );
      mBtnBreak.setOnClickListener( this );
      // mBtnCancel.setOnClickListener( this );

      if ( mIsBarrier ) {
        mBarrierLabel.setText( mContext.getResources().getString(R.string.barrier_del) );
      }

      setTitle( mContext.getResources().getString(R.string.STATION) + mStationName ); 
    }

    public void onClick(View view)
    {
      // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingStationDialog onClick() " + view.toString() );
      if (view.getId() == R.id.btn_ok ) {
        mActivity.addStationPoint( mStation );
      } else if (view.getId() == R.id.btn_set ) {
        mActivity.setCurrentStationName( mStation.mName );
      } else if (view.getId() == R.id.btn_break ) {
        mActivity.toggleStationBarrier( mStationName, mIsBarrier );
      }
      dismiss();
    }
}
        

