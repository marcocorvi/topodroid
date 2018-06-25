/* @file SetupDrawingUnitDialog.java
 *
 * @author marco corvi
 * @date jun 2018
 *
 * @brief TopoDroid setup: drawing unit
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Dialog;
import android.content.Context;
// import android.content.res.Resources;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.ImageView;
// import android.widget.Toast;

import android.text.TextWatcher;
import android.text.Editable;

import android.graphics.Path;
import android.graphics.Paint;

import java.util.Locale;

// import android.util.Log;

/**
 */
class SetupDrawingUnitDialog extends MyDialog
                             implements View.OnClickListener
{
  private final MainWindow mParent;
  private int   mSetup; // my setup index

  private Button  mBtnNext;
  private Button  mBtnSkip;
  private Button  mBtnMinus;
  private Button  mBtnPlus;

  private EditText mETsize;
  private boolean mETsizeChanged = false;

  private float  mSize;

  private ItemButton mSample;
  private LinearLayout mLayout;
  private Paint mPaint;
  private Path  mPath;


  SetupDrawingUnitDialog( Context context, MainWindow parent, int setup, float size )
  {
    super( context, R.string.SetupDrawingUnitDialog ); 
    mParent = parent;
    mSetup  = setup;
    mSize   = size;
    mPaint  = BrushManager.fixedShotPaint; // FIXME
    mPath   = BrushManager.mPointLib.getPointPath( 0 );
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

    initLayout( R.layout.setup_drawingunit_dialog, R.string.setup_drawingunit_title );
    mSample = (ItemButton) findViewById(R.id.sample);
    mSample.resetPaintPath( mPaint, mPath, mSize*1.4f, mSize*1.4f );

    mLayout = (LinearLayout) findViewById(R.id.layout2);
    mETsize = (EditText) findViewById( R.id.textsize );
    mETsize.setText( String.format(Locale.US, "%.2f", mSize ) );

    mETsize.addTextChangedListener( new TextWatcher() {
      @Override
      public void afterTextChanged( Editable e ) { }

      @Override
      public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

      @Override
      public void onTextChanged( CharSequence cs, int start, int before, int cnt ) 
      {
        try {
          float size = Float.parseFloat( mETsize.getText().toString() );
          setSize( size, false );
        } catch ( NumberFormatException e ) { }
      }
    } );

    mBtnNext   = (Button) findViewById( R.id.btn_next );
    mBtnSkip   = (Button) findViewById( R.id.btn_skip );
    mBtnPlus   = (Button) findViewById( R.id.btn_plus );
    mBtnMinus  = (Button) findViewById( R.id.btn_minus );
    mBtnNext.setOnClickListener( this );
    mBtnSkip.setOnClickListener( this );
    mBtnPlus.setOnClickListener( this );
    mBtnMinus.setOnClickListener( this );
  }

  private void setSize( float sz, boolean edit_text )
  {
    mSize = sz;
    if ( mSize < 0.1f ) { mSize = 0.1f; edit_text = true; }
    else if ( mSize >= 14 ) { mSize = 14; edit_text = true; }
    mETsizeChanged = ! edit_text;
    if ( edit_text ) mETsize.setText( String.format(Locale.US, "%.2f", mSize ) );
    mSample.resetPaintPath( mPaint, mPath, sz*1.4f, sz*1.4f );
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
       mParent.getApp().setDrawingUnit( mSize );
       mParent.doNextSetup( mSetup + 1 );
       dismiss();
     } else if ( b == mBtnPlus ) {
       setSize( mSize + 0.2f, true );
     } else if ( b == mBtnMinus ) {
       setSize( mSize - 0.2f, true );
     }
   }

   @Override
   public void onBackPressed()
   {
     mParent.doNextSetup( mSetup + 1 );
     dismiss();
   }

}
