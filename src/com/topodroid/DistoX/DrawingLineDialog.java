/** @file DrawingLineDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class DrawingLineDialog extends Dialog
                               implements View.OnClickListener
{
  private Context mContext;
  private DrawingLinePath mLine;
  private DrawingActivity mParent;
  private LinePoint mPoint;

  // private TextView mTVtype;
  private EditText mEToptions;
 
  private CheckBox mBtnOutlineOut;
  private CheckBox mBtnOutlineIn;
  // private RadioButton mBtnOutlineNone;


  private Button mBtnOk;

  private MyCheckBox mReversed;
  private MyCheckBox mBtnSharp;
  private MyCheckBox mBtnRock;
  private MyCheckBox mBtnClose;
  // private Button   mBtnSplit;
  // private Button   mBtnCancel;
  // private Button   mBtnErase;

  public DrawingLineDialog( DrawingActivity context, DrawingLinePath line, LinePoint lp )
  {
    super( context );
    mContext = context;
    mParent  = context;
    mLine  = line;
    mPoint = lp;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_line_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_line ),
              DrawingBrushPaths.mLineLib.getSymbolName( mLine.mLineType ) ) );

    // mTVtype = (TextView) findViewById( R.id.line_type );
    mEToptions = (EditText) findViewById( R.id.line_options );

    // mTVtype.setText( DrawingBrushPaths.mLineLib.getSymbolThName( mLine.mLineType ) );
    mEToptions.setText( mLine.getOptionString() );

    mBtnOutlineOut  = (CheckBox) findViewById( R.id.line_outline_out );
    mBtnOutlineIn   = (CheckBox) findViewById( R.id.line_outline_in );
    // mBtnOutlineNone = (RadioButton) findViewById( R.id.line_outline_none );

    if ( mLine.mOutline == DrawingLinePath.OUTLINE_OUT ) {
      mBtnOutlineOut.setChecked( true );
    } else if ( mLine.mOutline == DrawingLinePath.OUTLINE_IN ) {
      mBtnOutlineIn.setChecked( true );
    // } else if ( mLine.mOutline == DrawingLinePath.OUTLINE_NONE ) {
    //   mBtnOutlineNone.setChecked( true );
    }

    // mReversed = (CheckBox) findViewById( R.id.line_reversed );

    mBtnOutlineOut.setOnClickListener( this );
    mBtnOutlineIn.setOnClickListener( this );

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    int size = TopoDroidApp.getScaledSize( mContext );
    mReversed = new MyCheckBox( mContext, size, R.drawable.iz_reverse_ok, R.drawable.iz_reverse_no );
    mBtnSharp = new MyCheckBox( mContext, size, R.drawable.iz_sharp_ok, R.drawable.iz_sharp_no );
    mBtnRock  = new MyCheckBox( mContext, size, R.drawable.iz_rock_ok,  R.drawable.iz_rock_no  );
    mBtnClose = new MyCheckBox( mContext, size, R.drawable.iz_close_ok, R.drawable.iz_close_no );
    mReversed.setChecked( mLine.isReversed() );

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 10, 20, 10 );

    LinearLayout layout3 = (LinearLayout)findViewById( R.id.layout3 );
    layout3.addView( mReversed, lp );
    layout3.addView( mBtnSharp, lp );
    layout3.addView( mBtnRock, lp );
    layout3.addView( mBtnClose, lp );


    // mBtnSharp = (Button) findViewById( R.id.button_sharp );
    // mBtnSharp.setOnClickListener( this );
    // mBtnRock = (Button) findViewById( R.id.button_rock );
    // mBtnRock.setOnClickListener( this );
    // mBtnClose = (Button) findViewById( R.id.button_close );
    // mBtnClose.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    // mBtnSplit = (Button) findViewById( R.id.button_split );
    // mBtnSplit.setOnClickListener( this );

    // mBtnErase = (Button) findViewById( R.id.button_erase );
    // mBtnErase.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingLineDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOutlineIn ) {
      mBtnOutlineOut.setChecked( false );
      return;
    } else if ( b == mBtnOutlineOut ) {
      mBtnOutlineIn.setChecked( false );
      return;
    
    } else if ( b == mBtnOk ) {
      if ( mEToptions.getText() != null ) {
        String options = mEToptions.getText().toString().trim();
        if ( options.length() > 0 ) mLine.setOptions( options );
      }
      if ( mBtnOutlineOut.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_OUT;
      else if ( mBtnOutlineIn.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_IN;
      else /* if ( mBtnOutlineNone.isChecked() ) */ mLine.mOutline = DrawingLinePath.OUTLINE_NONE;

      mLine.setReversed( mReversed.isChecked() );

      if ( mBtnSharp.isChecked() ) {
        mParent.sharpenLine( mLine );
      } 
      if ( mBtnRock.isChecked() ) {
        mParent.reduceLine( mLine );
      }
      if ( mBtnClose.isChecked() ) {
        mParent.closeLine( mLine );
      }

    // } else if ( b == mBtnSharp ) {
    //   mParent.sharpenLine( mLine, false );
    // } else if ( b == mBtnRock ) {
    //   mParent.sharpenLine( mLine, true );
    // } else if ( b == mBtnClose ) {
    //   mParent.closeLine( mLine );
    }
    dismiss();
  }

}

