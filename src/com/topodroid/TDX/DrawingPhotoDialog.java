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
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
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
import android.widget.CheckBox;
import android.widget.EditText;


class DrawingPhotoDialog extends MyDialog
                         implements View.OnClickListener
{
  private EditText mComment;
  private CheckBox mCamera;
  private boolean  cameraCheck;

  private final DrawingWindow mActivity;
  private final float mX;
  private final float mY;

  DrawingPhotoDialog( Context context, DrawingWindow activity, float x, float y )
  {
    super(context, null, R.string.DrawingPhotoDialog ); // null app
    mActivity = activity;
    mX = x; 
    mY = y;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    cameraCheck = TDandroid.AT_LEAST_API_21 && TDandroid.checkCamera( mContext );
    initLayout( R.layout.drawing_photo_dialog, R.string.photo_title );

    mComment = (EditText) findViewById(R.id.photo_comment);
    mCamera  = (CheckBox) findViewById( R.id.photo_camera );
    if ( ! cameraCheck ) {
      mCamera.setVisibility( View.GONE );
    } else {
      mCamera.setChecked( true );
    }

    ((Button) findViewById(R.id.photo_ok)).setOnClickListener( this );
    ((Button) findViewById(R.id.photo_cancel)).setOnClickListener( this );

    mComment.setTextSize( TDSetting.mTextSize );
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Photo Dialog onClick() " + view.toString() );
    if (view.getId() == R.id.photo_ok ) {
      int camera = ( cameraCheck && mCamera.isChecked() )? PhotoInfo.CAMERA_TOPODROID : PhotoInfo.CAMERA_TOPODROID_2;
      mActivity.addPhotoPoint( mComment.getText().toString(), mX, mY, camera );
    // } else if ( view.getId() == R.id.photo_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }
}
        

