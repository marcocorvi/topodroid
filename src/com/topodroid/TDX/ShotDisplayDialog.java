/* @file ShotDisplayDialog.java
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

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


class ShotDisplayDialog extends MyDialog
                               implements View.OnClickListener
{
    private CheckBox mCBids;      // whether to hide ids
    private CheckBox mCBsplay;    // whether to hide splays
    private CheckBox mCBlatest;   // whether to show latest
    private CheckBox mCBblank;    // whether to hide blank
    private CheckBox mCBleg;      // whether to hide repeated leg 
    // private Button mBtnRefresh;

    private final ShotWindow mParent;

    ShotDisplayDialog( Context context, ShotWindow parent )
    {
      super( context, null, R.string.ShotDisplayDialog ); // null app
      mParent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initLayout( R.layout.shot_display_dialog, R.string.title_mode );

        mCBids    = (CheckBox) findViewById(R.id.cb_mode_ids);
        mCBsplay  = (CheckBox) findViewById(R.id.cb_mode_splay);
        mCBlatest = (CheckBox) findViewById(R.id.cb_mode_latest);
        mCBblank  = (CheckBox) findViewById(R.id.cb_mode_blank);
        mCBleg    = (CheckBox) findViewById(R.id.cb_mode_leg);

        ((Button) findViewById(R.id.btn_ok)).setOnClickListener( this );
        ((Button) findViewById(R.id.btn_cancel)).setOnClickListener( this );
        // mBtnRefresh = (Button) findViewById(R.id.button_mode_refresh);
        // mBtnRefresh.setOnClickListener( this );

        mCBids.setChecked(     mParent.isShowIds() );
        mCBsplay.setChecked( ! mParent.isFlagSplay() );
	if ( TDSetting.mShotRecent ) {
          mCBlatest.setChecked(  mParent.isFlagLatest() );
	} else {
	  mCBlatest.setVisibility( View.GONE );
	}
        mCBblank.setChecked( ! mParent.isFlagBlank() );
        mCBleg.setChecked(   ! mParent.isFlagLeg() );

    }

    @Override
    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "ShotDisplayDialog onClick " + view.toString() );
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

    // @Override
    // public void onBackPressed ()
    // {
    //   setParent();
    //   cancel();
    // }

    private void setParent()
    {
      // mParent.setShowIds( mCBids.isChecked() );
      // mParent.mFlagSplay = ! mCBsplay.isChecked();
      // if ( TDSetting.mShotRecent ) mParent.mFlagLatest =  mCBlatest.isChecked();
      // mParent.mFlagBlank = ! mCBblank.isChecked();
      // mParent.mFlagLeg   = ! mCBleg.isChecked();
      mParent.setFlags( mCBids.isChecked(),
                        ! mCBsplay.isChecked(),
                        mCBlatest.isChecked(),
                        ! mCBleg.isChecked(),
                        ! mCBblank.isChecked() );
      // mParent.updateDisplay( );
    }
}
