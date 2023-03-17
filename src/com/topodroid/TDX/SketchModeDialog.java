/* @file SketchModeDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketching: display mode dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.*;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

class SketchModeDialog extends MyDialog
                        implements View.OnClickListener
{
    // private CheckBox mCBleg;      // whether to show legs
    private CheckBox mCBsplay;    // whether to show splays
    private CheckBox mCBstation;  // whether to show stations
    private CheckBox mCBgrid;     // whether to show the grid
    private CheckBox mCBsections;
    private CheckBox mCBwalls;    // whether to show the walls
    private CheckBox mCBscaleRef; // whether to show the reference triad

    private final SketchSurface mSurface;
    private final SketchWindow mParent; // used only to decide whether display checkbox "Shift"

    SketchModeDialog( Context context, SketchWindow parent, SketchSurface surface )
    {
      super(context, null, R.string.SketchModeDialog ); // null app
      mParent  = parent;
      mSurface = surface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);

      initLayout( R.layout.sketch_mode_dialog, R.string.title_refs );

      // mCBleg     = (CheckBox) findViewById(R.id.cb_mode_leg);
      mCBsplay    = (CheckBox) findViewById(R.id.cb_mode_splay);
      mCBstation  = (CheckBox) findViewById(R.id.cb_mode_station);
      mCBgrid     = (CheckBox) findViewById(R.id.cb_mode_grid);
      mCBsections = (CheckBox) findViewById(R.id.cb_mode_sections);
      mCBwalls    = (CheckBox) findViewById(R.id.cb_mode_walls);
      mCBscaleRef = (CheckBox) findViewById(R.id.cb_mode_ref);

      ((Button) findViewById(R.id.button_ok)).setOnClickListener( this );
      ((Button) findViewById(R.id.button_back)).setOnClickListener( this );

      int mode = mSurface.getDisplayMode();
      // mCBleg.setChecked(   (mode & DisplayMode.DISPLAY_LEG) != 0 );
      mCBsplay.setChecked(    (mode & DisplayMode.DISPLAY_SPLAY) != 0 );
      mCBstation.setChecked(  (mode & DisplayMode.DISPLAY_STATION) != 0 );
      mCBgrid.setChecked(     (mode & DisplayMode.DISPLAY_GRID) != 0 );
      mCBsections.setChecked( (mode & DisplayMode.DISPLAY_OUTLINE) != 0 );
      mCBwalls.setChecked(    (mode & DisplayMode.DISPLAY_WALLS) != 0 );
      mCBscaleRef.setChecked( (mode & DisplayMode.DISPLAY_SCALEBAR) != 0);
    }

    @Override
    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "SketchModeDialog onClick " + view.toString() );
      int vid = view.getId();
      if ( vid == R.id.button_ok ) {
        int mode = DisplayMode.DISPLAY_NONE;
        // if ( mCBleg.isChecked() )   mode |= DisplayMode.DISPLAY_LEG;
        if ( mCBsplay.isChecked() )    mode |= DisplayMode.DISPLAY_SPLAY;
        if ( mCBstation.isChecked() )  mode |= DisplayMode.DISPLAY_STATION;
        if ( mCBgrid.isChecked() )     mode |= DisplayMode.DISPLAY_GRID;
        if ( mCBsections.isChecked() ) mode |= DisplayMode.DISPLAY_OUTLINE;
        if ( mCBwalls.isChecked() )    mode |= DisplayMode.DISPLAY_WALLS;
        if ( mCBscaleRef.isChecked() ) mode |= DisplayMode.DISPLAY_SCALEBAR;
        // TDLog.Error( "Mode " + mode );
        mSurface.setDisplayMode( mode );
      } else if ( vid == R.id.button_back ) {
      //   /* nothing */
      // } else if ( vid == R.id.button_mode_cancel ) {
      //   /* nothing */
      }
      dismiss();
    }

}
        



