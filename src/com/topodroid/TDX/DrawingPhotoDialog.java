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
                         , IGeoCoder
{
  private EditText mComment;
  private EditText mSize;
  private CheckBox mCamera;
  private boolean  cameraCheck;

  private final DrawingWindow mActivity;
  private final float mX;
  private final float mY;
  private String mGeoCode = "";

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
    mSize    = (EditText) findViewById(R.id.photo_size );
    mCamera  = (CheckBox) findViewById( R.id.photo_camera );
    if ( ! cameraCheck ) {
      mCamera.setVisibility( View.GONE );
    } else {
      mCamera.setChecked( true );
    }

    ((Button) findViewById(R.id.photo_ok)).setOnClickListener( this );
    ((Button) findViewById(R.id.photo_cancel)).setOnClickListener( this );
    if ( TDLevel.overExpert ) {
      GeoCodes geocodes = TopoDroidApp.getGeoCodes();
      if ( geocodes.size() > 0 ) {
        ((Button) findViewById(R.id.photo_code)).setOnClickListener( this );
      } else {
        ((Button) findViewById(R.id.photo_code)).setVisibility( View.GONE );
      }
    } else {
      ((Button) findViewById(R.id.photo_code)).setVisibility( View.GONE );
    }

    mComment.setTextSize( TDSetting.mTextSize );
    mSize.setTextSize( TDSetting.mTextSize );
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Photo Dialog onClick() " + view.toString() );
    if (view.getId() == R.id.photo_ok ) {
      int camera = ( cameraCheck && mCamera.isChecked() )? PhotoInfo.CAMERA_TOPODROID : PhotoInfo.CAMERA_TOPODROID_2;

      float size = 2 * TDSetting.mPictureMin; // pixels: 10 pixels means a square of size 10+10 = 20 = 1 meter
      if ( mSize.getText() != null ) {
        try {
          size = Float.parseFloat( mSize.getText().toString() );
          if ( size < TDSetting.mPictureMin ) size = TDSetting.mPictureMin;
          if ( size > TDSetting.mPictureMax ) size = TDSetting.mPictureMax;
        } catch ( NumberFormatException e ) {
          size = 1;
        }
      }
      mActivity.addPhotoPoint( mComment.getText().toString(), size, mX, mY, camera, mGeoCode );
    } else if ( view.getId() == R.id.photo_code ) {
      (new GeoCodeDialog( mContext, this, mGeoCode )).show();
      return;
    // } else if ( view.getId() == R.id.photo_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }

  public void setGeoCode( String geocode ) { mGeoCode = (geocode == null)? "" : geocode; }
}
        

