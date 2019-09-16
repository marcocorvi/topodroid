/* @file DrawingModeDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: display mode dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

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

class DrawingModeDialog extends MyDialog
                        implements View.OnClickListener
{
    private CheckBox mCBleg;      // whether to show legs
    private CheckBox mCBsplay;    // whether to show splays
    private CheckBox mCBlatest;   // whether to show latest shots
    private CheckBox mCBstation;  // whether to show stations
    private CheckBox mCBgrid;     // whether to show the grid
    private CheckBox mCBfixed;    // whether to show the grid
    private CheckBox mCBscrap;

    private CheckBox mCBfloor = null;
    private CheckBox mCBfill  = null;
    private CheckBox mCBceil  = null;
    private CheckBox mCBarti  = null;
    // private CheckBox mCBform  = null;
    // private CheckBox mCBwater = null;
    // private CheckBox mCBtext  = null;

    private CheckBox mCBscaleRef; // whether to show the scale reference bar

    // private Button mBtnOK;
    // private Button mBtnBack;

    private final DrawingSurface mSurface;
    private final DrawingWindow mParent; // used only to decide whether display checkbox "Shift"

    DrawingModeDialog( Context context, DrawingWindow parent, DrawingSurface surface )
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
      mCBlatest  = (CheckBox) findViewById(R.id.cb_mode_latest);
      mCBstation = (CheckBox) findViewById(R.id.cb_mode_station);
      mCBgrid    = (CheckBox) findViewById(R.id.cb_mode_grid);
      mCBfixed   = (CheckBox) findViewById(R.id.cb_mode_fixed);

      if ( TDSetting.mWithLevels > 0 ) {
        mCBfloor = (CheckBox) findViewById(R.id.cb_layer_floor);
        mCBfill  = (CheckBox) findViewById(R.id.cb_layer_fill);
        mCBceil  = (CheckBox) findViewById(R.id.cb_layer_ceil);
        mCBarti  = (CheckBox) findViewById(R.id.cb_layer_arti);
        // mCBform  = (CheckBox) findViewById(R.id.cb_layer_form);
        // mCBwater = (CheckBox) findViewById(R.id.cb_layer_water);
        // mCBtext  = (CheckBox) findViewById(R.id.cb_layer_text);
 
        int layers = DrawingLevel.getDisplayLevel();
        mCBfloor.setChecked( ( layers & DrawingLevel.LEVEL_FLOOR ) == DrawingLevel.LEVEL_FLOOR );
        mCBfill .setChecked( ( layers & DrawingLevel.LEVEL_FILL  ) == DrawingLevel.LEVEL_FILL  );
        mCBceil .setChecked( ( layers & DrawingLevel.LEVEL_CEIL  ) == DrawingLevel.LEVEL_CEIL  );
        mCBarti .setChecked( ( layers & DrawingLevel.LEVEL_ARTI  ) == DrawingLevel.LEVEL_ARTI  );
        // mCBform .setChecked( ( layers & DrawingLevel.LEVEL_FORM  ) == DrawingLevel.LEVEL_FORM  );
        // mCBwater.setChecked( ( layers & DrawingLevel.LEVEL_WATER ) == DrawingLevel.LEVEL_WATER );
        // mCBtext .setChecked( ( layers & DrawingLevel.LEVEL_TEXT  ) == DrawingLevel.LEVEL_TEXT  );

      } else {
        LinearLayout ll = (LinearLayout) findViewById( R.id.layer_layout );
        ll.setVisibility( View.GONE );
      }

      mCBscaleRef = (CheckBox) findViewById(R.id.cb_mode_scale_ref);
      mCBscrap = (CheckBox) findViewById(R.id.cb_scrap);

      ((Button) findViewById(R.id.button_ok)).setOnClickListener( this );
      ((Button) findViewById(R.id.button_back)).setOnClickListener( this );

      int mode = mSurface.getDisplayMode();
      if ( mParent != null && mParent.isAnySection() ) {
        mCBsplay.setVisibility( View.GONE );
        mCBlatest.setVisibility( View.GONE );
        mCBfixed.setVisibility( View.GONE );
        mCBscrap.setVisibility( View.GONE );
      } else {
        mCBsplay.setChecked(   (mode & DisplayMode.DISPLAY_SPLAY) != 0 );
	if ( TDSetting.mShotRecent ) {
          mCBlatest.setChecked(  (mode & DisplayMode.DISPLAY_LATEST) != 0 );
	} else {
          mCBlatest.setVisibility( View.GONE );
	}
        if ( mParent != null && TDLevel.overAdvanced && TDSetting.mPlotShift ) {
          mCBfixed.setChecked( mParent.mShiftDrawing );
        } else {
          mCBfixed.setVisibility( View.GONE );
        }
        if ( ! TDLevel.overNormal ) mCBscrap.setVisibility( View.GONE );
      }
      mCBleg.setChecked(     (mode & DisplayMode.DISPLAY_LEG) != 0 );
      mCBstation.setChecked( (mode & DisplayMode.DISPLAY_STATION) != 0 );
      mCBgrid.setChecked(    (mode & DisplayMode.DISPLAY_GRID) != 0 );
      mCBscaleRef.setChecked((mode & DisplayMode.DISPLAY_SCALEBAR) != 0);
    }

    // called only if mWithLevels > 0
    private void setLevels()
    {
      int layers = DrawingLevel.LEVEL_BASE;
      if (  mCBfloor.isChecked( ) ) layers |= DrawingLevel.LEVEL_FLOOR;
      if (  mCBfill .isChecked( ) ) layers |= DrawingLevel.LEVEL_FILL;
      if (  mCBceil .isChecked( ) ) layers |= DrawingLevel.LEVEL_CEIL;
      if (  mCBarti .isChecked( ) ) layers |= DrawingLevel.LEVEL_ARTI;
      // if (  mCBform .isChecked( ) ) layers |= DrawingLevel.LEVEL_FORM;
      // if (  mCBwater.isChecked( ) ) layers |= DrawingLevel.LEVEL_WATER;
      // if (  mCBtext .isChecked( ) ) layers |= DrawingLevel.LEVEL_TEXT;
      // Log.v("DistoXL", "set levels " + layers );
      DrawingLevel.setDisplayLevel( layers );
    }

    @Override
    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "DrawingModeDialog onClick " + view.toString() );
      switch (view.getId()){
        case R.id.button_ok:
          int mode = DisplayMode.DISPLAY_NONE;
          if ( mParent != null && ! mParent.isAnySection() ) {
            if ( mCBsplay.isChecked() )   mode |= DisplayMode.DISPLAY_SPLAY;
            if ( TDSetting.mShotRecent && mCBlatest.isChecked() )  mode |= DisplayMode.DISPLAY_LATEST;
            if ( /* mParent != null && */ TDLevel.overAdvanced && TDSetting.mPlotShift ) {
              mParent.mShiftDrawing = mCBfixed.isChecked();
            }
          }
          if ( mCBleg.isChecked() )     mode |= DisplayMode.DISPLAY_LEG;
          if ( mCBstation.isChecked() ) mode |= DisplayMode.DISPLAY_STATION;
          if ( mCBgrid.isChecked() )    mode |= DisplayMode.DISPLAY_GRID;
          if ( mCBscaleRef.isChecked() )mode |= DisplayMode.DISPLAY_SCALEBAR;

          // TDLog.Error( "Mode " + mode );
          mSurface.setDisplayMode( mode );
          if ( TDLevel.overNormal && mCBscrap.isChecked() && mParent != null ) {
            mParent.scrapOutlineDialog();
          }

          if ( TDSetting.mWithLevels > 0 ) setLevels();

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
    //   if ( mCBlatest.isChecked() )  mode |= DisplayMode.DISPLAY_LATEST;
    //   if ( mCBstation.isChecked() ) mode |= DisplayMode.DISPLAY_STATION;
    //   if ( mCBgrid.isChecked() )    mode |= DisplayMode.DISPLAY_GRID;
    //   mSurface.setDisplayMode( mode );
    //   cancel();
    // }
}
        



