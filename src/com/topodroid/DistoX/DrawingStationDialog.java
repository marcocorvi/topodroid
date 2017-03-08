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
 */
package com.topodroid.DistoX;

import java.util.List;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


public class DrawingStationDialog extends MyDialog 
                                  implements View.OnClickListener
                                  , IBearingAndClino
{
    private TextView mLabel;
    private TextView mBarrierLabel;
    private TextView mHiddenLabel;
    private TextView mCoords;
    private Button mBtnOK;
    private Button mBtnSet;
    private Button mBtnBarrier;
    private Button mBtnHidden;
    private Button mBtnSplays;

    private Button mBtnXSection;
    private Button mBtnXDelete;
    private Button mBtnDirect;
    private Button mBtnInverse;

    private Button mBtnCancel;

    private DrawingWindow mParent;
    private DrawingStationName mStation; // num station point
    private DrawingStationPath mPath;

    private String mStationName;
    private boolean mIsBarrier;
    private boolean mIsHidden;
    private boolean mSensors;
    private float mBearing;
    private float mClino;
    private List<DBlock> mBlk;

    public DrawingStationDialog( Context context, DrawingWindow parent, DrawingStationName station,
                                 DrawingStationPath path,
                                 boolean is_barrier, boolean is_hidden, List<DBlock> blk )
    {
      super( context, R.string.DrawingStationDialog );
      mParent   = parent;
      mStation  = station;
      mPath     = path;
      mStationName = mStation.mName;
      mIsBarrier = is_barrier; 
      mIsHidden  = is_hidden; 
      mBlk       = blk;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
  
      String title = mContext.getResources().getString(R.string.STATION) + " " + mStationName;
      initLayout( R.layout.drawing_station_dialog, title );

      mLabel        = (TextView) findViewById(R.id.station_text);
      mBarrierLabel = (TextView) findViewById(R.id.barrier_text);
      mHiddenLabel  = (TextView) findViewById(R.id.hidden_text);
      mCoords       = (TextView) findViewById(R.id.coords);
      mCoords.setText( mStation.getCoordsString() );

      mBtnBarrier  = (Button) findViewById(R.id.btn_break );
      mBtnHidden = (Button) findViewById(R.id.btn_hidden );
      mBtnSplays = (Button) findViewById(R.id.btn_splays );
      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      mBtnSet    = (Button) findViewById(R.id.btn_set);
      mBtnCancel = (Button) findViewById(R.id.btn_cancel);

      mBtnXSection  = (Button) findViewById(R.id.btn_xsection );
      mBtnXDelete   = (Button) findViewById(R.id.btn_xdelete );  // delete / sensors
      mBtnDirect  = (Button) findViewById( R.id.btn_direct );
      mBtnInverse = (Button) findViewById( R.id.btn_inverse );


      mSensors = false;
      mBearing = 0;
      mClino   = 0;
      if ( mParent.isAnySection() ) {
        mBtnOK.setVisibility( View.GONE );
        mBtnSet.setVisibility( View.GONE );
        mBtnBarrier.setVisibility( View.GONE );
        mBtnHidden.setVisibility( View.GONE );
        mBarrierLabel.setVisibility( View.GONE );
        mHiddenLabel.setVisibility( View.GONE );
        mLabel.setVisibility( View.GONE );
        ((TextView)findViewById(R.id.station_set)).setVisibility( View.GONE );
        mBtnSplays.setOnClickListener( this );

        mBtnXSection.setVisibility( View.GONE );
        mBtnXDelete.setVisibility( View.GONE );
        mBtnDirect.setVisibility( View.GONE );
        mBtnInverse.setVisibility( View.GONE );
      } else {
        if ( TDSetting.mAutoStations ) {
          mBtnOK.setVisibility( View.GONE );
          mLabel.setVisibility( View.GONE );
        } else {
          if ( mPath != null ) {
            mLabel.setText( mContext.getResources().getString(R.string.station_del) );
          // } else {
          //   mLabel.setText( mContext.getResources().getString(R.string.station_ask) );
          }
          mBtnOK.setOnClickListener( this );
        }
        mBtnSet.setOnClickListener( this );
        mBtnSplays.setOnClickListener( this );
    
        if ( TDSetting.mLevelOverAdvanced ) {
          mBtnXDelete.setOnClickListener( this );

          if ( mStation.mXSectionType == PlotInfo.PLOT_NULL ) {
            int leg_size = mBlk.size();
            String direct  = null;
            String inverse = null;
            boolean cb = false; // no checkboxes
            if ( leg_size == 1 ) {
              DBlock leg0 = mBlk.get(0);
              direct  = leg0.mFrom + ">" + leg0.mTo;
              inverse = leg0.mTo   + ">" + leg0.mFrom;
              cb = true;
              mBearing = leg0.mBearing; 
              mClino   = leg0.mClino;
            } else if ( leg_size == 2 ) {
              DBlock leg0 = mBlk.get(0);
              DBlock leg1 = mBlk.get(1);
              float b0 = leg0.mBearing;
              float b1 = leg1.mBearing;
              mBearing = (b1 + b0)/2;
              if ( Math.abs( b1 - b0 ) > 180 ) {
                mBearing += 180;
                if ( mBearing >= 360 ) mBearing -= 360;
              }
              mClino = ( leg0.mClino + leg1.mClino ) / 2;
              String from = leg0.mFrom;
              if ( from.equals( mStationName ) ) from = leg0.mTo;
              String to = leg1.mTo;
              if ( to.equals( mStationName ) ) to = leg0.mFrom;
              direct = from + ">" + to; // skip mStationName in the middle
              inverse = to + ">" + from;
              cb = true;
            }
            if ( cb ) {
              mBtnDirect.setText( direct );
              mBtnInverse.setText( inverse );
              mBtnDirect.setOnClickListener( this );
              mBtnInverse.setOnClickListener( this );
            } else {
              mBtnDirect.setVisibility( View.GONE );
              mBtnInverse.setVisibility( View.GONE );
            }
            mBtnXDelete.setText( "" );
            mBtnXDelete.setBackgroundDrawable(
              MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_compass ) );
            mSensors = true;
            mBtnXSection.setBackgroundColor( TDColor.MID_GRAY );
          } else {
            mBtnXSection.setOnClickListener( this );
            mBtnDirect.setVisibility( View.GONE );
            mBtnInverse.setVisibility( View.GONE );
          } 
        } else {
          mBtnXSection.setVisibility( View.GONE );
          mBtnXDelete.setVisibility( View.GONE );
          mBtnDirect.setVisibility( View.GONE );
          mBtnInverse.setVisibility( View.GONE );
        }

        mBtnCancel.setOnClickListener( this );
        if ( mIsBarrier ) {
          mBtnBarrier.setOnClickListener( this );
        } else {
          mBtnBarrier.setVisibility( View.GONE );
          mBarrierLabel.setVisibility( View.GONE );
        }
        if ( mIsHidden ) {
          mBtnHidden.setOnClickListener( this );
        } else {
          mBtnHidden.setVisibility( View.GONE );
          mHiddenLabel.setVisibility( View.GONE );
        }
      }
    }

    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "Drawing Station Dialog onClick() " + view.toString() );
      Button b = (Button)view;
      if ( b == mBtnOK ) {
        if ( mPath == null ) {
          mParent.addStationPoint( mStation );
        } else {
          mParent.removeStationPoint( mStation, mPath );
        }
      } else if ( b == mBtnCancel ) {
        /* nothing */
      } else if ( b == mBtnSet ) {
        mParent.setCurrentStationName( mStationName );
      } else if ( b == mBtnBarrier ) {
        mParent.toggleStationBarrier( mStationName, mIsBarrier );
      } else if ( b == mBtnHidden ) {
        mParent.toggleStationHidden( mStationName, mIsHidden );
      } else if ( b == mBtnSplays ) {
        mParent.toggleStationSplays( mStationName );
      } else if ( b == mBtnXSection ) {
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mBearing, mClino );
      } else if ( b == mBtnXDelete ) {
        if ( mSensors ) {
          TimerTask timer = new TimerTask( mContext, this, TimerTask.Y_AXIS );
          timer.execute();
          return;
        } else {
          mParent.deleteXSection( mStation, mStationName, mParent.getPlotType() );
        }
      } else if ( b == mBtnDirect ) {
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mBearing, mClino );
      } else if ( b == mBtnInverse ) {
        mBearing += 180;
        if ( mBearing >= 360 ) mBearing -= 360;
        mClino = -mClino;
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mBearing, mClino );
      }
      dismiss();
    }

    public void setBearingAndClino( float b, float c )
    {
      if ( mParent.getPlotType() == PlotInfo.PLOT_PLAN ) {
        c = 0;
      } else { // PlotInfo.isProfile( type )
      }
      mParent.openXSection( mStation, mStationName, mParent.getPlotType(), b, c );
      dismiss();
    }

    public void setJpegData( byte[] data ) { }
  
}
        

