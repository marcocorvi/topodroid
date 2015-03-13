/* @file PhotoCommentDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo comment dialog (to enter the comment of the photo)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

// import java.Thread;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

// import android.util.Log;

public class PhotoCommentDialog extends Dialog
                         implements View.OnClickListener
{
  private ShotActivity mParent;

  private EditText mETcomment;     // photo comment
  private Button   mButtonOK;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  PhotoCommentDialog( Context context, ShotActivity parent )
  {
    super( context );
    mParent = parent;
    // TopoDroidLog.Log( TopoDroidLog.LOG_PHOTO, "PhotoCommentDialog");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidLog.Log(  TopoDroidLog.LOG_PHOTO, "PhotoCommentDialog onCreate" );
    // Log.v(  TopoDroidApp.TAG, "PhotoCommentDialog onCreate" );
    setContentView(R.layout.photo_comment_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETcomment    = (EditText) findViewById(R.id.photo_comment_comment);
    mButtonOK     = (Button) findViewById(R.id.photo_comment_ok );
    // mButtonCancel = (Button) findViewById(R.id.photo_comment_cancel );

    setTitle( R.string.title_photo_comment );

    mButtonOK.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "PhotoCommentDialog onClick() " + b.getText().toString() );
    // Log.v(  TopoDroidApp.TAG, "PhotoCommentDialog onClick() " + b.getText().toString() );

    if ( b == mButtonOK && mETcomment.getText() != null ) {
      // TopoDroidLog.Log( TopoDroidLog.LOG_PHOTO, "set photo comment " + mETcomment.getText().toString() );
      // Log.v( TopoDroidApp.TAG, "set photo comment " + mETcomment.getText().toString() );
      // mParent.insertPhoto( mETcomment.getText().toString() );
      mParent.doTakePhoto( mETcomment.getText().toString() );
    }
    dismiss();
  }

}

