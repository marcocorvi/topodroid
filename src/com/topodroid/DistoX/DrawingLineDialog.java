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
 * CHANGES
 * 20121225 implemented erase
 * 20130826 added splitLine
 * 201312   button to make the line sharp
 * 201401   button to make the line like a "rock" path
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.view.View;

public class DrawingLineDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingLinePath mLine;
  private DrawingActivity mParent;
  private LinePoint mPoint;

  // private TextView mTVtype;
  private EditText mEToptions;
 
  private CheckBox mBtnOutlineOut;
  private CheckBox mBtnOutlineIn;
  // private RadioButton mBtnOutlineNone;

  private CheckBox mReversed;

  private Button mBtnOk;
  private Button mBtnSharp;
  private Button mBtnRock;
  // private Button   mBtnSplit;
  // private Button   mBtnCancel;
  private Button   mBtnErase;

  public DrawingLineDialog( DrawingActivity context, DrawingLinePath line, LinePoint lp )
  {
    super( context );
    mParent = context;
    mLine = line;
    mPoint = lp;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_line_dialog);

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_line ),
              DrawingBrushPaths.getLineName( mLine.mLineType ) ) );

    // mTVtype = (TextView) findViewById( R.id.line_type );
    mEToptions = (EditText) findViewById( R.id.line_options );

    // mTVtype.setText( DrawingBrushPaths.getLineThName( mLine.mLineType ) );
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

    mReversed = (CheckBox) findViewById( R.id.line_reversed );
    mReversed.setChecked( mLine.mReversed );

    mBtnOutlineOut.setOnClickListener( this );
    mBtnOutlineIn.setOnClickListener( this );

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    mBtnSharp = (Button) findViewById( R.id.button_sharp );
    mBtnSharp.setOnClickListener( this );

    mBtnRock = (Button) findViewById( R.id.button_rock );
    mBtnRock.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    // mBtnSplit = (Button) findViewById( R.id.button_split );
    // mBtnSplit.setOnClickListener( this );

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
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
    // } else if ( b == mBtnSplit   ) {
    //   mParent.splitLine( mLine, mPoint );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    } else if ( b == mBtnSharp ) {
      mParent.sharpenLine( mLine, false );
    } else if ( b == mBtnRock ) {
      mParent.sharpenLine( mLine, true );
    } else if ( b == mBtnErase ) {
      mParent.deleteLine( mLine, null );
    }
    dismiss();
  }

}

