/* @file QCamCompass.java
 *
 * @author marco corvi
 * @date jan. 2017
 *
 * @brief TopoDroid quick cam compass
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Activity;
import android.app.Dialog;
// import android.content.Intent;
import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
// import android.widget.FrameLayout;
// import android.widget.Toast;

import android.graphics.drawable.BitmapDrawable;

// import android.util.Log;
import java.util.Locale;

class QCamCompass extends Dialog
                         implements OnClickListener
                                  , IBearingAndClino
{
  private final Context mContext;
  // DrawingWindow mDrawer;
  // long mPid;

  private final IPhotoInserter mInserter;
  private QCamDrawingSurface mSurface;
  // private QCamBox mBox;
  private Button buttonClick;
  private Button buttonSave;
  private Button buttonCancel;

  private BitmapDrawable mBDcameraRed;
  private BitmapDrawable mBDcamera;
  private BitmapDrawable mBDsaveok;
  private BitmapDrawable mBDsaveoff;

  private TextView mTVdata;
  private float mBearing;
  private float mClino;
  private int   mOrientation;
  private boolean mHasBearingAndClino;
  private IBearingAndClino mCallback;
  private boolean mWithBox;
  private boolean mWithDelay;
  private boolean mHasSaved;
  private boolean mHasShot;

  QCamCompass( Context context, IBearingAndClino callback, 
               // DrawingWindow drawer, long pid, 
               IPhotoInserter inserter, boolean with_box, boolean with_delay )
  {
    super( context );
    mContext   = context;
    mCallback  = callback;
    // mDrawer    = drawer;
    // mPid       = pid;
    mInserter  = inserter;
    mWithBox   = with_box;
    mWithDelay = with_delay;
    mHasSaved  = false;
    mHasShot   = false;
  }

  void enableButtons( boolean enable )
  {
    buttonClick.setEnabled( enable );
    buttonCancel.setEnabled( enable );
    // if ( enable ) {
    //   buttonClick.setVisibility( View.VISIBLE );
    //   buttonCancel.setVisibility( View.VISIBLE );
    // } else {
    //   buttonClick.setVisibility( View.GONE );
    //   buttonCancel.setVisibility( View.GONE );
    // }
  }

  private void enableButtonSave( boolean enable )
  {
    buttonSave.setEnabled( enable );
    // buttonSave.setVisibility( enable? View.VISIBLE : View.GONE );
    buttonSave.setBackgroundDrawable( enable? mBDsaveok : mBDsaveoff );
  }


  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature( Window.FEATURE_NO_TITLE );
    setContentView(R.layout.qcam_compass);

    mSurface = (QCamDrawingSurface) findViewById( R.id.drawingSurface );
    mSurface.mQCam = this;

    // buttonClick  = (Button) findViewById(R.id.buttonClick);
    // buttonSave   = (Button) findViewById(R.id.buttonSave);
    // buttonCancel = (Button) findViewById(R.id.buttonQuit);

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    buttonClick  = MyButton.getButton( mContext, this, R.drawable.iz_camera_red_transp );
    buttonSave   = MyButton.getButton( mContext, this, R.drawable.iz_save_off_transp );
    buttonCancel = MyButton.getButton( mContext, this, R.drawable.iz_cancel_transp );

    mBDcameraRed = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_camera_red_transp );
    mBDcamera    = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_camera_transp );
    mBDsaveok    = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_save_transp );
    mBDsaveoff   = MyButton.getButtonBackground( mContext, mContext.getResources(), R.drawable.iz_save_off_transp );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );
    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    layout4.setMinimumHeight( size + 20 );
    layout4.addView( buttonClick, lp );
    layout4.addView( buttonSave, lp );
    layout4.addView( buttonCancel, lp );

    enableButtonSave( false );

    mHasShot = false;

    mTVdata = (TextView)findViewById( R.id.data );
    mHasBearingAndClino = false; 

    if ( mWithBox ) {
      QCamBox box = new QCamBox( mContext );
      addContentView( box, new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
    }
  }

  // implements
  public void setBearingAndClino( float b, float c, int o )
  {
    mBearing = b;
    mClino   = c;
    mOrientation = MyBearingAndClino.getCameraOrientation( o );
    // Log.v("DistoX", "QCam compass orient " + o + " --> " + mOrientation );
    mTVdata.setText( String.format(Locale.US, "%.2f %.2f", mBearing, mClino ) );
    mHasBearingAndClino = true;

    // take snapshot
    mHasShot = mSurface.takePicture( mOrientation );
    enableButtonSave( true );
    buttonClick.setBackgroundDrawable( mHasShot ? mBDcamera : mBDcameraRed );
    // buttonClick.setText( mContext.getString( mHasShot ? R.string.button_redo : R.string.button_eval ) );
  }


  public void setJpegData( byte[] data ) { }

  @Override
  public void onClick(View v)
  {
    Button b = (Button)v;
    if ( b == buttonClick ) {
      if ( mHasShot ) {
        mHasShot = false;
        buttonClick.setBackgroundDrawable( mBDcameraRed );
        // buttonClick.setText( mContext.getString( R.string.button_eval ) );
        mSurface.start();
        enableButtons( true );
      } else {
        int wait  = TDSetting.mTimerWait;
        int count = 10;
        if ( ! mWithDelay ) {
          wait = 0;
          count = 3;
        }
        enableButtons( false );
        enableButtonSave( false );
        TimerTask timer = new TimerTask( mContext, this, -TimerTask.Z_AXIS, wait, count );
        timer.execute();
      }
      return;
    } else if ( b == buttonSave ) {
      if ( mHasBearingAndClino ) {
        if ( mCallback != null ) {
          // Log.v("DistoX", "Orientation " + mOrientation );
          mCallback.setBearingAndClino( mBearing, mClino, mOrientation );
          mCallback.setJpegData( mSurface.mJpegData );
          mHasSaved = true;
        }
      }
    } else if ( b == buttonCancel ) {
    }
    mSurface.close();
    try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }

    if ( mHasSaved ) {
      if ( mInserter != null ) mInserter.insertPhoto();
      // if ( mDrawer   != null ) mDrawer.notifyAzimuthClino( mPid, mBearing, mClino );
    }
    dismiss();
  }
}
