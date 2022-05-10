/* @file SetupButtonSizeDialog.java
 *
 * @author marco corvi
 * @date jun 2018
 *
 * @brief TopoDroid setup button size 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.content.Context;
// import android.content.res.Resources;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
// import java.util.Locale;

/**
 */
class SetupButtonSizeDialog extends MyDialog
                            implements View.OnClickListener
{
  private final MainWindow mParent;
  private int   mSetup; // my setup index

  private Button  mBtnNext;
  private Button  mBtnSkip;
  private RadioButton  mRBxs;
  private RadioButton  mRBs;
  private RadioButton  mRBm;
  private RadioButton  mRBl;
  private RadioButton  mRBxl;

  private int  mSize;

  private ImageView mSample;
  private LinearLayout mLayout;


  SetupButtonSizeDialog( Context context, MainWindow parent, int setup, int size )
  {
    super( context, R.string.SetupButtonSizeDialog ); 
    mParent = parent;
    mSetup  = setup;
    mSize   = size;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    // int width = mContext.getResources().getDisplayMetrics().widthPixels;

    initLayout( R.layout.setup_buttonsize_dialog, R.string.setup_buttonsize_title );
    mSample = (ImageView) findViewById(R.id.sample);
    mLayout = (LinearLayout) findViewById(R.id.layout2);

    mBtnNext   = (Button) findViewById( R.id.btn_next );
    mBtnSkip   = (Button) findViewById( R.id.btn_skip );
    mBtnNext.setOnClickListener( this );
    mBtnSkip.setOnClickListener( this );

    mRBxs = (RadioButton) findViewById( R.id.btn_xs );
    mRBs  = (RadioButton) findViewById( R.id.btn_s  );
    mRBm  = (RadioButton) findViewById( R.id.btn_m  );
    mRBl  = (RadioButton) findViewById( R.id.btn_l  );
    mRBxl = (RadioButton) findViewById( R.id.btn_xl );

    mRBxs.setOnClickListener( this );
    mRBs.setOnClickListener( this );
    mRBm.setOnClickListener( this );
    mRBl.setOnClickListener( this );
    mRBxl.setOnClickListener( this );

    // float displayWidth = TopoDroidApp.mDisplayWidth;
    // float displayHeight = TopoDroidApp.mDisplayHeight;
    
    switch ( mSize ) {
      case TDSetting.BTN_SIZE_SMALL:  mRBxs.setChecked( true ); setSize(0); break;
      case TDSetting.BTN_SIZE_NORMAL: mRBs.setChecked( true );  setSize(1); break;
      case TDSetting.BTN_SIZE_MEDIUM: mRBm.setChecked( true );  setSize(3); break;
      case TDSetting.BTN_SIZE_LARGE:  mRBl.setChecked( true );  setSize(4); break;
      case TDSetting.BTN_SIZE_HUGE:   mRBxl.setChecked( true ); setSize(2); break;
    }
  }

  private void setSize( int sz )
  {
    mSize = sz;
    int bs = TDSetting.getSizeButtons( sz );
    mSample.setImageDrawable( MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_topodroid, bs ) );
    mLayout.invalidate();
  }

// ----------------------------------------------------------------------------

   @Override
   public void onClick(View view)
   {
     Button b = (Button)view;
     if ( b == mBtnSkip ) {
       mParent.doNextSetup( -1 );
       dismiss();
     } else if ( b == mBtnNext ) {
       // mParent.getApp().setButtonSize( mSize );
       TopoDroidApp.setButtonSize( mSize );
       mParent.resetButtonBar();
       mParent.doNextSetup( mSetup + 1 );
       dismiss();
     } else if ( b == mRBxs ) {
       setSize( 0 );
     } else if ( b == mRBs  ) {
       setSize( 1 );
     } else if ( b == mRBm  ) {
       setSize( 3 );
     } else if ( b == mRBl  ) {
       setSize( 4 );
     } else if ( b == mRBxl ) {
       setSize( 2 );
     }
   }

   @Override
   public void onBackPressed()
   {
     mParent.doNextSetup( mSetup + 1 );
     dismiss();
   }

}
