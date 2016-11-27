/* @file DrawingModeDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: display mode dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.*;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


public class DrawingModeDialog extends MyDialog 
                               implements View.OnClickListener
{
    private CheckBox mCBleg;      // whether to show legs
    private CheckBox mCBsplay;    // whether to show splays
    private CheckBox mCBstation;  // whether to show stations
    private CheckBox mCBgrid;     // whether to show the grid
    private CheckBox mCBfixed;    // whether to show the grid
    // private Button mBtnOK;
    // private Button mBtnBack;

    private DrawingSurface mSurface;
    private DrawingWindow mParent; // used only to decide whether display checkbox "Shift"

    public DrawingModeDialog( Context context, DrawingWindow parent, DrawingSurface surface )
    {
      super(context, R.string.DrawingModeDialog );
      mParent  = parent;
      mSurface = surface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);

      initLayout( R.layout.drawing_mode_dialog, R.string.title_refs );

      mCBleg     = (CheckBox) findViewById(R.id.cb_mode_leg);
      mCBsplay   = (CheckBox) findViewById(R.id.cb_mode_splay);
      mCBstation = (CheckBox) findViewById(R.id.cb_mode_station);
      mCBgrid    = (CheckBox) findViewById(R.id.cb_mode_grid);
      mCBfixed   = (CheckBox) findViewById(R.id.cb_mode_fixed);

      ((Button) findViewById(R.id.button_ok)).setOnClickListener( this );
      ((Button) findViewById(R.id.button_back)).setOnClickListener( this );

      int mode = mSurface.getDisplayMode();
      if ( mParent != null && mParent.isAnySection() ) {
        mCBsplay.setVisibility( View.GONE );
      } else {
        mCBsplay.setChecked(   (mode & DisplayMode.DISPLAY_SPLAY) != 0 );
        if ( mParent != null && TDSetting.mLevelOverNormal ) {
          mCBfixed.setChecked( mParent.mShiftDrawing );
        } else {
          mCBfixed.setVisibility( View.GONE );
        }
      }
      mCBleg.setChecked(     (mode & DisplayMode.DISPLAY_LEG) != 0 );
      mCBstation.setChecked( (mode & DisplayMode.DISPLAY_STATION) != 0 );
      mCBgrid.setChecked(    (mode & DisplayMode.DISPLAY_GRID) != 0 );
    }

    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "DrawingModeDialog onClick " + view.toString() );
      switch (view.getId()){
        case R.id.button_ok:
          int mode = DisplayMode.DISPLAY_NONE;
          if ( mParent != null && ! mParent.isAnySection() ) {
            if ( mCBsplay.isChecked() )   mode |= DisplayMode.DISPLAY_SPLAY;
            if ( mParent != null && TDSetting.mLevelOverNormal ) {
              mParent.mShiftDrawing = mCBfixed.isChecked();
            }
          }
          if ( mCBleg.isChecked() )     mode |= DisplayMode.DISPLAY_LEG;
          if ( mCBstation.isChecked() ) mode |= DisplayMode.DISPLAY_STATION;
          if ( mCBgrid.isChecked() )    mode |= DisplayMode.DISPLAY_GRID;
          // TDLog.Error( "Mode " + mode );
          mSurface.setDisplayMode( mode );
          break;
        case R.id.button_back:
          /* nothing */
          break;
        // case R.id.button_mode_cancel:
        //   break;
      }
      dismiss();
    }

    // @Override
    // public void onBackPressed ()
    // {
    //   int mode = DisplayMode.DISPLAY_NONE;
    //   if ( mCBleg.isChecked() )     mode |= DisplayMode.DISPLAY_LEG;
    //   if ( mCBsplay.isChecked() )   mode |= DisplayMode.DISPLAY_SPLAY;
    //   if ( mCBstation.isChecked() ) mode |= DisplayMode.DISPLAY_STATION;
    //   if ( mCBgrid.isChecked() )    mode |= DisplayMode.DISPLAY_GRID;
    //   mSurface.setDisplayMode( mode );
    //   cancel();
    // }
}
        



