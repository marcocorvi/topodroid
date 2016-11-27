/* @file ShotDisplayDialog.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief TopoDroid shot-list: display mode dialog
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


public class ShotDisplayDialog extends MyDialog 
                               implements View.OnClickListener
{
    private CheckBox mCBids;      // whether to hide ids
    private CheckBox mCBsplay;    // whether to hide splays
    private CheckBox mCBblank;    // whether to hide blank
    private CheckBox mCBleg;      // whether to hide repeated leg 
    // private Button mBtnRefresh;

    private ShotWindow mParent;

    public ShotDisplayDialog( Context context, ShotWindow parent )
    {
      super( context, R.string.ShotDisplayDialog );
      mParent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initLayout( R.layout.shot_display_dialog, R.string.title_mode );

        mCBids   = (CheckBox) findViewById(R.id.cb_mode_ids);
        mCBsplay = (CheckBox) findViewById(R.id.cb_mode_splay);
        mCBblank = (CheckBox) findViewById(R.id.cb_mode_blank);
        mCBleg   = (CheckBox) findViewById(R.id.cb_mode_leg);

        ((Button) findViewById(R.id.button_ok)).setOnClickListener( this );
        ((Button) findViewById(R.id.button_back)).setOnClickListener( this );
        // mBtnRefresh = (Button) findViewById(R.id.button_mode_refresh);
        // mBtnRefresh.setOnClickListener( this );

        mCBids.setChecked(     mParent.getShowIds() );
        mCBsplay.setChecked( ! mParent.mSplay );
        mCBblank.setChecked( ! mParent.mBlank );
        mCBleg.setChecked(   ! mParent.mLeg );

    }

    public void onClick(View view)
    {
      // TDLog.Log( TDLog.LOG_INPUT, "ShotDisplayDialog onClick " + view.toString() );
      hide();
      switch (view.getId()) {
        case R.id.button_ok:
          setParent();
          break;
        case R.id.button_back:
          /* nothing */
          break;
        // case R.id.button_mode_refresh:
        //   mParent.updateDisplay( );
        //   break;
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
      mParent.setShowIds( mCBids.isChecked() );
      mParent.mSplay = ! mCBsplay.isChecked();
      mParent.mBlank = ! mCBblank.isChecked();
      mParent.mLeg   = ! mCBleg.isChecked();
      mParent.updateDisplay( );
    }
}
