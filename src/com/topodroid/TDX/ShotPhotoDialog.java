/* @file ShotPhotoDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog for a shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This is the general purpose dialog to choose how to take photos:
 * - user can enter a comment for the photo
 * - user can decide whether to use TopoDroid camera or a Camera App
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

class ShotPhotoDialog extends MyDialog
                         implements View.OnClickListener
{
  private final ShotWindow mParent;

  private EditText mETcomment;     // photo comment
  private Button   mButtonOK;
  private CheckBox mCamera;        // whether to use camera or camera2
  private long     mSid;           // shot id
  private String   mName;          // shot name
  // private Button   mButtonCancel;
  private boolean  cameraAPI;

  /**
   * @param context   context
   * @param parent    parent shot list activityA
   * @param sid       shot id
   * @param name      shot name
   */
  ShotPhotoDialog( Context context, ShotWindow parent, long sid, String name )
  {
    super( context, null, R.string.ShotPhotoDialog ); // null app
    mParent = parent;
    mSid    = sid;
    mName   = name;
    // TDLog.Log( TDLog.LOG_PHOTO, "PhotoComment");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    if ( ! TDandroid.checkCamera( mContext ) ) {
      TDToast.makeWarn( R.string.warning_nogrant_camera );
      dismiss();
    } else {
      cameraAPI = TDandroid.BELOW_API_21;
      // TDLog.Log(  TDLog.LOG_PHOTO, "PhotoComment onCreate" );
      initLayout(R.layout.photo_comment_dialog, R.string.title_photo_comment );
      
      TextView tv = (TextView) findViewById(R.id.photo_shot_name );
      tv.setText( String.format( mContext.getResources().getString( R.string.shot_name ), mName ) );
      mETcomment = (EditText) findViewById(R.id.photo_comment_comment);
      mButtonOK  = (Button) findViewById(R.id.photo_comment_ok );
      mCamera    = (CheckBox) findViewById(R.id.photo_camera );
      if ( cameraAPI ) { // use old Camera API
        mCamera.setVisibility( View.GONE );
      } else {
        mCamera.setChecked( true );  // checked = use old Camera API
      }

      mButtonOK.setOnClickListener( this );
      // mButtonCancel = (Button) findViewById(R.id.photo_comment_cancel );
      // mButtonCancel.setOnClickListener( this );
      ( (Button) findViewById(R.id.photo_comment_cancel ) ).setOnClickListener( this );
    }
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mButtonOK ) {
      String comment = "";
      if ( mETcomment.getText() != null ) comment = mETcomment.getText().toString().trim();
      // if ( comment.length() == 0 ) { // this was annoying
      //   mETcomment.setError(  mContext.getResources().getString( R.string.error_text_required ) );
      //   return;
      // }
      // TDLog.v( "PHOTO comment " + comment );
      int camera = ( cameraAPI || mCamera.isChecked() )? PhotoInfo.CAMERA_TOPODROID : PhotoInfo.CAMERA_TOPODROID_2;
      // TDLog.v("camera " + camera + " old-API " + cameraAPI + ", checked " + mCamera.isChecked() );
      // int camera = PhotoInfo.CAMERA_TOPODROID;
      mParent.doTakePhoto( mSid, comment, camera );
    // } else if ( b == mButtonCancel ) {
      /* nothing */
    }
    dismiss();
  }

}

