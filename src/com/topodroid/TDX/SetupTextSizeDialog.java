/* @file SetupTextSizeDialog.java
 *
 * @author marco corvi
 * @date jun 2018
 *
 * @brief TopoDroid setup text size
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.content.Context;
// import android.content.res.Resources;

import android.os.Bundle;
import android.view.View;
// import android.widget.LinearLayout;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.SeekBar;
// import android.widget.SeekBar.OnSeekBarChangeListener;
// import android.text.TextWatcher;
// import android.text.Editable;

import java.util.Locale;

class SetupTextSizeDialog extends MyDialog
                          implements View.OnClickListener
{
  private final MainWindow mParent;
  private int   mSetup; // my setup index

  // private SeekBar mSeekBar;
  private Button  mBtnNext;
  private Button  mBtnSkip;
  private Button  mBtnPlus;
  private Button  mBtnMinus;
  private EditText mETsize;
  private TextView mSample;

  private int  mSize;
  private boolean mETskip = false;

  SetupTextSizeDialog( Context context, MainWindow parent, int setup, int size )
  {
    super( context, R.string.SetupTextSizeDialog ); 
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

    initLayout( R.layout.setup_textsize_dialog, R.string.setup_textsize_title );
    // setContentView( R.layout.setup_textsize_dialog );

    // mSeekBar   = (SeekBar) findViewById(R.id.seekbar );
    mSample    = (TextView) findViewById(R.id.sample_text);
    mETsize    = (EditText) findViewById( R.id.textsize );
    mBtnPlus   = (Button) findViewById( R.id.btn_plus );
    mBtnMinus  = (Button) findViewById( R.id.btn_minus );
    mBtnNext   = (Button) findViewById( R.id.btn_next );
    mBtnSkip   = (Button) findViewById( R.id.btn_skip );
    mBtnNext.setOnClickListener( this );
    mBtnSkip.setOnClickListener( this );
    mBtnPlus.setOnClickListener( this );
    mBtnMinus.setOnClickListener( this );

    mETsize.setKeyListener( null );
    // mETsize.setBackgroundColor( TDColor.MID_GRAY );

    // mSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
    //   public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
    //     int s = (int)( progress );
    //     if ( s < TDSetting.MIN_SIZE_TEXT ) s = TDSetting.MIN_SIZE_TEXT;
    //     else if ( s > 128 ) s = 128;
    //     if ( s != mSize ) {
    //       setSize( s );
    //     }
    //   }
    //   public void onStartTrackingTouch(SeekBar seekbar) { }
    //   public void onStopTrackingTouch(SeekBar seekbar) { }
    // } );

    /*
    mETsize.addTextChangedListener( new TextWatcher() {
      @Override
      public void afterTextChanged( Editable e ) { }

      @Override
      public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

      @Override
      public void onTextChanged( CharSequence cs, int start, int before, int cnt ) 
      {
        if ( mETskip ) return;
        try {
          int size = Integer.parseInt( mETsize.getText().toString() );
          setSize( size, false );
        } catch ( NumberFormatException e ) { 
          setSize( mSize, false );
        }
      }
    } );
    */

    // mSeekBar.setProgress( mSize );
    setEditSize();
    mSample.setTextSize( TDSetting.getTextSize( mSize ) );
  }

  private void setSize( int sz, boolean edit_text )
  {
    mSize = sz;
    if ( mSize < 10 ) { mSize = 10; edit_text = true; }
    else if ( mSize > 128 ) { mSize = 128; edit_text = true; }
    // mSeekBar.setProgress( mSize );
    if ( edit_text ) setEditSize();
    mSample.setTextSize( TDSetting.getTextSize( mSize ) );
  }

  private void setEditSize()
  { 
    mETskip = true;
    mETsize.setText( String.format(Locale.US, "%d", mSize ) );
    mETskip = false;
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
       mParent.getApp().setTextSize( mSize );
       mParent.updateDisplay();
       mParent.doNextSetup( mSetup + 1 );
       dismiss();
     } else if ( b == mBtnPlus ) {
       setSize( mSize + 1, true );
     } else if ( b == mBtnMinus ) {
       setSize( mSize - 1, true );
     }
   }

   @Override
   public void onBackPressed()
   {
     mParent.doNextSetup( mSetup + 1 );
     dismiss();
   }

}
