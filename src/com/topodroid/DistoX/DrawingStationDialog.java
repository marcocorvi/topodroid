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
    private Button mBtnXDelete;
    private Button mBtnCancel;
    private CheckBox mCBdirect;
    private CheckBox mCBinverse;

    private DrawingWindow mParent;
    private DrawingStationName mStation; // num station point
    private DrawingStationPath mPath;

    private String mStationName;
    private boolean mIsBarrier;
    private boolean mIsHidden;
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

      mBtnBreak  = (Button) findViewById(R.id.btn_break );
      mBtnHidden = (Button) findViewById(R.id.btn_hidden );
      mBtnSplays = (Button) findViewById(R.id.btn_splays );
      mBtnXSection  = (Button) findViewById(R.id.btn_xsection );
      mBtnXDelete   = (Button) findViewById(R.id.btn_xdelete );
      mBtnOK     = (Button) findViewById(R.id.btn_ok);
      mBtnSet    = (Button) findViewById(R.id.btn_set);
      mBtnCancel = (Button) findViewById(R.id.btn_cancel);

      mCBdirect  = (CheckBox) findViewById( R.id.cb_direct );
      mCBinverse = (CheckBox) findViewById( R.id.cb_inverse );
      mCBdirect.setChecked( true );
      mCBinverse.setChecked( false );

      if ( mParent.isAnySection() ) {
        mBtnOK.setVisibility( View.GONE );
        mBtnSet.setVisibility( View.GONE );
        mBtnBreak.setVisibility( View.GONE );
        mBtnHidden.setVisibility( View.GONE );
        mBtnXSection.setVisibility( View.GONE );
        mBtnXDelete.setVisibility( View.GONE );
        mBarrierLabel.setVisibility( View.GONE );
        mHiddenLabel.setVisibility( View.GONE );
        mLabel.setVisibility( View.GONE );
        ((TextView)findViewById(R.id.station_set)).setVisibility( View.GONE );
        mBtnSplays.setOnClickListener( this );
        mCBdirect.setVisibility( View.GONE );
        mCBinverse.setVisibility( View.GONE );
      } else {
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
          int leg_size = mBlk.size();
          String direct  = null;
          String inverse = null;
          if ( leg_size == 1 ) {
            DBlock leg0 = mBlk.get(0);
            direct  = leg0.mFrom + ">" + leg0.mTo;
            inverse = leg0.mTo   + ">" + leg0.mFrom;
          } else if ( leg_size == 2 ) {
            DBlock leg0 = mBlk.get(0);
            DBlock leg1 = mBlk.get(1);
            String from = leg0.mFrom;
            if ( from.equals( mStationName ) ) from = leg0.mTo;
            String to = leg1.mTo;
            if ( to.equals( mStationName ) ) to = leg0.mFrom;
            direct = from + ">" + to; // skip mStationName in the middle
            inverse = to + ">" + from;
          }
          if ( inverse != null ) {
            mBtnXSection.setOnClickListener( this );
            if ( mStation.mXSectionType != PlotInfo.PLOT_NULL ) {
              mBtnXDelete.setOnClickListener( this );
              mCBdirect.setVisibility( View.GONE );
              mCBinverse.setVisibility( View.GONE );
            } else {
              mBtnXDelete.setVisibility( View.GONE );
              mCBdirect.setText( direct );
              mCBinverse.setText( inverse );
              mCBdirect.setOnClickListener( new View.OnClickListener() {
                @Override public void onClick( View v ) {
                  mCBdirect.setChecked( true );
                  mCBinverse.setChecked( false );
                } } );
              mCBinverse.setOnClickListener( new View.OnClickListener() {
                @Override public void onClick( View v ) {
                  mCBinverse.setChecked( true );
                  mCBdirect.setChecked( false );
                } } );
            }
          } else {
            mBtnXSection.setVisibility( View.GONE );
            mBtnXDelete.setVisibility( View.GONE );
            mCBdirect.setVisibility( View.GONE );
            mCBinverse.setVisibility( View.GONE );
          }
        } else {
          mBtnXSection.setVisibility( View.GONE );
          mBtnXDelete.setVisibility( View.GONE );
          mCBdirect.setVisibility( View.GONE );
          mCBinverse.setVisibility( View.GONE );
        }

        mBtnCancel.setOnClickListener( this );
        if ( mIsBarrier ) {
          mBarrierLabel.setText( mContext.getResources().getString(R.string.barrier_del) );
        }
        if ( mIsHidden ) {
          mHiddenLabel.setText( mContext.getResources().getString(R.string.hidden_del) );
        }
      }
    }

    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "Drawing Station Dialog onClick() " + view.toString() );
      if (view.getId() == R.id.btn_ok ) {
        if ( mPath == null ) {
          mParent.addStationPoint( mStation );
        } else {
          mParent.removeStationPoint( mStation, mPath );
        }
      } else if (view.getId() == R.id.btn_cancel ) {
        /* nothing */
      } else if (view.getId() == R.id.btn_set ) {
        mParent.setCurrentStationName( mStation.mName );
      } else if (view.getId() == R.id.btn_break ) {
        mParent.toggleStationBarrier( mStationName, mIsBarrier );
      } else if (view.getId() == R.id.btn_hidden ) {
        mParent.toggleStationHidden( mStationName, mIsHidden );
      } else if (view.getId() == R.id.btn_splays ) {
        mParent.toggleStationSplays( mStationName );
      } else if (view.getId() == R.id.btn_xsection ) {
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mCBinverse.isChecked() );
      } else if (view.getId() == R.id.btn_xdelete ) {
        mParent.deleteXSection( mStation, mStationName, mParent.getPlotType() );
      }
      dismiss();
    }
}
        

