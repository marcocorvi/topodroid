/* @file PhotoCommentDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This is the general purpose dialog to choose how to take photos:
 * - user can enter a comment for the photo
 * - user can decide whether to use TopoDroid camera or a Camera App
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;


// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

// import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.util.Log;

class PhotoCommentDialog extends MyDialog
                         implements View.OnClickListener
{
  private final ShotWindow mParent;

  private EditText mETcomment;     // photo comment
  private Button   mButtonOK;
  private CheckBox mCamera;        // whether to use camera app
  private long     mSid;           // shot id
  // private Button   mButtonCancel;
  private boolean  cameraCheck;

  /**
   * @param context   context
   * @param parent    parent shot list activity
   */
  PhotoCommentDialog( Context context, ShotWindow parent, long sid )
  {
    super( context, R.string.PhotoCommentDialog );
    mParent = parent;
    mSid    = sid;
    // TDLog.Log( TDLog.LOG_PHOTO, "PhotoComment");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    cameraCheck = TDandroid.checkCamera( mContext );
    // TDLog.Log(  TDLog.LOG_PHOTO, "PhotoComment onCreate" );
    initLayout(R.layout.photo_comment_dialog, R.string.title_photo_comment );
    

    mETcomment = (EditText) findViewById(R.id.photo_comment_comment);
    mButtonOK  = (Button) findViewById(R.id.photo_comment_ok );
    mCamera    = (CheckBox) findViewById(R.id.photo_camera );
    if ( ! cameraCheck ) {
      mCamera.setVisibility( View.GONE );
    } else {
      mCamera.setChecked( true );
    }

    mButtonOK.setOnClickListener( this );
    // mButtonCancel = (Button) findViewById(R.id.photo_comment_cancel );
    // mButtonCancel.setOnClickListener( this );
    ( (Button) findViewById(R.id.photo_comment_cancel ) ).setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoComment onClick() " + b.getText().toString() );

    if ( b == mButtonOK && mETcomment.getText() != null ) {
      // TDLog.Log( TDLog.LOG_PHOTO, "set photo comment " + mETcomment.getText().toString() );
      // mParent.insertPhoto( mETcomment.getText().toString() );
      int camera = ( cameraCheck && ! mCamera.isChecked() )? PhotoInfo.CAMERA_INTENT : PhotoInfo.CAMERA_TOPODROID;
      mParent.doTakePhoto( mSid, mETcomment.getText().toString(), camera );
    // } else if ( b == mButtonCancel ) {
      /* nothing */
    }
    dismiss();
  }

}

