/* @file DialogFractal.java
 *
 * @author marco corvi
 * @date mar 2018
 *
 * @brief Cave3D fractal counts dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import android.os.Bundle;
// import android.app.Activity;
import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.RadioButton;

import android.graphics.Bitmap;

import java.util.Locale;

class DialogFractal extends MyDialog 
                    implements View.OnClickListener
{
  private FractalResult mResult;

  private CheckBox mCBsplays;
  private ImageView mImage;
  // private Button mBtnClose;
  private EditText mCell;
  private static double mCellSide = 2;

  private TglParser   mParser;
  private RadioButton mRBtotal;
  private RadioButton mRBnghb6;
  private RadioButton mRBnghb26;
  private TextView    mTVcount;
  // private TextView    mTVcomputer;

  /** cstr
   * @param context context
   * @param parser  3D model data
   */
  public DialogFractal( Context context, TglParser parser )
  {
    super( context, R.string.DialogFractal );
    mParser = parser;
    mResult = new FractalResult( this );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout(R.layout.fractal_dialog, R.string.fractal_title );

    mTVcount = ( TextView ) findViewById(R.id.fractal_count_text);
    mTVcount.setText( "-" );

    // mTVcomputer = (TextView) findViewById( R.id.fractal_computer );
    // mTVcomputer.setText( (FractalResult.computer != null)? "fractal computer is running" : "fractal computer is idle" );
    // mTVcomputer.setText( R.string.fractal_idle );

    ((Button) findViewById( R.id.fractal_ok )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_cancel )).setOnClickListener( this );

    mCell = (EditText) findViewById( R.id.fractal_cell );
    mCell.setText( String.format(Locale.US, "%.1f", mCellSide ) );

    mCBsplays = (CheckBox) findViewById( R.id.fractal_splays );
    mImage    = (ImageView) findViewById( R.id.fractal_dims );

    // mImage.setImageBitmap( FractalResult.makeImage() );

    mRBtotal  = (RadioButton) findViewById( R.id.fractal_cnt_total );
    mRBnghb6  = (RadioButton) findViewById( R.id.fractal_cnt_six );
    mRBnghb26 = (RadioButton) findViewById( R.id.fractal_cnt_twentysix );
    mRBtotal.setChecked( true );
  }

  void showResult( FractalResult result )
  {
    TDLog.v("show fractal result");
    Bitmap bitmap = result.makeImage();
    mImage.setImageBitmap( bitmap );
    mTVcount.setText( result.countsString() );
    // mTVcomputer.setText( R.string.fractal_result );
  }

  /** respond to user taps
   * @param v tapped view
   */
  @Override
  public void onClick( View v )
  {
    // TDLog.v( "Fractal onClick()" );
    if ( v.getId() == R.id.fractal_ok ) {
      try {
        mCellSide = Double.parseDouble( mCell.getText().toString() );
      } catch ( NumberFormatException e ) {
        mCell.setError( mContext.getResources().getString(R.string.illegal_value) );
        return;
      }
      int mode = FractalComputer.COUNT_TOTAL;
      if ( mRBnghb6.isChecked() ) {
        mode = FractalComputer.COUNT_NGHB_6;
      } else if ( mRBnghb26.isChecked() ) {
        mode = FractalComputer.COUNT_NGHB_26;
      }
      if ( mResult.compute( mParser, mCBsplays.isChecked(), mCellSide, mode ) ) return;
      TDToast.make( R.string.fractal_failed );

    // } else if ( v.getId() == R.id.button_cancel ) {
    //   // nothing
    }
    TDLog.v("dismiss fractal dialog");
    dismiss();
  }

}

