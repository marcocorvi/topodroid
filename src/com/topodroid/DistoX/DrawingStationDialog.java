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
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
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
    private TextView mHiddenLabel;
    private TextView mCoords;
    private Button mBtnOK;
    private Button mBtnSet;
    private Button mBtnBreak;
    private Button mBtnHidden;
    private Button mBtnSplays;
    private Button mBtnXSection;
    // private Button mBtnCancel;

    private Context mContext;
    private DrawingActivity mActivity;
    private DrawingStationName mStation;
    private DrawingStationPath mPath;

    private String mStationName;
    private boolean mIsBarrier;
    private boolean mIsHidden;

    public DrawingStationDialog( Context context, DrawingActivity activity, DrawingStationName station,
                                 DrawingStationPath path,
                                 boolean is_barrier, boolean is_hidden )
    {
      super(context);
      mContext  = context;
      mActivity = activity;
      mStation  = station;
      mPath     = path;
      mStationName = mStation.mName;
      mIsBarrier = is_barrier; 
      mIsHidden  = is_hidden; 
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.drawing_station_dialog);
      getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

      mLabel     = (TextView) findViewById(R.id.station_text);
      mBarrierLabel = (TextView) findViewById(R.id.barrier_text);
      mHiddenLabel  = (TextView) findViewById(R.id.hidden_text);
      mCoords    = (TextView) findViewById(R.id.coords);
      mCoords.setText( mStation.getCoordsString() );

      mBtnBreak  = (Button) findViewById(R.id.btn_break );
      mBtnHidden = (Button) findViewById(R.id.btn_hidden );
      mBtnSplays = (Button) findViewById(R.id.btn_splays );
      mBtnXSection  = (Button) findViewById(R.id.btn_xsection );
      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      mBtnSet    = (Button) findViewById(R.id.btn_set);
      // mBtnCancel = (Button) findViewById(R.id.button_cancel);

      if ( TDSetting.mAutoStations ) {
        mBtnOK.setVisibility( View.GONE );
        mLabel.setVisibility( View.GONE );
      } else {
        mBtnOK.setOnClickListener( this );
      }
      mBtnSet.setOnClickListener( this );
      mBtnBreak.setOnClickListener( this );
      mBtnHidden.setOnClickListener( this );
      mBtnSplays.setOnClickListener( this );
    
      if ( TDSetting.mLevelOverAdvanced ) {
        mBtnXSection.setOnClickListener( this );
      } else {
        mBtnXSection.setVisibility( View.GONE );
      }

      // mBtnCancel.setOnClickListener( this );

      if ( mIsBarrier ) {
        mBarrierLabel.setText( mContext.getResources().getString(R.string.barrier_del) );
      }

      if ( mIsHidden ) {
        mHiddenLabel.setText( mContext.getResources().getString(R.string.hidden_del) );
      }

      setTitle( mContext.getResources().getString(R.string.STATION) + mStationName ); 
    }

    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "Drawing Station Dialog onClick() " + view.toString() );
      if (view.getId() == R.id.btn_ok ) {
        if ( mPath == null ) {
          mActivity.addStationPoint( mStation );
        } else {
          mActivity.removeStationPoint( mStation, mPath );
        }
      } else if (view.getId() == R.id.btn_set ) {
        mActivity.setCurrentStationName( mStation.mName );
      } else if (view.getId() == R.id.btn_break ) {
        mActivity.toggleStationBarrier( mStationName, mIsBarrier );
      } else if (view.getId() == R.id.btn_hidden ) {
        mActivity.toggleStationHidden( mStationName, mIsHidden );
      } else if (view.getId() == R.id.btn_splays ) {
        mActivity.toggleStationSplays( mStationName );
      } else if (view.getId() == R.id.btn_xsection ) {
        mActivity.openXSection( mStation, mStationName, mActivity.getPlotType() );
      }
      dismiss();
    }
}
        

