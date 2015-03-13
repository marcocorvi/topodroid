/** @file DrawingAreaDialog.java
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
 * 20121225 created
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

public class DrawingAreaDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingAreaPath mArea;
  private DrawingActivity mParent;

  // private TextView mTVtype;
  // private EditText mEToptions;
 
  private CheckBox mCBvisible;

  private Button   mBtnOk;
  private Button   mBtnErase;
  // private Button   mBtnCancel;

  public DrawingAreaDialog( DrawingActivity context, DrawingAreaPath line )
  {
    super( context );
    mParent = context;
    mArea = line;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_area_dialog);

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_area ),
              DrawingBrushPaths.getAreaName( mArea.mAreaType ) ) );

    // mTVtype = (TextView) findViewById( R.id.area_type );
    // mTVtype.setText( DrawingBrushPaths.getAreaThName( mArea.areaType() ) );

    // mEToptions = (EditText) findViewById( R.id.area_options );
    // if ( mArea.mOptions != null ) {
    //   mEToptions.setText( mArea.mOptions );
    // }

    mCBvisible = (CheckBox) findViewById( R.id.area_visible );
    mCBvisible.setChecked( mArea.mVisible );

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingAreaDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      // if ( mEToptions.getText() != null ) {
      //   String options = mEToptions.getText().toString().trim();
      //   if ( options.length() > 0 ) mArea.mOptions = options;
      // }
      mArea.mVisible = mCBvisible.isChecked();
    } else if ( b == mBtnErase ) {
      mParent.deleteArea( mArea );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

