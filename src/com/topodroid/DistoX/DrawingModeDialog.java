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
 * CHANGES
 *
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


public class DrawingModeDialog extends Dialog 
                               implements View.OnClickListener
{
    private CheckBox mCBleg;      // whether to show legs
    private CheckBox mCBsplay;    // whether to show splays
    private CheckBox mCBstation;  // whether to show stations
    private CheckBox mCBgrid;     // whether to show the grid
    private CheckBox mCBfixed;    // whether to show the grid
    private Button mBtnOK;
    // private Button mBtnBack;

    private DrawingSurface mSurface;
    private DrawingActivity mParent;

    public DrawingModeDialog( Context context, DrawingActivity parent, DrawingSurface surface )
    {
      super(context);
      mParent  = parent;
      mSurface = surface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_mode_dialog);

        mCBleg     = (CheckBox) findViewById(R.id.cb_mode_leg);
        mCBsplay   = (CheckBox) findViewById(R.id.cb_mode_splay);
        mCBstation = (CheckBox) findViewById(R.id.cb_mode_station);
        mCBgrid    = (CheckBox) findViewById(R.id.cb_mode_grid);
        mCBfixed   = (CheckBox) findViewById(R.id.cb_mode_fixed);

        mBtnOK   = (Button) findViewById(R.id.button_ok);
        mBtnOK.setOnClickListener( this );
        // mBtnBack = (Button) findViewById(R.id.button_cancel);
        // mBtnBack.setOnClickListener( this );

        int mode = mSurface.getDisplayMode();
        mCBleg.setChecked(     (mode & DrawingCommandManager.DISPLAY_LEG) != 0 );
        mCBsplay.setChecked(   (mode & DrawingCommandManager.DISPLAY_SPLAY) != 0 );
        mCBstation.setChecked( (mode & DrawingCommandManager.DISPLAY_STATION) != 0 );
        mCBgrid.setChecked(    (mode & DrawingCommandManager.DISPLAY_GRID) != 0 );

        mCBfixed.setChecked( mParent.mShiftDrawing );
        if ( ! TopoDroidSetting.mLevelOverNormal ) mCBfixed.setVisibility( View.GONE );

        setTitle( R.string.title_refs );
    }

    public void onClick(View view)
    {
      // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingModeDialog onClick " + view.toString() );
      switch (view.getId()){
        case R.id.button_ok:
          int mode = DrawingCommandManager.DISPLAY_NONE;
          if ( mCBleg.isChecked() ) mode |= DrawingCommandManager.DISPLAY_LEG;
          if ( mCBsplay.isChecked() ) mode |= DrawingCommandManager.DISPLAY_SPLAY;
          if ( mCBstation.isChecked() ) mode |= DrawingCommandManager.DISPLAY_STATION;
          if ( mCBgrid.isChecked() ) mode |= DrawingCommandManager.DISPLAY_GRID;
          // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Mode " + mode );
          if ( TopoDroidSetting.mLevelOverNormal ) {
            mParent.mShiftDrawing = mCBfixed.isChecked();
          }
          mSurface.setDisplayMode( mode );
          break;
        // case R.id.button_mode_cancel:
        //   break;
      }
      dismiss();
    }

    // @Override
    // public void onBackPressed ()
    // {
    //   int mode = DrawingCommandManager.DISPLAY_NONE;
    //   if ( mCBleg.isChecked() ) mode |= DrawingCommandManager.DISPLAY_LEG;
    //   if ( mCBsplay.isChecked() ) mode |= DrawingCommandManager.DISPLAY_SPLAY;
    //   if ( mCBstation.isChecked() ) mode |= DrawingCommandManager.DISPLAY_STATION;
    //   if ( mCBgrid.isChecked() ) mode |= DrawingCommandManager.DISPLAY_GRID;
    //   mSurface.setDisplayMode( mode );
    //   cancel();
    // }
}
        



