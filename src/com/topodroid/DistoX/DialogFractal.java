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
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
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

class DialogFractal extends MyDialog 
                    implements View.OnClickListener
{
  private CheckBox mCBsplays;
  private ImageView mImage;
  // private Button mBtnClose;
  private EditText mCell;
  private static int mCellSide = 2;

  private TglParser   mParser;
  private RadioButton mRBtotal;
  private RadioButton mRBnghb6;
  private RadioButton mRBnghb26;

  /** cstr
   * @param context context
   * @param parser  3D model data
   */
  public DialogFractal( Context context, TglParser parser )
  {
    super( context, R.string.DialogFractal );
    mParser = parser;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout(R.layout.fractal_dialog, R.string.fractal_title );

    TextView tv = ( TextView ) findViewById(R.id.fractal_count_text);
    tv.setText( FractalResult.countsString() );

    tv = (TextView) findViewById( R.id.fractal_computer );
    tv.setText( (FractalResult.computer != null)? "fractal computer is running" : "fractal computer is idle" );

    ((Button) findViewById( R.id.fractal_ok )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_cancel )).setOnClickListener( this );

    mCell = (EditText) findViewById( R.id.fractal_cell );
    mCell.setText( Integer.toString( mCellSide ) );

    mCBsplays = (CheckBox) findViewById( R.id.fractal_splays );
    mImage    = (ImageView) findViewById( R.id.fractal_dims );

    mImage.setImageBitmap( FractalResult.makeImage() );

    mRBtotal  = (RadioButton) findViewById( R.id.fractal_cnt_total );
    mRBnghb6  = (RadioButton) findViewById( R.id.fractal_cnt_six );
    mRBnghb26 = (RadioButton) findViewById( R.id.fractal_cnt_twentysix );
  }

  /** respond to user taps
   * @param v tapped view
   */
  @Override
  public void onClick( View v )
  {
    // TDLog.v( "Fractal onClick()" );
    if ( v.getId() == R.id.fractal_ok ) {
      mCellSide = Integer.parseInt( mCell.getText().toString() );
      int mode = FractalComputer.COUNT_TOTAL;
      if ( mRBnghb6.isChecked() ) {
        mode = FractalComputer.COUNT_NGHB_6;
      } else if ( mRBnghb26.isChecked() ) {
        mode = FractalComputer.COUNT_NGHB_26;
      }
      int ret = FractalResult.compute( mParser, mCBsplays.isChecked(), mCellSide, mode );
    // } else if ( v.getId() == R.id.button_cancel ) {
    //   // nothing
    }
    dismiss();
  }

}

