/* @file DrawingLabelDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for the text of a label-point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.*;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;


class DrawingLabelDialog extends MyDialog
                                implements View.OnClickListener
{
  private EditText mLabel;

  private final ILabelAdder mActivity;
  private final float mX;
  private final float mY;

  DrawingLabelDialog( Context context, ILabelAdder activity, float x, float y )
  {
    super(context, R.string.DrawingLabelDialog );
    mActivity = activity;
    mX = x; 
    mY = y;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_label_dialog, R.string.label_title );

    mLabel     = (EditText) findViewById(R.id.label_text);

    ((Button) findViewById(R.id.label_ok)).setOnClickListener( this );
    ((Button) findViewById(R.id.label_cancel)).setOnClickListener( this );

    mLabel.setTextSize( TDSetting.mTextSize );

  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingLabelDialog onClick() " + view.toString() );
    if (view.getId() == R.id.label_ok ) {
      mActivity.addLabel( mLabel.getText().toString(), mX, mY );
    // } else if ( view.getId() == R.id.label_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }
}
        

