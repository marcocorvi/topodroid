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
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.num.Tri2StationAxle;
import com.topodroid.num.Tri2StationStatus;
import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.MyButton;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
import com.topodroid.common.StationFlag;

import java.util.List;

import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;

class DrawingStationDialog extends MyDialog
                           implements View.OnClickListener
                           , IBearingAndClino
                           , IGeoCoder
{
    // private TextView mLabel;
    // private TextView mBarrierLabel;
    // private TextView mHiddenLabel;
    // private TextView mCoords;
    private Button mBtnOK;
    private Button mBtnCurrent;
    private TextView mTextCurrent;
    private Button mBtnBarrier;
    private Button mBtnHidden;
    private CheckBox mCbSplaysOn;
    private CheckBox mCbSplaysOff;

    private EditText mETnick;
    private EditText mComment;
    private Button mBtnXSection;
    private Button mBtnXDelete;
    private Button mBtnDirect;
    private Button mBtnInverse;
    private CheckBox mCBhorizontal;

    private Button mBtnOkComment; // saved station dialog
    private Button mBtnSaved; // saved station dialog
    private Button mBtnCancel;
    private Button mBtnGeoCode;

    private TextView mTriangulationTitle;
    private Button mTriangulationMirrorButton;
    private TextView mTriangulationText;

    private final DrawingWindow mParent;
    private final DrawingStationName mStation; // num station point
    private final DrawingStationUser mPath;
    private final TopoDroidApp mApp;

    private String mStationName;
    private int mFlag; // saved-station flag (if any)
    private boolean mIsBarrier;
    private boolean mIsHidden;
    // private boolean mGlobalXSections; // unused
    private float mBearing;
    private float mClino;
    private String mGeoCode = "";
    private List< DBlock > mBlk;
    private Tri2StationStatus mTriStatus;
    private Tri2StationAxle mTriAxle;
    private boolean mTriMirrored;

    // cannot use disabled compass, otherwise there is no way to choose x-section at junction station
    // private boolean sensorCheck; // whether android sensor is enabled
    private boolean mSensors;    // whether use compass or delete x-section

    DrawingStationDialog(Context context, DrawingWindow parent, TopoDroidApp app,
                         DrawingStationName station, DrawingStationUser path,
                         boolean is_barrier, boolean is_hidden, // boolean global_xsections,
                         List< DBlock > blk, Tri2StationStatus triStatus, Tri2StationAxle triAxle, boolean triMirrored )
    {
      super( context, null, R.string.DrawingStationDialog ); // null app
      mParent   = parent;
      mApp      = app;
      mStation  = station;
      mPath     = path;
      mStationName = mStation.getName();
      mIsBarrier = is_barrier; 
      mIsHidden  = is_hidden; 
      // mGlobalXSections = global_xsections;
      mBlk       = blk;
      // sensorCheck = TDSetting.mWithAzimuth && TDLevel.overNormal;
      mTriStatus = triStatus;
      mTriAxle   = triAxle;
      mTriMirrored = triMirrored;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
  
      String title = mContext.getResources().getString(R.string.STATION) + " " + mStationName;
      initLayout( R.layout.drawing_station_dialog, title );

      TextView mLabel        = (TextView) findViewById(R.id.station_text);
      TextView mBarrierLabel = (TextView) findViewById(R.id.barrier_text);
      TextView mHiddenLabel  = (TextView) findViewById(R.id.hidden_text);
      TextView mCoords       = (TextView) findViewById(R.id.coords);
      mCoords.setText( mStation.getCoordsString() );

      mBtnBarrier  = (Button) findViewById(R.id.btn_barrier );
      mBtnHidden = (Button) findViewById(R.id.btn_hidden );
      mCbSplaysOn  = (CheckBox) findViewById(R.id.btn_splays_on );
      mCbSplaysOff = (CheckBox) findViewById(R.id.btn_splays_off );
      mBtnOK       = (Button) findViewById(R.id.btn_ok);
      mBtnCurrent  = (Button) findViewById(R.id.btn_set);
      mTextCurrent = (TextView) findViewById(R.id.station_set);
      mBtnSaved    = (Button) findViewById(R.id.btn_saved);
      mBtnCancel   = (Button) findViewById(R.id.btn_cancel);

      mBtnXSection  = (Button) findViewById(R.id.btn_xsection );
      mCBhorizontal = (CheckBox) findViewById(R.id.cb_horizontal );
      mBtnXDelete   = (Button) findViewById(R.id.btn_xdelete );  // delete / sensors
      mBtnDirect  = (Button) findViewById( R.id.btn_direct );
      mBtnInverse = (Button) findViewById( R.id.btn_inverse );
      mETnick = (EditText) findViewById( R.id.nick );  // section name

      mBtnOkComment = (Button) findViewById( R.id.btn_ok_comment );
      mComment = (EditText) findViewById( R.id.comment );
      mFlag    = StationFlag.STATION_NONE;
      if ( TDLevel.overExpert ) {
        StationInfo cs = TopoDroidApp.mData.getStation( TDInstance.sid, mStationName, null ); // null: do not create
        mBtnOkComment.setOnClickListener( this );
        if ( cs != null ) {
          mComment.setText( cs.mComment );
          mFlag = cs.mFlag.getValue();
        // } else {
        //   mComment.setVisibility( View.GONE );
        }
      } else {
        mBtnOkComment.setVisibility( View.GONE );
        mComment.setVisibility( View.GONE );
      }

      mSensors = false;
      mBearing = 0;
      mClino   = 0;
      if ( mParent.isAnySection() ) {
        mBtnOK.setVisibility( View.GONE );
        ((LinearLayout)findViewById( R.id.layout_set )).setVisibility( View.GONE );
        // mBtnCurrent.setVisibility( View.GONE );
        // ((TextView)findViewById(R.id.station_set)).setVisibility( View.GONE );
        ((LinearLayout)findViewById( R.id.layout_barrier )).setVisibility( View.GONE );
        // mBtnBarrier.setVisibility( View.GONE );
        // mBarrierLabel.setVisibility( View.GONE );
        ((LinearLayout)findViewById( R.id.layout_hidden )).setVisibility( View.GONE );
        // mBtnHidden.setVisibility( View.GONE );
        // mHiddenLabel.setVisibility( View.GONE );
        mLabel.setVisibility( View.GONE );
        mCbSplaysOn.setOnClickListener( this );
        mCbSplaysOff.setOnClickListener( this );
        mCbSplaysOn.setChecked( mParent.isStationSplaysOn( mStationName ) );
        mCbSplaysOff.setChecked( mParent.isStationSplaysOff( mStationName ) );

        mETnick.setVisibility( View.GONE );
        mBtnXSection.setVisibility( View.GONE );
        mCBhorizontal.setVisibility( View.GONE );
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
        mBtnCurrent.setOnClickListener( this );
        if ( mStationName.equals( StationName.getCurrentStationName() ) ) {
          mTextCurrent.setText( R.string.station_reset );
        }
        mCbSplaysOn.setOnClickListener( this );
        mCbSplaysOff.setOnClickListener( this );
        mCbSplaysOn.setChecked( mParent.isStationSplaysOn( mStationName ) );
        mCbSplaysOff.setChecked( mParent.isStationSplaysOff( mStationName ) );
    
        if ( TDLevel.overNormal ) {
          mBtnXDelete.setOnClickListener( this );

          if ( mStation.mXSectionType == PlotType.PLOT_NULL ) {
            int leg_size = mBlk.size();
            String direct  = null;
            String inverse = null;
            boolean cb = false; // no checkboxes
            if ( leg_size == 1 ) {
              DBlock leg0 = mBlk.get(0);
              direct  = String.format( mContext.getResources().getString(R.string.arrow_from_to), leg0.mFrom, leg0.mTo );
              inverse = String.format( mContext.getResources().getString(R.string.arrow_from_to), leg0.mTo,   leg0.mFrom );
              cb = true;
              mBearing = leg0.mBearing; 
              mClino   = leg0.mClino; // later reset to 0 if PLAN
            } else if ( leg_size == 2 ) {
              DBlock leg0 = mBlk.get(0);
              DBlock leg1 = mBlk.get(1);
              float b0 = leg0.mBearing;
              float b1 = leg1.mBearing;
              float c0 = leg0.mClino;
              float c1 = leg1.mClino;
              // TDLog.v( "legs " + leg0.mFrom + " " + leg0.mTo + " .. " + leg1.mFrom + " " + leg1.mTo );

              String from = leg0.mFrom;
              if ( from.equals( mStationName ) ) {
                from = leg0.mTo;
                // b0 += 180; if ( b0 >= 360 ) b0 -= 360;
                b0 = TDMath.add180( b0 );
                c0 = -c0;
              }
              String to = leg1.mTo;
              if ( to.equals( mStationName ) ) {
                to = leg1.mFrom;
                // b1 += 180; if ( b1 >= 360 ) b1 -= 360;
                b1 = TDMath.add180( b1 );
                c1 = -c1;
              }

              mBearing = (b1 + b0)/2;
              if ( Math.abs( b1 - b0 ) > 180 ) {
                // like adding 360 to b1
                // mBearing += 180; if ( mBearing >= 360 ) mBearing -= 360;
                mBearing = TDMath.add180( mBearing );
              }
              mClino = ( c0 + c1 ) / 2; // later reset to 0 if PLAN

              direct  = String.format( mContext.getResources().getString(R.string.arrow_from_to), from, to); // skip mStationName in the middle
              inverse = String.format( mContext.getResources().getString(R.string.arrow_from_to), to, from); 
              cb = true;
            }
            if ( mParent.getPlotType() == PlotType.PLOT_PLAN ) {
              mClino = 0;
              mCBhorizontal.setVisibility( View.GONE );
            } else {
              mCBhorizontal.setChecked( false );
            }

            if ( cb ) { // leg_size == 1 or 2
              mBtnDirect.setText( direct );
              mBtnInverse.setText( inverse );
              mBtnDirect.setOnClickListener( this );
              mBtnInverse.setOnClickListener( this );
            } else {
              mBtnDirect.setVisibility( View.GONE );
              mBtnInverse.setVisibility( View.GONE );
            }

            // if ( sensorCheck ) {
              mBtnXDelete.setText( "" );
              TDandroid.setButtonBackground( mBtnXDelete, MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_compass ) );
              mSensors = true;
              mBtnXDelete.setOnClickListener( this );
            // } else {
            //   mBtnXDelete.setVisibility( View.GONE );
            // }

            mBtnXSection.setBackgroundColor( TDColor.MID_GRAY );
            // if ( mGlobalXSections ) {
            //   mETnick.setVisibility( View.GONE );
            // }
          } else {
            // if ( mGlobalXSections ) {
            //   mETnick.setVisibility( View.GONE );
            // } else
            String nick = mParent.getXSectionNick( mStationName, mParent.getPlotType() );
            if ( nick != null ) {
              mETnick.setText( nick );
              // mETnick.setFocusable( false ); 
              // TDLog.v( "Station " + mStationName + " nick <" + nick + ">" );
            }
            mBtnXSection.setOnClickListener( this );
            mCBhorizontal.setVisibility( View.GONE );
            mBtnXDelete.setOnClickListener( this );
            mBtnDirect.setVisibility( View.GONE );
            mBtnInverse.setVisibility( View.GONE );
          } 
        } else { // level <= normal
          mETnick.setVisibility( View.GONE );
          mBtnXSection.setVisibility( View.GONE );
          mCBhorizontal.setVisibility( View.GONE );
          mBtnXDelete.setVisibility( View.GONE );
          mBtnDirect.setVisibility( View.GONE );
          mBtnInverse.setVisibility( View.GONE );
        }

        if ( TDLevel.overNormal ) {
          mBtnSaved.setOnClickListener( this );
        } else {
          mBtnSaved.setVisibility( View.GONE );
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

      mBtnGeoCode = (Button) findViewById( R.id.station_code );
      if ( TDLevel.overExpert ) {
        GeoCodes geocodes = TopoDroidApp.getGeoCodes();
        if ( geocodes.size() > 0 ) {
          mBtnGeoCode.setOnClickListener( this );
        } else {
          mBtnGeoCode.setVisibility( View.GONE );
        }
      } else {
        mBtnGeoCode.setVisibility( View.GONE );
      }

      mTriangulationTitle = findViewById( R.id.triangulation_title );
      mTriangulationMirrorButton = findViewById( R.id.btn_triangulation_mirror_unmirror );
      mTriangulationText = findViewById( R.id.triangulation_axle );
      if ( mTriStatus == null ) {
        mTriangulationTitle.setVisibility( View.GONE );
        mTriangulationMirrorButton.setVisibility( View.GONE );
        mTriangulationText.setVisibility( View.GONE );
      } else {
        mTriangulationTitle.setVisibility( View.VISIBLE );
        switch (mTriStatus) {
          case REFERENCE:
            mTriangulationText.setText( mContext.getResources().getString( R.string.triangulation_reference_station ) );
            mTriangulationMirrorButton.setVisibility( View.GONE );
            mTriangulationText.setVisibility( View.VISIBLE );
            break;
          case UNADJUSTED:
            mTriangulationText.setText( mContext.getResources().getString( R.string.triangulation_unadjusted_station) );
            mTriangulationMirrorButton.setVisibility( View.GONE );
            mTriangulationText.setVisibility( View.VISIBLE );
            break;
          case ADJUSTED:
            String mirrorButtonText = mContext.getResources().getString( mTriMirrored ?
                R.string.button_triangulation_unmirror_station :
                R.string.button_triangulation_mirror_station
            );
            mTriangulationText.setText( String.format( mContext.getResources().getString( R.string.triangulation_axle ), mTriAxle.getAxleName() ) );
            mTriangulationMirrorButton.setVisibility( View.VISIBLE );
            mTriangulationMirrorButton.setText( mirrorButtonText );
            mTriangulationText.setVisibility( View.VISIBLE );

            mTriangulationMirrorButton.setOnClickListener( this );
            break;
        }
      }
    }

    @Override
    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "Drawing Station Dialog onClick() " + view.toString() );
      Button b = (Button)view;

      String nick = "";
      if ( b == mBtnOK ) {
        if ( mPath == null ) {
          mParent.addStationPoint( mStation );
        } else {
          mParent.removeStationPoint( mStation, mPath );
        }
      } else if ( /* TDLevel.overExpert && */ b == mBtnGeoCode ) {
        (new GeoCodeDialog( mContext, this, mGeoCode )).show();
        return;
      } else if ( /* TDLevel.overExpert && */ b == mBtnOkComment ) {
        boolean fail = true;
        if ( mComment.getText() != null ) {
          String comment = mComment.getText().toString().trim();
          if ( comment.length() > 0 ) {
            // set/change saved-station comment - leave flags unchanged
            TopoDroidApp.mData.insertStation( TDInstance.sid, mStationName, comment, mFlag, mStationName, mGeoCode ); // PRESENTATION
            fail = false;
          } 
        } 
        if ( fail ) {
          mComment.setError( mContext.getResources().getString( R.string.error_comment_required ) );
          return;
        }
      } else if ( /* TDLevel.overNormal && */ b == mBtnSaved ) {
        dismiss();
        (new CurrentStationDialog( mContext, null, mApp, mStationName )).show();
        return;
      } else if ( b == mBtnCancel ) {
        /* nothing */
      } else if ( b == mBtnCurrent ) {
        mParent.setCurrentStationName( mStationName, mStation );
      } else if ( b == mBtnBarrier ) {
        mParent.toggleStationBarrier( mStationName, mIsBarrier );
      } else if ( b == mBtnHidden ) {
        mParent.toggleStationHidden( mStationName, mIsHidden );
      } else if ( b == mCbSplaysOn ) {
        mCbSplaysOff.setChecked( false );
        mParent.toggleStationSplays( mStationName, mCbSplaysOn.isChecked(), false );
      } else if ( b == mCbSplaysOff ) {
        mCbSplaysOn.setChecked( false );
        mParent.toggleStationSplays( mStationName, false, mCbSplaysOff.isChecked() );

      } else if ( b == mBtnXSection ) {
        nick = (mETnick.getText() != null)? mETnick.getText().toString() : null;
        // if ( nick.length() == 0 ) {
        //   mETnick.setError( mContext.getResources().getString( R.string.error_nick_required ) );
        //   return;
        // }
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mBearing, mClino, false, nick );
      } else if ( b == mBtnXDelete ) {
        if ( mSensors ) {
          TimerTask timer = new TimerTask( this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
          timer.execute();
          return;
        } else {
          mParent.deleteXSection( mStation, mStationName, mParent.getPlotType() );
        }
      } else if ( b == mBtnDirect ) {
        nick = (mETnick.getText() != null)? mETnick.getText().toString() : "";
        // if ( nick.length() == 0 ) {
        //   mETnick.setError( mContext.getResources().getString( R.string.error_nick_required ) );
        //   return;
        // }
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mBearing, mClino, mCBhorizontal.isChecked(), nick);
      } else if ( b == mBtnInverse ) {
        nick = (mETnick.getText() != null)? mETnick.getText().toString() : "";
        // if ( nick.length() == 0 ) {
        //   mETnick.setError( mContext.getResources().getString( R.string.error_nick_required ) );
        //   return;
        // }
        // mBearing += 180; if ( mBearing >= 360 ) mBearing -= 360;
        mBearing = TDMath.add180( mBearing );
        mClino = -mClino;
        mParent.openXSection( mStation, mStationName, mParent.getPlotType(), mBearing, mClino, mCBhorizontal.isChecked(), nick);
      } else if ( b == mTriangulationMirrorButton) {
        mParent.toggleTriMirror( mStationName, mTriMirrored );
      }
      dismiss();
    }

  /**
   * @param b  azimuth [degrees]
   * @param c  clino [degrees]
   * @param orientation  orientation (unused)
   * @param accuracy     sensor accuracy (unused)
   * @param cam          camera API (unused)
   */
    public void setBearingAndClino( float b, float c, int orientation, int accuracy, int cam )
    {
      // TDLog.v( "Station dialog set orientation " + orientation + " bearing " + b + " clino " + c );
      String nick = "";
      nick = (mETnick.getText() != null)? mETnick.getText().toString() : "";
      // if ( nick.length() == 0 ) {
      //   mETnick.setError( mContext.getResources().getString( R.string.error_nick_required ) );
      //   return;
      // }
      if ( mParent.getPlotType() == PlotType.PLOT_PLAN ) {
        c = 0;
      } else { // PlotType.isProfile( type )
      }
      mParent.openXSection( mStation, mStationName, mParent.getPlotType(), b, c, mCBhorizontal.isChecked(), nick );
      dismiss();
    }

  /** implement set the JPEG data - use default (comment wen minsdk = 24)
   * @param data   JPEG data
   * @return false: the JPEG data are not stored
   */
  public boolean setJpegData( byte[] data ) { return false; }
  
  public void setGeoCode( String geocode ) { mGeoCode = (geocode == null)? "" : geocode; }

}
        

