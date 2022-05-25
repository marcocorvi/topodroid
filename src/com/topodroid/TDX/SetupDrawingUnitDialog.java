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
package com.topodroid.TDX;

// import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.ItemButton;
import com.topodroid.prefs.TDSetting;

import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;

// import android.text.TextWatcher;
// import android.text.Editable;

import android.graphics.Path;
import android.graphics.Paint;

import java.util.Locale;

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
  private boolean mETskip = false;

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
    mPaint  = SymbolPoint.makePaint( 0xffffffff, Paint.Style.STROKE );
    // mPath   = BrushManager.mPointLib.getPointPath( 0 );
    float unit = TDSetting.mUnitIcons;
    mPath   = new Path();
    mPath.moveTo( 0*unit,  3*unit );
    mPath.lineTo( 0*unit, -6*unit );
    mPath.lineTo(-3*unit, -6*unit );
    mPath.lineTo( 3*unit, -6*unit );
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
    setETsize( mSize );

    mETsize.setKeyListener( null );
    // mETsize.setBackgroundColor( TDColor.MID_GRAY );

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
          float size = Float.parseFloat( mETsize.getText().toString() );
          setSize( size, false );
        } catch ( NumberFormatException e ) { 
          setETsize( mSize );
        }
      }
    } );
    */

    mBtnNext   = (Button) findViewById( R.id.btn_next );
    mBtnSkip   = (Button) findViewById( R.id.btn_skip );
    mBtnPlus   = (Button) findViewById( R.id.btn_plus );
    mBtnMinus  = (Button) findViewById( R.id.btn_minus );
    mBtnNext.setOnClickListener( this );
    mBtnSkip.setOnClickListener( this );
    mBtnPlus.setOnClickListener( this );
    mBtnMinus.setOnClickListener( this );
  }

  /** update the EditText text - disable TextChange listening temporary
   * @param sz         new size
   */
  private void setETsize( float sz )
  {
    mETskip = true;
    mETsize.setText( String.format(Locale.US, "%.2f", sz ) );
    mETskip = false;
  }

  /** set the size - do some bounds checks: size must be in [0.1, 14.0]
   * @param sz         new size
   * @param edit_text  whether to update EditText
   */
  private void setSize( float sz, boolean edit_text )
  {
    mSize = sz;
    if ( mSize < 0.1f ) { mSize = 0.1f; edit_text = true; }
    else if ( mSize >= 14 ) { mSize = 14; edit_text = true; }
    if ( edit_text ) setETsize( mSize );
    mSample.resetPaintPath( mPaint, mPath, mSize*1.4f, mSize*1.4f );
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
       // mParent.getApp().setDrawingUnitIcons( mSize );
       // mParent.getApp().setDrawingUnitLines( mSize ); // FIXME_LINE_UNITS dialog
       TopoDroidApp.setDrawingUnitIcons( mSize );
       TopoDroidApp.setDrawingUnitLines( mSize ); // FIXME_LINE_UNITS dialog
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
