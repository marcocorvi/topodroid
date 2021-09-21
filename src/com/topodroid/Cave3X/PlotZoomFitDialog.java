/* @file PlotZoomFitDialog.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid plot zomm-fit / landscape
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;


import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
// import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

class PlotZoomFitDialog extends MyDialog
                        implements OnClickListener
                                 // FIXED_ZOOM 
                                 , AdapterView.OnItemSelectedListener
{
  private final DrawingWindow mParent;
  private Button mBtnPortrait;
  private Button mBtnLandscape;
  private Button mBtnStation;
  private EditText mETstation;
  // private Button mBtnZoomFit;

  // FIXED_ZOOM
  private Button mBtnZoomFix = null;
  // // private CheckBox mCBZoomFix = null;
  private Spinner mSpinZoom;
  private int mSelectedPos;
  private static final String[] mZooms = { "---", "1 : 100", "1 : 200", "1 : 300", "1 : 400", "1 : 500" };

  PlotZoomFitDialog( Context context, DrawingWindow parent )
  {
    super( context, R.string.PlotZoomFitDialog );
    mParent = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.plot_zoomfit_dialog, R.string.title_plot_zoomfit );

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );

    LinearLayout layout_orientation = (LinearLayout) findViewById( R.id.layout_orientation );
    layout_orientation.setMinimumHeight( size + 20 );
    
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20 ,10 );

    // mBtnZoomFit = new MyCheckBox( mContext, size, R.drawable.iz_zoomfit, R.drawable.iz_zoomfit );
    // mBtnZoomFit.setOnClickListener( this );
    // layout_orientation.addView( mBtnZoomFit );
    // mBtnZoomFit.setLayoutParams( lp );

    // ((Button) findViewById( R.id.button_zoomfit )).setOnClickListener( this );
    // mBtnPortrait  = (Button) findViewById( R.id.button_portrait );
    // mBtnLandscape = (Button) findViewById( R.id.button_landscape );
    // mBtnPortrait.setOnClickListener( this );
    // mBtnLandscape.setOnClickListener( this );
    // if ( mParent.isLandscape() ) {
      mBtnPortrait = new MyCheckBox( mContext, size, R.drawable.iz_northup, R.drawable.iz_northup );
      mBtnPortrait.setOnClickListener( this );
      layout_orientation.addView( mBtnPortrait );
      mBtnPortrait.setLayoutParams( lp );
      // mBtnLandscape.setText( R.string.button_zoomfit );
    // } else {
      mBtnLandscape = new MyCheckBox( mContext, size, R.drawable.iz_northleft, R.drawable.iz_northleft );
      mBtnLandscape.setOnClickListener( this );
      layout_orientation.addView( mBtnLandscape );
      mBtnLandscape.setLayoutParams( lp );
      // mBtnPortrait.setText( R.string.button_zoomfit );
    // }

    // FIXED_ZOOM
    if ( TDLevel.overExpert ) {
      mBtnZoomFix = (Button) findViewById( R.id.button_zoom );
      mBtnZoomFix.setOnClickListener( this );
      // mCBZoomFix = (CheckBox) findViewById( R.id.cb_zoom );
      // mCBZoomFix.setChecked( mParent.getFixedZoom() );
      mSpinZoom = (Spinner)findViewById( R.id.spin_zoom );
      mSpinZoom.setOnItemSelectedListener( this );
      ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mZooms );
      mSpinZoom.setAdapter( adapter );
      
      mSelectedPos = mParent.getFixedZoom();
      if ( mSelectedPos < 0 || mSelectedPos >= adapter.getCount() ) mSelectedPos = 0;
      mSpinZoom.setSelection( mSelectedPos );
    } else { 
      LinearLayout layout_zoom = (LinearLayout) findViewById( R.id.layout_zoom );
      layout_zoom.setVisibility( View.GONE );
    }

    ((Button) findViewById( R.id.button_cancel )).setOnClickListener( this );

    mBtnStation  = (Button) findViewById( R.id.button_station );
    mETstation = (EditText) findViewById( R.id.center_station );
    mBtnStation.setOnClickListener( this );
  }
 
  // ---------------------------------------------------------------
  // FIXED_ZOOM
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mSelectedPos = pos;
  }
  
  @Override
  public void onNothingSelected( AdapterView av ) 
  { 
    mSelectedPos = -1;
  }

  @Override 
  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.button_cancel ) {
      // nothing
    } else {
      Button btn = (Button) v;
      if ( btn == mBtnPortrait ) {
        mParent.setOrientation( PlotInfo.ORIENTATION_PORTRAIT );
      } else if ( btn == mBtnLandscape ) {
        mParent.setOrientation( PlotInfo.ORIENTATION_LANDSCAPE );
      // } else if ( btn == mBtnZoomFit ) {
      //   mParent.doZoomFit();
      // FIXED_ZOOM
      } else if ( TDLevel.overExpert && btn == mBtnZoomFix ) {
        // mParent.setFixedZoom( mCBZoomFix.isChecked() );
        mParent.setFixedZoom( (mSelectedPos < 0)? 0 : mSelectedPos );
      } else if ( btn == mBtnStation ) {
        String station = mETstation.getText().toString();
	if ( station == null || station.length() == 0 ) {
          mETstation.setError( mContext.getResources().getString( R.string.error_station_required ) );
	  return;
	}
	mParent.centerAtStation( station );
      }
    }
    dismiss();
  }

}
