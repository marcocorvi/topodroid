/* @file DrawingPhotoDialog.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid drawing: dialog for the photo of a point "photo"
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;


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


class DrawingPhotoDialog extends MyDialog
                         implements View.OnClickListener
{
  private EditText mComment;

  private final ILabelAdder mActivity;
  private final float mX;
  private final float mY;

  DrawingPhotoDialog( Context context, ILabelAdder activity, float x, float y )
  {
    super(context, R.string.DrawingPhotoDialog );
    mActivity = activity;
    mX = x; 
    mY = y;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_photo_dialog, R.string.photo_title );

    mComment   = (EditText) findViewById(R.id.photo_comment);

    ((Button) findViewById(R.id.photo_ok)).setOnClickListener( this );
    ((Button) findViewById(R.id.photo_cancel)).setOnClickListener( this );

    mComment.setTextSize( TDSetting.mTextSize );
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingPhotoDialog onClick() " + view.toString() );
    if (view.getId() == R.id.photo_ok ) {
      mActivity.addPhotoPoint( mComment.getText().toString(), mX, mY );
    // } else if ( view.getId() == R.id.photo_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }
}
        

