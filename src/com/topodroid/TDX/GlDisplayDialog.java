/* @file GlDisplayDialog.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief TopoDroid shot-list: display mode dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.util.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


class GlDisplayDialog extends MyDialog
                               implements View.OnClickListener
{
    private CheckBox mCBstation;  // whether to show stations
    private CheckBox mCBname;     // whether to show station names
    private CheckBox mCBsplay;    // whether to show splays
    private CheckBox mCBendpoint; // whether to show splay endpoints
    private CheckBox mCBwall;     // whether to show walls
    private CheckBox mCBsurface;  // whether to show surface
    private CheckBox mCBphoto;    // whether to show photo
    private CheckBox mCBref;      // whether to show reference
    private CheckBox mCBaxes;     // whether to show only axes
    // private Button mBtnRefresh;

    private final TopoGL mParent;

    GlDisplayDialog( Context context, TopoGL parent )
    {
      super( context, null, R.string.GlDisplayDialog ); // null app
      mParent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initLayout( R.layout.gl_display_dialog, R.string.title_mode );

        mCBstation  = (CheckBox) findViewById(R.id.cb_mode_station);
        mCBname     = (CheckBox) findViewById(R.id.cb_mode_name);
        mCBsplay    = (CheckBox) findViewById(R.id.cb_mode_splay);
        mCBendpoint = (CheckBox) findViewById(R.id.cb_mode_endpoint);
        mCBwall     = (CheckBox) findViewById(R.id.cb_mode_wall);
        mCBsurface  = (CheckBox) findViewById(R.id.cb_mode_surface);
        mCBphoto    = (CheckBox) findViewById(R.id.cb_mode_photo);
        mCBref      = (CheckBox) findViewById(R.id.cb_mode_ref);
        mCBaxes     = (CheckBox) findViewById(R.id.cb_mode_axes);

        ((Button) findViewById(R.id.btn_ok)).setOnClickListener( this );
        ((Button) findViewById(R.id.btn_cancel)).setOnClickListener( this );
        // mBtnRefresh = (Button) findViewById(R.id.button_mode_refresh);
        // mBtnRefresh.setOnClickListener( this );
        mCBstation.setChecked( mParent.isShowStations() ); 
        mCBname .setChecked( mParent.isStationNames() );   
        mCBsplay .setChecked( mParent.isShowSplays() );
        mCBendpoint.setChecked( mParent.isSplayEndpoint() );
        // if ( mParent.hasWalls() ) {
          mCBwall.setChecked( mParent.isShowWalls() );
        // } else {
        //   mCBwall.setVisibility( View.GONE );
        // }
        // if ( mParent.hasSurface() ) {
	  mCBsurface.setChecked( mParent.isShowSurface() );
        // } else {
        //   mCBsurface.setVisibility( View.GONE );
        // }
        if ( mParent.hasPhotos() ) {
	  mCBphoto.setChecked( mParent.isShowPhotos() );
        } else {
          mCBphoto.setVisibility( View.GONE );
        }
        mCBref.setChecked( mParent.isShowReference() );
	mCBaxes.setChecked( mParent.isReferenceAxes() );
    }

    @Override
    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "GlDisplayDialog onClick " + view.toString() );
      hide();
      int vid = view.getId();
      if ( vid == R.id.btn_ok ) {
        setParent();
      // } else if ( vid == R.id.btn_cancel ) {
      //   /* nothing */
      // } else if ( vid == R.id.button_mode_refresh ) {
      //   mParent.updateDisplay( );
      }
      dismiss();
    }

    // @SuppressWarnings("deprecated")
    // @Override
    // public void onBackPressed ()
    // {
    //   setParent();
    //   cancel();
    // }

    private void setParent()
    {
      mParent.setShowStations( mCBstation.isChecked(), mCBname.isChecked() );
      mParent.setShowSplay( mCBsplay.isChecked(), mCBendpoint.isChecked() );
      mParent.setShowReference( mCBref.isChecked(), mCBaxes.isChecked() );
      /* if ( mParent.hasWalls() ) */ mParent.setShowWalls( mCBwall.isChecked() );
      /* if ( mParent.hasSurface() ) */ mParent.setShowSurface( mCBsurface.isChecked() );
      if ( mParent.hasPhotos() ) mParent.setShowPhotos( mCBphoto.isChecked() );
    }
}
