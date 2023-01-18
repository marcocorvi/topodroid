/* @file TDProgress.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid progress dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

// import com.topodroid.utils.TDLog;
// import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.TopoDroidApp;
// import com.topodroid.TDX.TDandroid;
import com.topodroid.TDX.R;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

// import android.content.Intent;

// import android.widget.TextView;
import android.widget.TextView;
import android.widget.Button;
// import android.widget.SeekBar;
import android.widget.ProgressBar;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.graphics.Bitmap;
// import android.graphics.drawable.BitmapDrawable;

public class TDProgress extends MyDialog // Activity
                 implements View.OnClickListener
{
  public final static int PROGRESS_UPLOAD   = 1;
  public final static int PROGRESS_DOWNLOAD = 2;

  private final int MAX = 100;
  // private TextView mTVtitle;   // dialog title
  private TextView mText;  
  private ProgressBar   mSlider;
  private boolean mDone = false;
  private String mFilename;
  private int    mLength;
  private String mMsg;
  private int    mWhat;

  private Button mBtnOk;
  private Button mBtnClose;

  /** cstr
   * @param context   context
   * @param app       application
   * @param filename  name of the firmware file
   * @param len       length of the firmware data
   * @param text      initial display message
   * @param what      what to do: UPLOAD or DOWNLOAD
   */
  public TDProgress( Context context, TopoDroidApp app, String filename, long len, String text, int what )
  {
    super( context, app, 0 ); // no help
    mFilename = filename;
    mLength   = (int)len;
    mMsg      = text;
    mWhat     = what;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.progress_dialog, -1 ); // no title

    // mTVtitle  = (TextView) findViewById(R.id.note_title );
    mText   = (TextView) findViewById(R.id.message );
    mText.setText( mMsg );

    mSlider = (ProgressBar) findViewById( R.id.progress );
    mSlider.setMax( MAX );

    // Bitmap bitmap = MyButton.getLVRseekbarBackGround( mContext, (int)(TopoDroidApp.mDisplayWidth), 20 );
    // if ( bitmap != null ) {
    //   BitmapDrawable background = new BitmapDrawable( mContext.getResources(), bitmap );
    //   TDandroid.setSeekBarBackground( mSlider, background ); 
    // }

    // mButtonCancel = (Button) findViewById(R.id.button_cancel );
    // mButtonOk     = (Button) findViewById(R.id.button_ok );

    mBtnOk    = (Button) findViewById(R.id.button_ok );
    mBtnOk.setOnClickListener( this );
    if ( mWhat == PROGRESS_DOWNLOAD ) mBtnOk.setText( R.string.button_download );

    mBtnClose = (Button) findViewById(R.id.button_cancel );
    mBtnClose.setOnClickListener( this );
  }

  /** implement response to user tap action
   * @param v    tapped view
   */
  @Override
  public void onClick(View v) 
  { 
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    if ( v.getId() == R.id.button_ok ) {
      if ( ! mDone ) {
        mBtnOk.setOnClickListener( null );
        mBtnOk.setText( R.string.button_wait );
        mBtnClose.setVisibility( View.GONE );
        if ( mWhat == PROGRESS_UPLOAD ) {
          mApp.uploadFirmware( mFilename, this );
        } else {
          mApp.dumpFirmware( mFilename, this );
        }
      } else {
        dismiss();
      }
    } else {
      // R.id.button_cancel dismiss
      dismiss();
    }
  }

  /** set the progress bar
   * @param value   progress value from 0 to mLength
   */
  public void setProgress( int value ) 
  { 
    // TDLog.v("Set progress " + value );
    mSlider.setProgress( (int)((MAX * value)/mLength) );
    mSlider.postInvalidate();
  }

  /** set the text message
   * @param text   display message
   */
  public void setText( String text ) 
  { 
    // TDLog.v("Set text " + text );
    if ( text != null ) mText.setText( text );
    mText.postInvalidate();
  }

  /** set the "done" flag to true
   * @param text   display message
   * @note when "done" is true the OK button closes the dialog
   */
  public void setDone( boolean result, String text ) 
  {
    // TDLog.v("Set done");
    mDone = true; 
    if ( result ) setProgress( mLength );
    if ( text != null ) mText.setText( text );
    mText.postInvalidate();
    mBtnOk.setOnClickListener( this );
    mBtnOk.setText( R.string.button_close );
    // mBtnClose.setVisibility( View.VISIBLE );
  }

  // unused
  // public void setTheTitle( String title ) { setTitle( title ); }

}


